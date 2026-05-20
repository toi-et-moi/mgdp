package src.toi_et_moi.mgdp.modifier;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;

public class EnchantedDiamondAttackModifier extends GolemModifier {

    public EnchantedDiamondAttackModifier() {
        super(StatFilterType.ATTACK, 1);
    }

    @Override
    public void onHurtTarget(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
        event.setAmount(event.getAmount() * 1.5F);
    }

    @Override
    public List<MutableComponent> getDetail(int v) {
        return List.of(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GREEN));
    }
}
