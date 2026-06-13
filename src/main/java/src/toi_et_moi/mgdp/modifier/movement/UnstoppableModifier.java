package src.toi_et_moi.mgdp.modifier.movement;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.GolemFlags;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;

import java.util.function.Consumer;

public class UnstoppableModifier extends GolemModifier {

    public UnstoppableModifier() {
        super(StatFilterType.MOVEMENT, 1);
    }

    @Override
    public void onRegisterFlag(Consumer<GolemFlags> addFlag) {
        addFlag.accept(GolemFlags.FREE_MOVE);
    }
}
