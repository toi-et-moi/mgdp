package src.toi_et_moi.mgdp.modifier.combat;

import dev.xkmc.l2damagetracker.contents.attack.CreateSourceEvent;
import dev.xkmc.l2damagetracker.contents.damage.DefaultDamageState;
import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class EndVoidModifier extends GolemModifier {

	public EndVoidModifier() {
		super(StatFilterType.ATTACK, 1);
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		if (golem.level().dimension() != Level.END) return;
		if (golem.tickCount % 20 != 0) return;

		AABB area = golem.getBoundingBox().inflate(32);
		if (golem.getHealth() >= golem.getMaxHealth() && golem.getReforgeCount() == 0) return;

		for (EndCrystal crystal : golem.level().getEntitiesOfClass(EndCrystal.class, area, EndCrystal::isAlive)) {
			if (golem.distanceToSqr(crystal) < 32 * 32) {
				golem.repairWithItem();
				break;
			}
		}
	}

	@Override
	public void modifySource(AbstractGolemEntity<?, ?> golem, CreateSourceEvent event, int level) {
		if (golem.level().isClientSide()) return;
		if (golem.level().dimension() != Level.END) return;

		// Apply void-like damage tags: bypass armor, protection, etc.
		var result = event.getResult();
		if (result == null) return;
		if (result.validState(DefaultDamageState.BYPASS_ARMOR))
			event.enable(DefaultDamageState.BYPASS_ARMOR);
		if (result.validState(DefaultDamageState.BYPASS_MAGIC))
			event.enable(DefaultDamageState.BYPASS_MAGIC);
	}
}
