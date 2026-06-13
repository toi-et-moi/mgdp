package src.toi_et_moi.mgdp.modifier.buff;

import dev.xkmc.modulargolems.content.entity.common.GolemFlags;
import dev.xkmc.modulargolems.content.modifier.base.AttributeGolemModifier;
import dev.xkmc.modulargolems.init.registrate.GolemTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.function.Consumer;

public class NetheriteGoldModifier extends AttributeGolemModifier {

    private static final AttrEntry REGEN_ENTRY = new AttrEntry(
            () -> GolemTypes.STAT_REGEN.get(), () -> 5.0);

    public NetheriteGoldModifier() {
        super(1, REGEN_ENTRY);
    }

    @Override
    public void onRegisterFlag(Consumer<GolemFlags> addFlag) {
        addFlag.accept(GolemFlags.FIRE_IMMUNE);
    }

    @Override
    public List<MutableComponent> getDetail(int v) {
        return List.of(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GREEN));
    }
}
