package src.toi_et_moi.mgdp.init;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import src.toi_et_moi.mgdp.Mgdp;
import src.toi_et_moi.mgdp.jukebox.JukeboxMenu;

public class MgdpMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, Mgdp.MODID);

    public static final RegistryObject<MenuType<JukeboxMenu>> JUKEBOX =
            MENUS.register("jukebox", () -> IForgeMenuType.create(JukeboxMenu::fromNetwork));

    public static void register() {
    }
}
