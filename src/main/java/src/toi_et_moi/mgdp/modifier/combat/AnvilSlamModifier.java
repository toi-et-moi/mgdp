package src.toi_et_moi.mgdp.modifier.combat;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class AnvilSlamModifier extends GolemModifier {

	private static final String TAG_JUMPING = "mgdp_anvil_jumping";
	private static final String TAG_ANVIL_ID = "mgdp_anvil_id";
	private static final String TAG_ANVIL_TICK = "mgdp_anvil_tick";
	private static final String TAG_ANVIL_X = "mgdp_anvil_x";
	private static final String TAG_ANVIL_Z = "mgdp_anvil_z";
	private static final String TAG_COOLDOWN = "mgdp_anvil_cd";

	private static final int MAX_ANVIL_TICKS = 100; // 5 second timeout

	public AnvilSlamModifier() {
		super(StatFilterType.ATTACK, 1);
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		var data = golem.getPersistentData();
		boolean jumping = data.getBoolean(TAG_JUMPING);

		if (jumping) {
			int anvilId = data.getInt(TAG_ANVIL_ID);

			// Spawn anvil at jump peak
			if (anvilId == 0 && golem.getDeltaMovement().y < 0) {
				spawnAnvil(golem);
				data.putInt(TAG_ANVIL_TICK, golem.tickCount);
			}

			// Safety: timeout — anvil has been around too long
			if (anvilId != 0 && golem.tickCount - data.getInt(TAG_ANVIL_TICK) > MAX_ANVIL_TICKS) {
				forceStop(golem);
				return;
			}

			// Landing on ground
			if (golem.onGround()) {
				clearAnvilBlock(golem);
				doSlam(golem, level);
				forceStop(golem);
			}
			// Hitting water — slam immediately
			if (golem.isInWater()) {
				doSlam(golem, level);
				clearAnvilBlock(golem);
				forceStop(golem);
			}
			return;
		}

		LivingEntity target = golem.getTarget();
		if (target == null) return;
		if (!golem.onGround()) return;

		long lastJump = data.getLong(TAG_COOLDOWN);
		if (golem.level().getGameTime() - lastJump < 40) return;

		double dx = target.getX() - golem.getX();
		double dz = target.getZ() - golem.getZ();
		double dist = Math.sqrt(dx * dx + dz * dz);
		if (dist < 3 || dist > 20) return;

		double hFactor = 0.15;
		double vy = 1.0 + 0.2 * level;
		golem.setDeltaMovement(dx * hFactor, vy, dz * hFactor);
		golem.hasImpulse = true;
		data.putBoolean(TAG_JUMPING, true);
	}

	private static void spawnAnvil(AbstractGolemEntity<?, ?> golem) {
		Level levelWorld = golem.level();
		BlockPos pos = golem.blockPosition().above(4);
		FallingBlockEntity anvil = FallingBlockEntity.fall(levelWorld,
				pos, Blocks.ANVIL.defaultBlockState());
		anvil.setHurtsEntities(0, 0);
		anvil.dropItem = false;
		levelWorld.addFreshEntity(anvil);
		golem.startRiding(anvil);
		var data = golem.getPersistentData();
		data.putInt(TAG_ANVIL_ID, anvil.getId());
		data.putInt(TAG_ANVIL_X, pos.getX());
		data.putInt(TAG_ANVIL_Z, pos.getZ());
	}

	private static void clearAnvilBlock(AbstractGolemEntity<?, ?> golem) {
		var data = golem.getPersistentData();
		int ax = data.getInt(TAG_ANVIL_X);
		int az = data.getInt(TAG_ANVIL_Z);
		if (ax == 0 && az == 0) return;
		for (int y = golem.blockPosition().getY() + 2; y > golem.blockPosition().getY() - 3; y--) {
			BlockPos p = new BlockPos(ax, y, az);
			var state = golem.level().getBlockState(p);
			if (state.is(Blocks.ANVIL) || state.is(Blocks.CHIPPED_ANVIL) || state.is(Blocks.DAMAGED_ANVIL)) {
				golem.level().destroyBlock(p, false);
				break;
			}
		}
	}

	private static void forceStop(AbstractGolemEntity<?, ?> golem) {
		var data = golem.getPersistentData();
		int anvilId = data.getInt(TAG_ANVIL_ID);
		if (anvilId != 0) {
			Entity anvil = golem.level().getEntity(anvilId);
			if (anvil != null) anvil.discard();
		}
		data.putLong(TAG_COOLDOWN, golem.level().getGameTime());
		data.remove(TAG_JUMPING);
		data.remove(TAG_ANVIL_ID);
		data.remove(TAG_ANVIL_TICK);
		data.remove(TAG_ANVIL_X);
		data.remove(TAG_ANVIL_Z);
	}

	private static void doSlam(AbstractGolemEntity<?, ?> golem, int level) {
		int range = 6;
		float damage = (float) golem.getAttributeValue(
				net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
		Level levelWorld = golem.level();

		AABB area = golem.getBoundingBox().inflate(range, 1, range);
		for (LivingEntity target : levelWorld.getEntitiesOfClass(LivingEntity.class, area,
				e -> e.isAlive() && golem.predicateTarget(e))) {
			target.hurt(levelWorld.damageSources().anvil(golem), damage);
			target.push(0, 0.5, 0);
		}

		BlockPos pos = golem.blockPosition();
		for (int i = 0; i < 30; i++) {
			double px = pos.getX() + (golem.getRandom().nextDouble() - 0.5) * range;
			double pz = pos.getZ() + (golem.getRandom().nextDouble() - 0.5) * range;
			levelWorld.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ANVIL.defaultBlockState()),
					px, pos.getY(), pz, 0, 0.4, 0);
		}
		levelWorld.playSound(null, pos, SoundEvents.ANVIL_PLACE,
				net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
	}

	@Override
	public List<MutableComponent> getDetail(int v) {
		return List.of(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GREEN));
	}
}
