package src.toi_et_moi.mgdp.modifier;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.entity.common.GolemFlags;
import dev.xkmc.modulargolems.content.modifier.base.AttributeGolemModifier;
import dev.xkmc.modulargolems.init.data.MGConfig;
import dev.xkmc.modulargolems.init.registrate.GolemTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;
import java.util.function.Consumer;

public class EnchantedNetheriteGoldModifier extends AttributeGolemModifier {

    private static final AttrEntry REGEN_ENTRY = new AttrEntry(
            () -> GolemTypes.STAT_REGEN.get(), () -> 5.0);

    public EnchantedNetheriteGoldModifier() {
        super(1, REGEN_ENTRY);
    }

    @Override
    public void onRegisterFlag(Consumer<GolemFlags> addFlag) {
        addFlag.accept(GolemFlags.FIRE_IMMUNE);
    }

    @Override
    public void onHurt(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
        if (!event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            event.setAmount(event.getAmount() * 0.8F);
            if (event.getSource().is(DamageTypeTags.IS_EXPLOSION)) {
                double reduction = 1.0 - level * MGConfig.COMMON.explosionResistance.get();
                event.setAmount((float) Math.max(0, event.getAmount() * reduction));
            }
        }
    }

    @Override
    public List<MutableComponent> getDetail(int v) {
        return List.of(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GREEN));
    }
}
