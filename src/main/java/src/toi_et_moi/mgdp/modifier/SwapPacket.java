package src.toi_et_moi.mgdp.modifier;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import src.toi_et_moi.mgdp.Mgdp;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

import java.util.List;
import java.util.function.Supplier;

public class SwapPacket {

    public SwapPacket() {
    }

    public static void encode(SwapPacket packet, FriendlyByteBuf buf) {
    }

    public static SwapPacket decode(FriendlyByteBuf buf) {
        return new SwapPacket();
    }

    public static void handle(SwapPacket packet, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            SwapModifier.trySwapByPlayer(player);
        });
        context.setPacketHandled(true);
    }
}
