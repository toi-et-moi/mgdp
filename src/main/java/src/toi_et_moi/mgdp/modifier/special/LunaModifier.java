package src.toi_et_moi.mgdp.modifier.special;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import src.toi_et_moi.mgdp.Config;
import src.toi_et_moi.mgdp.Mgdp;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 重现（luna）升级：按重现键（默认 Alt+L），复制准星指向的、属于自己的、装有本升级的傀儡。
 * 复制体除 UUID 外完整继承 NBT（材料/升级/owner/自定义名/血量/模式/配置卡/持久数据）。
 * 与拾荒箱的联动逻辑在 {@link src.toi_et_moi.mgdp.modifier.farming.ScavBoxModifier} 内。
 */
public class LunaModifier extends GolemModifier {

	/** 准星射线射程（格） */
	private static final double RANGE = 32.0;

	private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();

	public LunaModifier() {
		super(StatFilterType.MASS, 1);
	}

	@Override
	public List<MutableComponent> getDetail(int v) {
		return List.of(
				Component.translatable(getDescriptionId() + ".line1")
						.withStyle(ChatFormatting.GREEN),
				Component.translatable(getDescriptionId() + ".line2")
						.withStyle(ChatFormatting.GREEN)
		);
	}

	// ---------------- 入口（由 LunaClonePacket 调用） ----------------

	public static void tryCloneByPlayer(ServerPlayer player) {
		if (!(player.level() instanceof ServerLevel sl)) return;
		if (onCooldown(player)) return;
		AbstractGolemEntity<?, ?> target = findLookedAt(player);
		if (target == null) return;
		try {
			if (spawnClone(sl, player, target)) {
				setCooldown(player);
			}
		} catch (Exception e) {
			Mgdp.LOGGER.error("[mgdp-luna] clone failed", e);
		}
	}

	// ---------------- 准星选傀儡 ----------------

	private static AbstractGolemEntity<?, ?> findLookedAt(ServerPlayer player) {
		Vec3 eye = player.getEyePosition();
		Vec3 look = player.getViewVector(1.0F);
		Vec3 end = eye.add(look.scale(RANGE));
		// 与原版准星（GameRenderer#pick）同款：候选盒 = 玩家碰撞箱沿视线外扩 + 膨胀 1
		AABB box = player.getBoundingBox().expandTowards(look.scale(RANGE)).inflate(1.0D);
		EntityHitResult hit = ProjectileUtil.getEntityHitResult(
				player, eye, end, box,
				e -> e instanceof AbstractGolemEntity<?, ?> g
						&& g.isAlive()
						&& player.getUUID().equals(g.getOwnerUUID())
						&& g.getModifiers().containsKey(MGDPModifiers.LUNA.get()),
				RANGE * RANGE);
		return hit != null && hit.getEntity() instanceof AbstractGolemEntity<?, ?> golem ? golem : null;
	}

	// ---------------- 复制实体 ----------------

	private static boolean spawnClone(ServerLevel sl, ServerPlayer player, AbstractGolemEntity<?, ?> src) {
		CompoundTag tag = new CompoundTag();
		src.saveWithoutId(tag);
		// UUID 必须重分配（否则两具傀儡共享 UUID，追踪/召回全部错乱）；
		// 乘客与载具关系不复制，避免把骑在傀儡上的玩家/其它生物一并复制
		tag.remove("UUID");
		tag.remove("Passengers");
		tag.remove("RootVehicle");

		EntityType<?> type = src.getType();
		Entity created = type.create(sl);
		if (!(created instanceof AbstractGolemEntity<?, ?> copy)) return false;
		copy.load(tag);
		copy.setUUID(UUID.randomUUID());

		copy.setYRot(src.getYRot());
		copy.setYBodyRot(src.getYRot());
		copy.setYHeadRot(src.getYRot());
		copy.setXRot(src.getXRot());
		placeNear(sl, src, copy);

		sl.addFreshEntity(copy);
		sl.playSound(null, copy.blockPosition(), SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 0.8F, 1.0F);
		return true;
	}

	/** 偏移候选：先水平四向，再四个对角 */
	private static final double[][] OFFSETS = {
			{1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1},
			{1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1}
	};

	private static void placeNear(ServerLevel level, AbstractGolemEntity<?, ?> src, AbstractGolemEntity<?, ?> copy) {
		EntityDimensions dim = copy.getDimensions(Pose.STANDING);
		double step = Math.max(1.0D, dim.width);
		Vec3 base = src.position();
		copy.setPos(base);
		adjust(level, copy, dim);
		if (level.noCollision(copy)) return;
		for (double[] off : OFFSETS) {
			copy.setPos(base.add(off[0] * step, off[1], off[2] * step));
			adjust(level, copy, dim);
			if (level.noCollision(copy)) return;
		}
		// 全失败：与本体重叠摆放，让实体互相挤开，也绝不卡进方块
		copy.setPos(base);
	}

	/** 等价于 GolemHolder#setPos（private，故自写）：把傀儡微调到最近的空位；体积过大时跳过 */
	private static void adjust(ServerLevel level, AbstractGolemEntity<?, ?> copy, EntityDimensions dim) {
		if (dim.width * dim.width * dim.height > 64) return;
		Vec3 center = copy.position().add(0.0D, dim.height / 2.0D, 0.0D);
		double xz = dim.width - 1 + 1e-6;
		double y = dim.height - 1 + 1e-6;
		VoxelShape shape = Shapes.create(AABB.ofSize(center, xz, y, xz));
		level.findFreePosition(copy, shape, center, dim.width, dim.height, dim.width)
				.ifPresent(pos -> copy.setPos(pos.add(0.0D, -dim.height / 2.0D, 0.0D)));
	}

	// ---------------- 冷却（按玩家，仿 SwapModifier） ----------------

	private static boolean onCooldown(ServerPlayer player) {
		Long time = COOLDOWNS.get(player.getUUID());
		return time != null && time > player.level().getGameTime();
	}

	private static void setCooldown(ServerPlayer player) {
		COOLDOWNS.put(player.getUUID(), player.level().getGameTime() + Config.lunaCooldown * 20L);
	}
}
