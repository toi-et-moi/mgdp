package src.toi_et_moi.mgdp.init;

import dev.xkmc.modulargolems.content.item.upgrade.SimpleUpgradeItem;
import dev.xkmc.modulargolems.init.registrate.GolemItems;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;
import src.toi_et_moi.mgdp.Mgdp;

public class MGDPItems {

	public static final RegistryObject<SimpleUpgradeItem> HARVEST_CROP;
	public static final RegistryObject<SimpleUpgradeItem> FLIGHT;
	public static final RegistryObject<SimpleUpgradeItem> POTION_AURA;

	static {
		HARVEST_CROP = Mgdp.ITEMS.register("harvest_crop",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.HARVEST_CROP.get(), 1, false));

		FLIGHT = Mgdp.ITEMS.register("flight",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.FLIGHT.get(), 1, true));

		POTION_AURA = Mgdp.ITEMS.register("potion_aura",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.POTION_AURA.get(), 1, true));
	}

	public static void register() {
	}

	@Mod.EventBusSubscriber(modid = Mgdp.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class EventHandler {

		@SubscribeEvent
		public static void addCreative(BuildCreativeModeTabContentsEvent event) {
			if (event.getTabKey() == GolemItems.UPGRADES.getKey()) {
				event.accept(HARVEST_CROP.get());
				event.accept(FLIGHT.get());
				event.accept(POTION_AURA.get());
			}
		}
	}
}
