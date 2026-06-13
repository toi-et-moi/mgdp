package src.toi_et_moi.mgdp.modifier.buff;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;

public class SelfRepairModifier extends GolemModifier {

	private static final int IDLE_THRESHOLD = 200;
	private static final String TAG_IDLE = "mgdp_selfrepair_idle";

	public SelfRepairModifier() {
		super(StatFilterType.HEALTH, 1);
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		if (golem.tickCount % 20 != 0) return;

		if (golem.getTarget() != null) {
			golem.getPersistentData().remove(TAG_IDLE);
			return;
		}

		long idleStart = golem.getPersistentData().getLong(TAG_IDLE);
		if (idleStart == 0) {
			golem.getPersistentData().putLong(TAG_IDLE, golem.tickCount);
			return;
		}

		if (golem.tickCount - idleStart < IDLE_THRESHOLD) return;

		if (golem.getHealth() < golem.getMaxHealth() || golem.getReforgeCount() > 0) {
			golem.repairWithItem();
		}
	}
}
