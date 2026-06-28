package src.toi_et_moi.mgdp.modifier.common;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import src.toi_et_moi.mgdp.Mgdp;

@Mod.EventBusSubscriber(modid = Mgdp.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EnvHazardHandler {

	private static final String TAG_X = "mgdp_hazard_x";
	private static final String TAG_Y = "mgdp_hazard_y";
	private static final String TAG_Z = "mgdp_hazard_z";
	private static final String TAG_FLEE_X = "mgdp_flee_x";
	private static final String TAG_FLEE_Y = "mgdp_flee_y";
	private static final String TAG_FLEE_Z = "mgdp_flee_z";
	private static final String TAG_TIME = "mgdp_hazard_time";
	private static final String TAG_MEMORY = "mgdp_hazard_memory";
	private static final int COOLDOWN = 60;
	private static final int MEMORY_TICKS = 200; // remember hazard for 10 seconds
	private static final int SEARCH_RADIUS = 8;
	private static final int MIN_ESCAPE_DIST = 4;

	@SubscribeEvent
	public static void onGolemHurt(LivingHurtEvent event) {
		if (!(event.getEntity() instanceof AbstractGolemEntity<?, ?> golem)) return;
		if (golem.level().isClientSide()) return;
		Entity direct = event.getSource().getDirectEntity();
		if (direct != null) return;
		if (event.getSource().is(DamageTypes.FALL)) return;
		if (event.getSource().is(DamageTypes.STARVE)) return;
		if (event.getSource().is(DamageTypes.FLY_INTO_WALL)) return;
		if (event.getSource().is(DamageTypes.MAGIC)) return;
		if (event.getSource().is(DamageTypes.WITHER)) return;
		if (event.getSource().is(DamageTypes.THORNS)) return;

		if (golem.getTarget() != null && event.getAmount() < golem.getMaxHealth() * 0.05f) return;

		BlockPos pos = golem.blockPosition();
		var data = golem.getPersistentData();

		if (data.contains(TAG_TIME) && golem.tickCount - data.getInt(TAG_TIME) < 20) return;

		data.putInt(TAG_X, pos.getX());
		data.putInt(TAG_Y, pos.getY());
		data.putInt(TAG_Z, pos.getZ());
		data.putInt(TAG_TIME, golem.tickCount);
	}

	@SubscribeEvent
	public static void onGolemTick(LivingEvent.LivingTickEvent event) {
		if (!(event.getEntity() instanceof AbstractGolemEntity<?, ?> golem)) return;
		if (golem.level().isClientSide()) return;

		var data = golem.getPersistentData();
		// Check active flee
		if (data.contains(TAG_TIME)) {
			tickFlee(golem, data);
		}
		// Check memory-based avoidance
		if (data.contains(TAG_MEMORY)) {
			tickMemory(golem, data);
		}
	}

	private static void tickFlee(AbstractGolemEntity<?, ?> golem, net.minecraft.nbt.CompoundTag data) {
		int hazardTime = data.getInt(TAG_TIME);
		if (golem.tickCount - hazardTime > COOLDOWN) {
			// Flee period over → move into memory for continued avoidance
			data.putInt(TAG_MEMORY, golem.tickCount);
			clearTags(data, TAG_X, TAG_Y, TAG_Z, TAG_FLEE_X, TAG_FLEE_Y, TAG_FLEE_Z, TAG_TIME);
			return;
		}

		BlockPos from = new BlockPos(data.getInt(TAG_X), data.getInt(TAG_Y), data.getInt(TAG_Z));
		if (golem.blockPosition().distSqr(from) > MIN_ESCAPE_DIST * MIN_ESCAPE_DIST) {
			// Escaped → move to memory
			data.putInt(TAG_MEMORY, golem.tickCount);
			clearTags(data, TAG_X, TAG_Y, TAG_Z, TAG_FLEE_X, TAG_FLEE_Y, TAG_FLEE_Z, TAG_TIME);
			return;
		}

		if (!data.contains(TAG_FLEE_X)) {
			BlockPos safe = findSafeSpot(golem, from);
			if (safe != null) {
				data.putInt(TAG_FLEE_X, safe.getX());
				data.putInt(TAG_FLEE_Y, safe.getY());
				data.putInt(TAG_FLEE_Z, safe.getZ());
			}
		}

		if (data.contains(TAG_FLEE_X)) {
			// Push toward owner when possible, fallback away from hazard
			LivingEntity owner = golem.getOwner();
			double dx, dz;
			if (owner != null) {
				dx = owner.getX() - golem.getX();
				dz = owner.getZ() - golem.getZ();
			} else {
				dx = golem.getX() - (from.getX() + 0.5);
				dz = golem.getZ() - (from.getZ() + 0.5);
			}
			double len = Math.sqrt(dx * dx + dz * dz);
			if (len > 0.1) {
				double cross = golem.getRandom().nextBoolean() ? 0.3 : -0.3;
				Vec3 push = new Vec3((dx + cross * dz) / len * 0.2, 0.1, (dz - cross * dx) / len * 0.2);
				Vec3 vel = golem.getDeltaMovement();
				if (Math.abs(vel.x + push.x) < 100 && Math.abs(vel.z + push.z) < 100) {
					golem.setDeltaMovement(vel.add(push));
					golem.hasImpulse = true;
				}
			}
		}
	}

	private static void tickMemory(AbstractGolemEntity<?, ?> golem, net.minecraft.nbt.CompoundTag data) {
		int memoryEnd = data.getInt(TAG_MEMORY);
		if (golem.tickCount - memoryEnd > MEMORY_TICKS) {
			data.remove(TAG_MEMORY);
			return;
		}

		BlockPos hazardPos = new BlockPos(data.getInt(TAG_X), data.getInt(TAG_Y), data.getInt(TAG_Z));
		BlockPos safe = findSafeSpot(golem, hazardPos);
		if (safe == null) return;

		// If still very close, also add a velocity push to get unstuck fast
		if (golem.blockPosition().distSqr(hazardPos) < 9.0) {
			LivingEntity owner = golem.getOwner();
			double dx, dz;
			if (owner != null) {
				dx = owner.getX() - golem.getX();
				dz = owner.getZ() - golem.getZ();
			} else {
				dx = golem.getX() - (hazardPos.getX() + 0.5);
				dz = golem.getZ() - (hazardPos.getZ() + 0.5);
			}
			double len = Math.sqrt(dx * dx + dz * dz);
			if (len > 0.1) {
				Vec3 push = new Vec3(dx / len * 0.2, 0.1, dz / len * 0.2);
				Vec3 vel = golem.getDeltaMovement();
				if (Math.abs(vel.x + push.x) < 100 && Math.abs(vel.z + push.z) < 100) {
					golem.setDeltaMovement(vel.add(push));
					golem.hasImpulse = true;
				}
			}
		}
	}

	private static void clearTags(net.minecraft.nbt.CompoundTag data, String... tags) {
		for (String t : tags) data.remove(t);
	}

	private static BlockPos findSafeSpot(AbstractGolemEntity<?, ?> golem, BlockPos from) {
		LivingEntity target = golem.getTarget();
		LivingEntity owner = golem.getOwner();

		BlockPos.MutableBlockPos best = null;
		double bestScore = -1;

		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int r = MIN_ESCAPE_DIST; r <= SEARCH_RADIUS; r++) {
			for (int x = -r; x <= r; x++) {
				for (int z = -r; z <= r; z++) {
					if (Math.abs(x) != r && Math.abs(z) != r) continue;

					pos.set(from.getX() + x, from.getY(), from.getZ() + z);
					for (int dy = 0; dy <= 1; dy++) {
						pos.setY(from.getY() + dy);
						if (!isSafe(golem, pos)) continue;

						double score = pos.distSqr(from);
						// Run toward owner, away from target
						if (target != null) {
							score += target.blockPosition().distSqr(pos) * 0.3;
						}
						if (owner != null) {
							score -= owner.blockPosition().distSqr(pos) * 0.3;
						}
						if (score > bestScore) {
							bestScore = score;
							if (best == null) best = new BlockPos.MutableBlockPos();
							best.set(pos);
						}
					}
				}
			}
		}
		return best != null ? best.immutable() : null;
	}

	private static boolean isSafe(AbstractGolemEntity<?, ?> golem, BlockPos.MutableBlockPos pos) {
		BlockState ground = golem.level().getBlockState(pos.below());
		BlockState standing = golem.level().getBlockState(pos);
		if (!ground.isSolid()) return false;
		if (standing.isSolid()) return false;
		if (!standing.isPathfindable(golem.level(), pos, PathComputationType.LAND)) return false;
		if (golem.level().isWaterAt(pos)) return false;
		if (golem.level().getBlockState(pos).is(net.minecraft.tags.BlockTags.FIRE)) return false;
		if (golem.level().getBlockState(pos.below()).is(net.minecraft.tags.BlockTags.FIRE)) return false;
		return true;
	}
}
