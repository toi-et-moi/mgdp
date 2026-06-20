package src.toi_et_moi.mgdp.modifier.combat;

import dev.xkmc.modulargolems.content.modifier.base.AttributeGolemModifier;
import dev.xkmc.modulargolems.init.registrate.GolemTypes;

public class IronUpgradeModifier extends AttributeGolemModifier {

	public IronUpgradeModifier() {
		super(2,
			new AttrEntry(() -> GolemTypes.STAT_ARMOR.get(), () -> 5.0),
			new AttrEntry(() -> GolemTypes.STAT_ATTACK.get(), () -> 2.0),
			new AttrEntry(() -> GolemTypes.STAT_RANGE.get(), () -> 0.5),
			new AttrEntry(() -> GolemTypes.STAT_SWEEP.get(), () -> 0.5)
		);
	}
}
