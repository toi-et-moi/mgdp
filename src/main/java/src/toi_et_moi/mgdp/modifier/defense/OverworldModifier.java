package src.toi_et_moi.mgdp.modifier.defense;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class OverworldModifier extends GolemModifier {

	public OverworldModifier() {
		super(StatFilterType.HEALTH, 1);
	}

	@Override
	public double onHealTick(double heal, AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().dimension() == Level.OVERWORLD) {
			return heal * 2.0;
		}
		return heal;
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;

		var attr = golem.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
		if (attr == null) return;

		if (golem.level().dimension() == Level.OVERWORLD) {
			// Grant knockback immunity in the Overworld
			if (attr.getBaseValue() < 1.0) {
				golem.getPersistentData().putDouble("mgdp_overworld_kb", attr.getBaseValue());
				attr.setBaseValue(1.0);
			}
		} else if (golem.getPersistentData().contains("mgdp_overworld_kb")) {
			// Restore original value when leaving Overworld
			attr.setBaseValue(golem.getPersistentData().getDouble("mgdp_overworld_kb"));
			golem.getPersistentData().remove("mgdp_overworld_kb");
		}
	}

	@Override
	public void onHurt(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		if (golem.level().isClientSide()) return;
		if (golem.level().dimension() != Level.OVERWORLD) return;

		// Y ≤ 0 (underground/caves): reduce incoming damage by 30%
		if (golem.getY() <= 0) {
			event.setAmount(event.getAmount() * 0.7f);
		}
	}

	@Override
	public void onHurtTarget(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		if (golem.level().isClientSide()) return;
		if (golem.level().dimension() != Level.OVERWORLD) return;

		// Y ≤ 0 (underground/caves): double outgoing damage
		if (golem.getY() <= 0) {
			event.setAmount(event.getAmount() * 2.0f);
		}
	}
}
