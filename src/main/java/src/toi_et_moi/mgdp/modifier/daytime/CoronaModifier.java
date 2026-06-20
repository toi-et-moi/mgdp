package src.toi_et_moi.mgdp.modifier.daytime;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.world.effect.MobEffectCategory;

import java.util.ArrayList;

public class CoronaModifier extends GolemModifier {

	public CoronaModifier() {
		super(StatFilterType.HEALTH, 1);
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		if (!golem.level().isDay()) return;

		// Remove all negative potion effects every 10 ticks
		if (golem.tickCount % 10 != 0) return;

		var effects = new ArrayList<>(golem.getActiveEffects());
		for (var effect : effects) {
			if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
				golem.removeEffect(effect.getEffect());
			}
		}
	}
}
