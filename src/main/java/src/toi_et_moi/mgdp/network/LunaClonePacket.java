package src.toi_et_moi.mgdp.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import src.toi_et_moi.mgdp.modifier.special.LunaModifier;

import java.util.function.Supplier;

/** 空载荷 C->S 包：请求对"准星指向的傀儡"执行重现复制（服务端自行射线校验） */
public class LunaClonePacket {

	public LunaClonePacket() {
	}

	public static void encode(LunaClonePacket packet, FriendlyByteBuf buf) {
	}

	public static LunaClonePacket decode(FriendlyByteBuf buf) {
		return new LunaClonePacket();
	}

	public static void handle(LunaClonePacket packet, Supplier<NetworkEvent.Context> ctx) {
		NetworkEvent.Context context = ctx.get();
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player == null) return;
			LunaModifier.tryCloneByPlayer(player);
		});
		context.setPacketHandled(true);
	}
}
