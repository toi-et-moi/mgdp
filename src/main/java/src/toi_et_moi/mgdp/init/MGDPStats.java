package src.toi_et_moi.mgdp.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import src.toi_et_moi.mgdp.Mgdp;

@Mod.EventBusSubscriber(modid = Mgdp.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MGDPStats {

	public static ResourceLocation GOLEM_KILLS;

	@SubscribeEvent
	public static void commonSetup(FMLCommonSetupEvent event) {
		GOLEM_KILLS = register("golem_kills", StatFormatter.DEFAULT);
	}

	private static ResourceLocation register(String name, StatFormatter formatter) {
		ResourceLocation id = new ResourceLocation(Mgdp.MODID, name);
		Registry.register(BuiltInRegistries.CUSTOM_STAT, id, id);
		Stats.CUSTOM.get(id);
		return id;
	}

	public static void init() {
	}
}
