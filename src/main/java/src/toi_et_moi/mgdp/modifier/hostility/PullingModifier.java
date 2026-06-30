package src.toi_et_moi.mgdp.modifier.hostility;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;

public class PullingModifier extends GolemModifier {

	public PullingModifier() {
		super(StatFilterType.HEALTH, 3);
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;

		double range = 10.0 + level * 5.0;
		double strength = 0.15 + (level - 1) * 0.15;

		List<LivingEntity> targets = golem.level().getEntitiesOfClass(LivingEntity.class,
				golem.getBoundingBox().inflate(range),
				e -> e.isAlive() && isValidTarget(golem, e));

		for (LivingEntity target : targets) {
			double dist = target.distanceTo(golem);
			if (dist > range) continue;
			double factor = (1 - dist / range) * (dist / range) * 4 * strength;
			Vec3 dir = golem.position().subtract(target.position()).normalize();
			target.push(dir.x * factor, dir.y * factor, dir.z * factor);
			target.hurtMarked = true;

			// Level 2+: slow on pull
			if (level >= 2) {
				target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, level - 2, false, false));
			}
		}
	}

	@Override
	public void onHurtTarget(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		// Level 3: pulled targets take extra fall-style damage
		if (level >= 3) {
			LivingEntity target = event.getEntity();
			if (target.hurtTime <= 0) {
				event.setAmount(event.getAmount() * 1.15f);
			}
		}
	}

	private static boolean isValidTarget(AbstractGolemEntity<?, ?> golem, LivingEntity e) {
		if (e == golem) return false;
		if (e == golem.getOwner()) return false;
		if (e instanceof Player p && (p.isCreative() || p.isSpectator())) return false;
		return !golem.isAlliedTo(e);
	}
}
