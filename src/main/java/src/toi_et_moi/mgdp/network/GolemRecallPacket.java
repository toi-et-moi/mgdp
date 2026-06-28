package src.toi_et_moi.mgdp.network;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.entity.mode.GolemModes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class GolemRecallPacket {

	public GolemRecallPacket() {
	}

	public static void encode(GolemRecallPacket packet, FriendlyByteBuf buf) {
	}

	public static GolemRecallPacket decode(FriendlyByteBuf buf) {
		return new GolemRecallPacket();
	}

	public static void handle(GolemRecallPacket packet, Supplier<NetworkEvent.Context> ctx) {
		NetworkEvent.Context context = ctx.get();
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player == null) return;

			Vec3 pos = player.position();
			for (var golem : player.level().getEntitiesOfClass(AbstractGolemEntity.class,
					player.getBoundingBox().inflate(128),
					g -> g.isAlive() && g.getMode() == GolemModes.FOLLOW && player.getUUID().equals(g.getOwnerUUID()))) {
				golem.teleportTo(pos.x, pos.y, pos.z);
				golem.getNavigation().stop();
			}
		});
		context.setPacketHandled(true);
	}
}
