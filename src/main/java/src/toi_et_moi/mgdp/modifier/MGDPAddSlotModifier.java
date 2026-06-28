package src.toi_et_moi.mgdp.modifier;

import dev.xkmc.modulargolems.content.item.upgrade.IUpgradeItem;
import dev.xkmc.modulargolems.content.modifier.common.AddSlotModifier;
import java.util.List;

public class MGDPAddSlotModifier extends AddSlotModifier {

    private final int slots;

    public MGDPAddSlotModifier(int maxLevel, int slots) {
        super(maxLevel);
        this.slots = slots;
    }

    @Override
    public int addSlot(List<IUpgradeItem> list, int lv) {
        return slots * lv;
    }

    @Override
    public List<net.minecraft.network.chat.MutableComponent> getDetail(int v) {
        return List.of(net.minecraft.network.chat.Component.translatable(
                getDescriptionId() + ".desc", slots * v)
                .withStyle(net.minecraft.ChatFormatting.GREEN));
    }
}
