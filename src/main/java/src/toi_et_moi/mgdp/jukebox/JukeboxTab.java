package src.toi_et_moi.mgdp.jukebox;

import dev.xkmc.modulargolems.content.menu.registry.EquipmentGroup;
import dev.xkmc.modulargolems.content.menu.tabs.GolemTabBase;
import dev.xkmc.modulargolems.content.menu.tabs.GolemTabManager;
import dev.xkmc.modulargolems.content.menu.tabs.GolemTabToken;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import src.toi_et_moi.mgdp.Mgdp;

public class JukeboxTab extends GolemTabBase<EquipmentGroup, JukeboxTab> {

    public JukeboxTab(int index, GolemTabToken<EquipmentGroup, JukeboxTab> token,
                      GolemTabManager<EquipmentGroup> manager, ItemStack stack, Component title) {
        super(index, token, manager, stack, title);
    }

    @Override
    public void onTabClicked() {
        int id = manager.token.golem.getId();
        Mgdp.PACKET_HANDLER.sendToServer(new JukeboxPacket(
                JukeboxPacket.Action.OPEN_MENU, id));
    }
}
