package src.toi_et_moi.mgdp.modifier.combat;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class ExecutionerModifier extends GolemModifier {

	public ExecutionerModifier() {
		super(StatFilterType.ATTACK, 1);
	}

	@Override
	public void onHurt(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
		float golemMax = golem.getMaxHealth();
		float attackerHealth = attacker.getHealth();
		if (attackerHealth < golemMax * 0.25F) {
			event.setCanceled(true);
		} else if (attackerHealth < golemMax * 0.5F) {
			event.setAmount(event.getAmount() * 0.5F);
		}
	}

	@Override
	public void onHurtTarget(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		float targetMax = event.getEntity().getMaxHealth();
		float golemMax = golem.getMaxHealth();
		float targetHealth = event.getEntity().getHealth();
		if (targetHealth < golemMax * 0.5F) {
			event.setAmount(event.getAmount() + targetMax);
		}
	}
}
