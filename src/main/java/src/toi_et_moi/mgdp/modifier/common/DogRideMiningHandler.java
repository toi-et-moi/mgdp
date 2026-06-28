package src.toi_et_moi.mgdp.modifier.common;

import dev.xkmc.modulargolems.content.entity.dog.DogGolemEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import src.toi_et_moi.mgdp.Mgdp;

@Mod.EventBusSubscriber(modid = Mgdp.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DogRideMiningHandler {

	@SubscribeEvent
	public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
		if (event.getEntity().getVehicle() instanceof DogGolemEntity) {
			event.setNewSpeed(event.getOriginalSpeed() * 5.0f);
		}
	}
}
