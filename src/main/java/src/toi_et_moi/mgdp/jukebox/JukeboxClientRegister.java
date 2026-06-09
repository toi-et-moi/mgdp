package src.toi_et_moi.mgdp.jukebox;

import dev.xkmc.modulargolems.content.menu.registry.EquipmentGroup;
import dev.xkmc.modulargolems.content.menu.registry.GolemTabRegistry;
import dev.xkmc.modulargolems.content.menu.tabs.GolemTabToken;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

/**
 * Client-side registration for jukebox tab.
 * Must be called during client setup.
 */
public class JukeboxClientRegister {

    public static GolemTabToken<EquipmentGroup, JukeboxTab> JUKEBOX_TAB;

    public static void register() {
        JUKEBOX_TAB = new GolemTabToken<>(
                JukeboxTab::new,
                () -> Items.JUKEBOX,
                Component.translatable("gui.mgdp.jukebox")
        );
        GolemTabRegistry.LIST_EQUIPMENT.add(JUKEBOX_TAB);
    }
}
