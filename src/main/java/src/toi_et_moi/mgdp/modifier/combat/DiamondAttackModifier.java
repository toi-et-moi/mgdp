package src.toi_et_moi.mgdp.modifier.combat;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public class DiamondAttackModifier extends GolemModifier {

    public DiamondAttackModifier() {
        super(StatFilterType.ATTACK, 1);
    }

    @Override
    public List<MutableComponent> getDetail(int v) {
        return List.of(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GREEN));
    }
}
