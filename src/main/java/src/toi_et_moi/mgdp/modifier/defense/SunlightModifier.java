package src.toi_et_moi.mgdp.modifier.defense;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class SunlightModifier extends GolemModifier {

	public SunlightModifier() {
		super(StatFilterType.HEALTH, 1);
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;

		BlockPos pos = golem.blockPosition();
		int light = golem.level().getBrightness(LightLayer.BLOCK, pos)
				+ golem.level().getBrightness(LightLayer.SKY, pos);

		if (light >= 8) {
			golem.heal(0.1f);
		}
	}

	@Override
	public void onHurtTarget(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		if (golem.level().isClientSide()) return;

		BlockPos pos = golem.blockPosition();
		int light = golem.level().getBrightness(LightLayer.BLOCK, pos)
				+ golem.level().getBrightness(LightLayer.SKY, pos);

		if (light < 8) {
			event.setAmount(event.getAmount() * 1.25f);
		}
	}
}
