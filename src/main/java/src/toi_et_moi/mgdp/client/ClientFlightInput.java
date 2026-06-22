package src.toi_et_moi.mgdp.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import src.toi_et_moi.mgdp.init.MGDPKeyMappings;

@OnlyIn(Dist.CLIENT)
public class ClientFlightInput {

	public static float getVerticalInput() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.options.keyJump.isDown()) return 1.0F;
		if (MGDPKeyMappings.FLIGHT_DESCEND.isDown()) return -1.0F;
		return 0;
	}

	public static boolean isSprinting() {
		return MGDPKeyMappings.FLIGHT_SPRINT.isDown();
	}
}
