package src.toi_et_moi.mgdp.modifier;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.entity.targeting.TargetManager;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public class FireballModifier extends GolemModifier {

	private int attackTime;
	private int volleyCount;
	private final List<LivingEntity> volleyTargets = new ArrayList<>();

	public FireballModifier() {
		super(StatFilterType.ATTACK, 1);
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) {
			if (golem.level().random.nextBoolean()) {
				golem.level().addParticle(ParticleTypes.LARGE_SMOKE,
						golem.getRandomX(0.5), golem.getRandomY(), golem.getRandomZ(0.5),
						0, 0, 0);
			}
			return;
		}

		attackTime--;

		if (attackTime > 0) return;

		if (volleyCount == 0) {
			volleyTargets.clear();

			AABB area = golem.getBoundingBox().inflate(15.0);
			List<LivingEntity> allTargets = golem.level().getEntitiesOfClass(LivingEntity.class, area,
					e -> e.isAlive() && e != golem && golem.getSensing().hasLineOfSight(e)
							&& (e == golem.getTarget() || TargetManager.wantsToAttack(golem, e)));

			allTargets.sort((a, b) -> {
				if (a == golem.getTarget()) return -1;
				if (b == golem.getTarget()) return 1;
				return 0;
			});

			volleyTargets.addAll(allTargets.subList(0, Math.min(3, allTargets.size())));

			if (volleyTargets.isEmpty()) return;

			if (!golem.isSilent()) {
				golem.level().playSound(null, golem.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 0.3F, 1.0F);
			}
		}

		double speedBase = 0;
		boolean hasFired = false;
		for (LivingEntity target : volleyTargets) {
			if (!target.isAlive() || !golem.canAttack(target)
					|| !golem.getSensing().hasLineOfSight(target)) continue;

			double dx = target.getX() - golem.getX();
			double dy = target.getY(0.5) - golem.getY(0.5);
			double dz = target.getZ() - golem.getZ();
			if (speedBase == 0) {
				speedBase = Math.sqrt(Math.sqrt(golem.distanceToSqr(target))) * 0.5;
			}

			SmallFireball fireball = new SmallFireball(golem.level(), golem,
					dx * speedBase, dy * speedBase, dz * speedBase);
			fireball.setPos(golem.getX(), golem.getY(0.5), golem.getZ());
			fireball.getPersistentData().putBoolean("mgdp_fireball", true);
			golem.level().addFreshEntity(fireball);
			hasFired = true;
		}

		if (!hasFired) {
			volleyCount = 0;
			return;
		}

		volleyCount++;
		if (volleyCount >= 3) {
			volleyCount = 0;
			volleyTargets.clear();
			attackTime = Math.max(40, 100 - level * 20);
		} else {
			attackTime = 6;
		}
	}

	@Override
	public List<MutableComponent> getDetail(int v) {
		return List.of(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GREEN));
	}
}
