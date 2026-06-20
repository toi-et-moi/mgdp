package src.toi_et_moi.mgdp.modifier.defense;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import src.toi_et_moi.mgdp.init.IFlipData;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

public class BackstepModifier extends GolemModifier {

	private static final String TAG_FLIP_TICK = "mgdp_bs_flip_tick";

	public BackstepModifier() {
		super(StatFilterType.HEALTH, 1);
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide) return;

		// Time trigger: every 3 seconds, backstep when locked on a target
		if (golem.tickCount % 60 == 0 && golem.getTarget() != null) {
			Vec3 look = golem.getLookAngle();
			golem.setDeltaMovement(-look.x * 2.4, 1.0, -look.z * 2.4);
			golem.hasImpulse = true;
			triggerFlip(golem);
		}

		// Distance trigger: every 10 ticks, backstep when target is too close
		if (golem.tickCount % 10 == 0) {
			LivingEntity target = golem.getTarget();
			if (target != null && golem.distanceToSqr(target) <= 9.0) {
				Vec3 away = golem.position().subtract(target.position()).normalize();
				golem.setDeltaMovement(away.x * 1.2, 0.5, away.z * 1.2);
				golem.hasImpulse = true;
				triggerFlip(golem);
			}
		}

		// Flip animation tick (every tick)
		var data = golem.getPersistentData();
		int tick = data.getInt(TAG_FLIP_TICK);
		if (tick <= 0) return;

		int progress = Math.min(tick * 25, 400);
		((IFlipData) golem).mgdp$setFlipProgress(progress);

		if (progress >= 400) {
			data.remove(TAG_FLIP_TICK);
		} else {
			data.putInt(TAG_FLIP_TICK, tick + 1);
		}
	}

	private void triggerFlip(AbstractGolemEntity<?, ?> golem) {
		if (golem.getModifiers().containsKey(MGDPModifiers.BACKFLIP.get())) {
			golem.getPersistentData().putInt(TAG_FLIP_TICK, 1);
		}
	}
}
