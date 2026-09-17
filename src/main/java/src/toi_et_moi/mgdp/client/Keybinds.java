package src.toi_et_moi.mgdp.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import src.toi_et_moi.mgdp.Mgdp;
import src.toi_et_moi.mgdp.network.GolemRecallPacket;
import src.toi_et_moi.mgdp.network.LunaClonePacket;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_F;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_L;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_T;

@Mod.EventBusSubscriber(modid = Mgdp.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Keybinds {

	public static final KeyMapping RECALL_GOLEMS = new KeyMapping(
			"key.mgdp.recall",
			KeyConflictContext.IN_GAME,
			InputConstants.Type.KEYSYM,
			GLFW_KEY_F,
			"key.categories.mgdp"
	);

	public static final KeyMapping LUNA_CLONE = new KeyMapping(
			"key.mgdp.luna",
			KeyConflictContext.IN_GAME,
			KeyModifier.ALT,
			InputConstants.Type.KEYSYM,
			GLFW_KEY_L,
			"key.categories.mgdp"
	);

	@SubscribeEvent
	public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
		event.register(RECALL_GOLEMS);
		event.register(LUNA_CLONE);
	}

	@Mod.EventBusSubscriber(modid = Mgdp.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
	public static class KeyHandler {

		@SubscribeEvent
		public static void onKeyInput(InputEvent.Key event) {
			if (RECALL_GOLEMS.consumeClick()) {
				Mgdp.PACKET_HANDLER.sendToServer(new GolemRecallPacket());
			}
			if (LUNA_CLONE.consumeClick()) {
				Mgdp.PACKET_HANDLER.sendToServer(new LunaClonePacket());
			}
		}
	}
}
