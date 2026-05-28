package src.toi_et_moi.mgdp.modifier;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class FocusedDefenseModifier extends GolemModifier {

	public FocusedDefenseModifier() {
		super(StatFilterType.HEALTH, 1);
	}

	@Override
	public void onHurt(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		if (event.getSource().getEntity() == null) return;
		if (event.getSource().getEntity() != golem.getTarget()) {
			event.setCanceled(true);
		}
	}
}
