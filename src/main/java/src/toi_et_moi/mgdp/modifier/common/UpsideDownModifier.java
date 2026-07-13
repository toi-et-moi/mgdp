package src.toi_et_moi.mgdp.modifier.common;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;

public class UpsideDownModifier extends GolemModifier {

	public UpsideDownModifier() {
		super(StatFilterType.HEALTH, 1);
	}
}
