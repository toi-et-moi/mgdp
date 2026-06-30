package src.toi_et_moi.mgdp.modifier.hostility;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

import java.util.List;

public class RepellingModifier extends GolemModifier {

	public RepellingModifier() {
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
			double factor = (1 - dist / range) * strength;
			Vec3 dir = target.position().subtract(golem.position()).normalize();
			target.push(dir.x * factor, dir.y * factor, dir.z * factor);
			target.hurtMarked = true;

			// Level 2+: slow on repel
			if (level >= 2) {
				target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, level - 2, false, false));
			}
		}
	}

	// Projectile immunity
	@Override
	public void onAttacked(AbstractGolemEntity<?, ?> golem, LivingAttackEvent event, int level) {
		if (golem.level().isClientSide()) return;
		if (event.getSource().is(DamageTypeTags.IS_PROJECTILE)) {
			event.setCanceled(true);
		}
	}

	private static boolean isValidTarget(AbstractGolemEntity<?, ?> golem, LivingEntity e) {
		if (e == golem) return false;
		if (e == golem.getOwner()) return false;
		if (e instanceof Player p && (p.isCreative() || p.isSpectator())) return false;
		return !golem.isAlliedTo(e);
	}
}
