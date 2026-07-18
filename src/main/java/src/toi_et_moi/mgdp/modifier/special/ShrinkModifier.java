package src.toi_et_moi.mgdp.modifier.special;

import dev.xkmc.modulargolems.content.modifier.base.AttributeGolemModifier;
import dev.xkmc.modulargolems.init.registrate.GolemTypes;

public class ShrinkModifier extends AttributeGolemModifier {

	public ShrinkModifier() {
		super(4,
			new AttrEntry(() -> GolemTypes.STAT_SIZE_P.get(), () -> -0.2)
		);
	}
}
