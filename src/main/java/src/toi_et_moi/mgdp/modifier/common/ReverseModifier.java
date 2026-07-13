package src.toi_et_moi.mgdp.modifier.common;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;

public class ReverseModifier extends GolemModifier {

    public ReverseModifier() {
        super(StatFilterType.HEALTH, 1);
    }
}
