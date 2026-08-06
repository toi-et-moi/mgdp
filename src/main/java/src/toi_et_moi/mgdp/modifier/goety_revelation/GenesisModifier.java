package src.toi_et_moi.mgdp.modifier.goety_revelation;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

/**
 * 万众归一: master title that empowers every other apostle title (checked via THE_GENESIS
 * inside each title). Independent trait: a 1.5s invulnerability window on the attack
 * layer; while invulnerable, being hit refreshes the window, heals the damage value,
 * and reflects 7x damage to the attacker.
 */
public class GenesisModifier extends GolemModifier {

	private static final int INVULN_TICKS = 30;
	private static final String TAG_INVULN = "mgdp_genesis_invuln";

	public GenesisModifier() {
		super(StatFilterType.MASS, 1);
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;

		// Invulnerability window countdown
		var data = golem.getPersistentData();
		int invuln = data.getInt(TAG_INVULN);
		if (invuln > 0) {
			data.putInt(TAG_INVULN, invuln - 1);
		}
	}

	@Override
	public void onAttacked(AbstractGolemEntity<?, ?> golem, LivingAttackEvent event, int level) {
		if (golem.level().isClientSide()) return;

		// No target -> no hit judgement (original boss trait)
		if (golem.getTarget() == null) {
			event.setCanceled(true);
			return;
		}

		var data = golem.getPersistentData();
		int invuln = data.getInt(TAG_INVULN);
		float amount = event.getAmount();

		if (invuln > 0) {
			// Invulnerable: refresh the window, heal the damage value, reflect 7x
			data.putInt(TAG_INVULN, INVULN_TICKS);
			golem.heal(amount);
			if (event.getSource().getEntity() instanceof LivingEntity attacker) {
				attacker.hurt(attacker.damageSources().thorns(golem), amount * 7.0F);
			}
			event.setCanceled(true);
		} else {
			// Window opens after taking a hit
			data.putInt(TAG_INVULN, INVULN_TICKS);
		}
	}
}
