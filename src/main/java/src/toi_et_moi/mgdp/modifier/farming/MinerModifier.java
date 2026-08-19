package src.toi_et_moi.mgdp.modifier.farming;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import dev.xkmc.modulargolems.content.modifier.special.PickupModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import src.toi_et_moi.mgdp.Config;
import src.toi_et_moi.mgdp.compat.L2Compat;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * 挖矿升级（蓝色升级）
 * <p>
 * 最高三级，以傀儡自身为中心、每级三维半径 6 格（6/12/18 的立方体范围）。
 * 需要拾取升级才工作。
 * 主手持镐（isCorrectToolForDrops）时自动开采 #forge:ores 标签内的矿石与
 * Config 自定义方块，掉落按手持镐计算（效率/时运/精准采集全部生效），
 * 镐耐久正常消耗。效率附魔提高挖掘吞吐：工作预算制下每挖一块消耗
 * max(1, 4-效率等级) 点工作点（每 tick +1，上限 4），无效率约 5 块/秒，
 * 效率 V 约 20 块/秒。三级解锁连锁挖矿（同种矿石相邻最多 8 块，
 * 每块仅消耗 0.25 耐久）。
 * <p>
 * 安全阀：战斗中有目标时停挖；只挖已加载区块；名单外方块不碰。
 * 扫描采用滚动列模式（同 HarvestCropModifier），把全量扫描摊平到每个 tick；
 * 挖到矿物即显示动作栏提示（1 秒窗口内连续挖矿则累加数字并刷新显示，不割裂），
 * 新窗口开始时播一次音效。
 */
public class MinerModifier extends GolemModifier {

	private static final int CYCLE = 100;                // 全周期目标长度：约 5 秒扫完整个范围
	private static final int RANGE_PER_LEVEL = 6;        // 每级三维半径（矿洞上下都可能有矿）
	private static final int CHAIN_MAX = 8;              // 三级连锁最大块数
	private static final float CHAIN_DURABILITY = 0.25F; // 连锁每块耐久消耗（增强：只消耗一点）
	private static final int MAX_WORK = 4;               // 工作点上限（= 无效率附魔时每块消耗的工作点）
	private static final int DISPLAY_WINDOW = 20;        // 动作栏累加窗口：1 秒（20 tick）

	private static final Map<UUID, Integer> COLUMN_CURSORS = new WeakHashMap<>();
	private static final Map<UUID, long[]> MINED_STATE = new WeakHashMap<>(); // [0]=窗口内累计, [1]=上次挖矿 tick
	private static final Map<UUID, Integer> WORK_BUDGET = new WeakHashMap<>();

	public MinerModifier() {
		super(StatFilterType.MASS, 3);
	}

	@Override
	public List<MutableComponent> getDetail(int v) {
		int range = v * RANGE_PER_LEVEL;
		return List.of(Component.translatable(getDescriptionId() + ".desc", range).withStyle(ChatFormatting.GREEN));
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		if (level <= 0) return;
		// 安全阀：战斗停挖
		if (golem.getTarget() != null) return;
		// 工具门槛：主手必须持有工具
		ItemStack tool = golem.getMainHandItem();
		if (tool.isEmpty()) return;
		// 前置：需要拾取升级
		if (!hasPickup(golem)) return;
		if (!(golem.level() instanceof ServerLevel sl)) return;

		int range = level * RANGE_PER_LEVEL;
		int side = range * 2 + 1;
		int totalColumns = side * side;
		int columnsPerTick = Math.max(1, (totalColumns + CYCLE - 1) / CYCLE);

		Level levelW = golem.level();
		BlockPos center = golem.blockPosition();
		Set<String> extra = Config.minerExtraBlocks;

		UUID id = golem.getUUID();
		// 工作预算制：每 tick +1 工作点（上限 MAX_WORK）；每挖一块消耗
		// max(1, 4-效率等级) 点。效率 V 一块一点（≈20 块/秒），无效率四点一块（≈5 块/秒）。
		int[] budget = { Math.min(MAX_WORK, WORK_BUDGET.getOrDefault(id, 0) + 1) };
		int cost = Math.max(1, MAX_WORK - getEfficiency(tool));

		int cursor = COLUMN_CURSORS.getOrDefault(id, 0);
		int mined = 0;
		for (int i = 0; i < columnsPerTick && budget[0] >= cost; i++) {
			mined += scanColumn(golem, sl, levelW, center, range, level, (cursor + i) % totalColumns, extra, budget, cost);
		}
		int next = (cursor + columnsPerTick) % totalColumns;
		COLUMN_CURSORS.put(id, next);
		WORK_BUDGET.put(id, budget[0]);

		// 动作栏反馈：挖到就立即显示；1 秒窗口内再次挖到则重置显示时间并累加数字，
		// 避免数字来回跳的割裂感。新窗口开始时播一次音效（防刷屏）。
		if (mined > 0) {
			long now = golem.level().getGameTime();
			long[] state = MINED_STATE.get(id);
			boolean fresh = state == null || now - state[1] > DISPLAY_WINDOW;
			int count = fresh ? mined : (int) state[0] + mined;
			MINED_STATE.put(id, new long[]{count, now});
			feedback(golem, count, fresh);
		}
	}

	private boolean hasPickup(AbstractGolemEntity<?, ?> golem) {
		for (var entry : golem.getModifiers().entrySet()) {
			if (entry.getKey() instanceof PickupModifier) return entry.getValue() > 0;
		}
		return false;
	}

	private int scanColumn(AbstractGolemEntity<?, ?> golem, ServerLevel sl, Level level, BlockPos center,
			int range, int modifierLevel, int col, Set<String> extra, int[] budget, int cost) {
		int side = range * 2 + 1;
		int dx = col % side - range;
		int dz = col / side - range;
		int mined = 0;
		for (int dy = -range; dy <= range; dy++) {
			BlockPos pos = center.offset(dx, dy, dz);
			if (!level.isLoaded(pos)) continue;
			BlockState state = level.getBlockState(pos);
			if (state.isAir()) continue;
			ItemStack tool = golem.getMainHandItem();
			if (tool.isEmpty()) return mined; // 工具坏了，本列停工
			if (!isMineable(state, extra)) continue;
			if (!tool.isCorrectToolForDrops(state)) continue;

			Block block = state.getBlock();
			if (budget[0] < cost) return mined; // 工作点不足，本列停工
			budget[0] -= cost;
			mineBlock(golem, sl, level, pos, state, tool, 1.0F);
			mined++;
			// 三级：连锁同种矿石，每块只消耗一点耐久
			if (modifierLevel >= 3) {
				mined += chainMine(golem, sl, level, pos, block, tool, budget, cost);
			}
		}
		return mined;
	}

	private boolean isMineable(BlockState state, Set<String> extra) {
		if (state.is(Tags.Blocks.ORES)) return true;
		if (extra != null && !extra.isEmpty()) {
			var key = ForgeRegistries.BLOCKS.getKey(state.getBlock());
			return key != null && extra.contains(key.toString());
		}
		return false;
	}

	private boolean mineBlock(AbstractGolemEntity<?, ?> golem, ServerLevel sl, Level level,
			BlockPos pos, BlockState state, ItemStack tool, float durabilityCost) {
		BlockEntity be = level.getBlockEntity(pos);
		List<ItemStack> drops = Block.getDrops(state, sl, pos, be, golem, tool);
		L2Compat.tryAutoSmelt(sl, tool, drops); // 自动冶炼（莱特兰扩充附魔）
		for (ItemStack drop : drops) {
			Block.popResource(level, pos, drop);
		}
		state.spawnAfterBreak(sl, pos, tool, true);
		level.levelEvent(2001, pos, Block.getId(state));
		level.removeBlock(pos, false);
		if (durabilityCost >= 1.0F) {
			tool.hurtAndBreak((int) durabilityCost, golem, e -> e.broadcastBreakEvent(InteractionHand.MAIN_HAND));
		}
		return true;
	}

	private int chainMine(AbstractGolemEntity<?, ?> golem, ServerLevel sl, Level level,
			BlockPos start, Block block, ItemStack tool, int[] budget, int cost) {
		Set<BlockPos> visited = new HashSet<>();
		Queue<BlockPos> queue = new ArrayDeque<>();
		queue.add(start);
		visited.add(start);
		int mined = 0;
		float durability = 0.0F;
		while (!queue.isEmpty() && mined < CHAIN_MAX && budget[0] >= cost) {
			BlockPos pos = queue.poll();
			BlockState state = level.getBlockState(pos);
			if (state.isAir() || !state.is(block)) continue; // 起点已被挖掉，跳过
			budget[0] -= cost;
			mineBlock(golem, sl, level, pos, state, tool, 0.0F); // 耐久单独累计
			durability += CHAIN_DURABILITY;
			mined++;
			for (Direction dir : Direction.values()) {
				BlockPos n = pos.relative(dir);
				if (visited.contains(n)) continue;
				if (!level.isLoaded(n)) continue;
				if (level.getBlockState(n).is(block)) {
					visited.add(n);
					queue.add(n);
				}
			}
		}
		if (durability >= 1.0F) {
			tool.hurtAndBreak((int) durability, golem, e -> e.broadcastBreakEvent(InteractionHand.MAIN_HAND));
		}
		return mined;
	}

	private void feedback(AbstractGolemEntity<?, ?> golem, int count, boolean fresh) {
		if (golem.getOwner() instanceof ServerPlayer player) {
			player.displayClientMessage(Component.translatable("mgdp.miner.mined", golem.getDisplayName(), count), true);
		}
		if (fresh) {
			golem.level().playSound(null, golem.blockPosition(),
					SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.5F, 1.2F);
		}
	}

	private int getEfficiency(ItemStack tool) {
		return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, tool);
	}
}
