package src.toi_et_moi.mgdp.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import src.toi_et_moi.mgdp.Mgdp;
import src.toi_et_moi.mgdp.jukebox.JukeboxPacket;
import src.toi_et_moi.mgdp.modifier.SwapPacket;

public class MGDPNetwork {

    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Mgdp.MODID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private static int id = 0;

    public static SimpleChannel register() {
        CHANNEL.registerMessage(id++, JukeboxPacket.class,
                JukeboxPacket::encode, JukeboxPacket::decode, JukeboxPacket::handle);
        CHANNEL.registerMessage(id++, SwapPacket.class,
                SwapPacket::encode, SwapPacket::decode, SwapPacket::handle);
        return CHANNEL;
    }
}
