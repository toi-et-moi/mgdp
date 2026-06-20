package src.toi_et_moi.mgdp.modifier.special;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;

public class WindmillModifier extends GolemModifier {


	public WindmillModifier() {
		super(StatFilterType.MASS, 1);
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide) return;
		float angle = (golem.tickCount * 36) % 360;
		((src.toi_et_moi.mgdp.init.IFlipData) golem).mgdp$setWindmill(angle);
	}
}
