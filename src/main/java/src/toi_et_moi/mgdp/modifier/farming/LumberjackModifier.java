package src.toi_et_moi.mgdp.modifier.farming;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import src.toi_et_moi.mgdp.compat.L2Compat;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.ArrayDeque;

/**
 * 伐木升级（蓝色升级）——从收获作物升级中独立出来的砍树功能
 * <p>
 * 主手持斧时，自动砍伐周围 4/8/12 格（按等级）内的树木：
 * 一级只砍原木；二级起清理树叶；三级扩大范围。
 * 玩家建造的原木（无树叶、非树结构）受保护不砍；双持双斧可跳过保护判定。
 * 掉落按手持斧计算（自动冶炼附魔可出木炭），原木每块消耗 1 点耐久。
 * 滚动列扫描模式与其他挂机升级一致。
 */
public class LumberjackModifier extends GolemModifier {

	private static final int CYCLE = 100;            // 全周期目标长度：约 5 秒扫完整个范围
	private static final int RANGE_PER_LEVEL = 4;    // 每级水平半径
	private static final int Y_MIN = -1;
	private static final int Y_MAX = 25;

	private static final Map<UUID, Integer> COLUMN_CURSORS = new WeakHashMap<>();

	public LumberjackModifier() {
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
		// 安全阀：战斗停伐
		if (golem.getTarget() != null) return;
		// 工具门槛：主手必须持斧
		if (!(golem.getMainHandItem().getItem() instanceof AxeItem)) return;
		if (!(golem.level() instanceof ServerLevel sl)) return;

		int range = level * RANGE_PER_LEVEL;
		int side = range * 2 + 1;
		int totalColumns = side * side;
		int columnsPerTick = Math.max(1, (totalColumns + CYCLE - 1) / CYCLE);

		Level levelW = golem.level();
		BlockPos center = golem.blockPosition();

		UUID id = golem.getUUID();
		int cursor = COLUMN_CURSORS.getOrDefault(id, 0);
		for (int i = 0; i < columnsPerTick; i++) {
			scanColumn(golem, sl, levelW, center, range, level, (cursor + i) % totalColumns);
		}
		COLUMN_CURSORS.put(id, (cursor + columnsPerTick) % totalColumns);
	}

	private void scanColumn(AbstractGolemEntity<?, ?> golem, ServerLevel sl, Level level,
			BlockPos center, int range, int modifierLevel, int col) {
		int side = range * 2 + 1;
		int dx = col % side - range;
		int dz = col / side - range;
		for (int dy = Y_MIN; dy <= Y_MAX; dy++) {
			BlockPos pos = center.offset(dx, dy, dz);
			if (!level.isLoaded(pos)) continue;
			BlockState state = level.getBlockState(pos);
			if (state.isAir()) continue;
			ItemStack tool = golem.getMainHandItem();
			if (!(tool.getItem() instanceof AxeItem)) return; // 斧子脱手，本列停工
			if (!(state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES))) continue;

			boolean dualWield = golem.getOffhandItem().getItem() instanceof AxeItem;
			if (state.is(BlockTags.LOGS) && !isTreeLog(level, pos, dualWield)) {
				continue; // 玩家建造的非树原木受保护
			}
			if (state.is(BlockTags.LEAVES) && !dualWield) {
				if (modifierLevel < 2) continue; // 一级不清理树叶
				boolean adjProtected = false;
				for (Direction dir : Direction.values()) {
					BlockPos n = pos.relative(dir);
					if (level.getBlockState(n).is(BlockTags.LOGS) && !isTreeLog(level, n, false)) {
						adjProtected = true;
						break;
					}
				}
				if (adjProtected) continue;
			}

			// 掉落 + 自动冶炼（原木→木炭），原木每块消耗 1 点耐久
			List<ItemStack> drops = Block.getDrops(state, sl, pos, level.getBlockEntity(pos), golem, tool);
			L2Compat.tryAutoSmelt(sl, tool, drops);
			for (ItemStack drop : drops) {
				Block.popResource(level, pos, drop);
			}
			level.levelEvent(2001, pos, Block.getId(state));
			level.removeBlock(pos, false);
			if (state.is(BlockTags.LOGS)) {
				tool.hurtAndBreak(1, golem, e -> e.broadcastBreakEvent(InteractionHand.MAIN_HAND));
			}
		}
	}

	/**
	 * 树结构判定（与收获作物原逻辑一致）：连接的原木 BFS，需含树叶且横向扩展不超过 10 格。
	 */
	private static boolean isTreeLog(Level level, BlockPos pos, boolean dualWield) {
		if (dualWield) return true; // 双持双斧跳过所有保护判定
		Set<BlockPos> visited = new HashSet<>();
		Queue<BlockPos> queue = new ArrayDeque<>();
		queue.add(pos);
		visited.add(pos);
		int horizontalCount = 0;
		boolean hasLeaves = false;
		while (!queue.isEmpty()) {
			BlockPos current = queue.poll();
			for (Direction dir : Direction.values()) {
				BlockPos neighbor = current.relative(dir);
				if (visited.contains(neighbor)) continue;
				if (!level.isLoaded(neighbor)) continue;
				BlockState state = level.getBlockState(neighbor);
				if (state.is(BlockTags.LEAVES)) hasLeaves = true;
				if (state.is(BlockTags.LOGS) && visited.size() < 64) {
					visited.add(neighbor);
					queue.add(neighbor);
					if (dir.getAxis() != Direction.Axis.Y) horizontalCount++;
				}
			}
		}
		return hasLeaves && horizontalCount <= 10;
	}
}
