package src.toi_et_moi.mgdp.modifier.hostility;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

import java.util.List;

public class RepellingModifier extends GolemModifier {

	private static final double RANGE = 10.0;
	private static final double STRENGTH = 0.15;

	public RepellingModifier() {
		super(StatFilterType.HEALTH, 1);
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;

		List<LivingEntity> targets = golem.level().getEntitiesOfClass(LivingEntity.class,
				golem.getBoundingBox().inflate(RANGE),
				e -> e.isAlive() && isValidTarget(golem, e));

		for (LivingEntity target : targets) {
			double dist = target.distanceTo(golem);
			if (dist > RANGE) continue;
			double factor = (1 - dist / RANGE) * STRENGTH;
			Vec3 dir = target.position().subtract(golem.position()).normalize();
			target.push(dir.x * factor, dir.y * factor, dir.z * factor);
			target.hurtMarked = true;
		}
	}

	// Projectile immunity (like the original Repelling trait)
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
