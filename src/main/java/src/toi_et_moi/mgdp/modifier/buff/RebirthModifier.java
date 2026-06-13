package src.toi_et_moi.mgdp.modifier.buff;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.GolemFlags;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;

import java.util.function.Consumer;

public class RebirthModifier extends GolemModifier {

    public RebirthModifier() {
        super(StatFilterType.HEAD, 1);
    }

    @Override
    public void onRegisterFlag(Consumer<GolemFlags> addFlag) {
        addFlag.accept(GolemFlags.RECYCLE);
        addFlag.accept(GolemFlags.REVIVE);
    }
}
