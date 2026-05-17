package src.toi_et_moi.mgdp.init;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class MGDPKeyMappings {

    public static final KeyMapping FLIGHT_DESCEND = new KeyMapping(
            "key.mgdp.flight_descend",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            "key.categories.mgdp"
    );

    public static final KeyMapping FLIGHT_SPRINT = new KeyMapping(
            "key.mgdp.flight_sprint",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_CONTROL,
            "key.categories.mgdp"
    );
}
