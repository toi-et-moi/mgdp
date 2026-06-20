package src.toi_et_moi.mgdp.modifier.defense;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class NetherModifier extends GolemModifier {

	public NetherModifier() {
		super(StatFilterType.HEALTH, 1);
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		if (golem.level().dimension() != Level.NETHER) return;
		if (golem.tickCount % 20 != 0) return;

		// Repair once per second when on fire, in lava, or above y=128
		if ((golem.getHealth() < golem.getMaxHealth() || golem.getReforgeCount() > 0) &&
			(golem.isOnFire() || golem.isInLava() || golem.getY() >= 128)) {
			golem.repairWithItem();
		}
	}

	@Override
	public void onHurtTarget(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		if (golem.level().isClientSide()) return;
		if (golem.level().dimension() == Level.NETHER) {
			event.setAmount(event.getAmount() * 2.0f);
		}
	}
}
