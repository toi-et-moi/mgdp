package src.toi_et_moi.mgdp.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 拾荒箱头顶状态数据包（服务端 -> 客户端）：
 * 服务端每秒推送一次当前状态，客户端在傀儡头顶用公告板文字渲染。
 * 不占用 customName，与玩家自定义名称互不干扰（参照歌词渲染）。
 */
public class ScavStatusPacket {

	public static final byte STATE_NONE = 0;
	public static final byte STATE_COUNTING = 1;
	public static final byte STATE_COMBAT = 2;
	public static final byte STATE_NO_MODE = 3;

	/** 客户端侧缓存：实体 id -> [状态, 剩余秒, 收到时的游戏时间] */
	public static final Map<Integer, long[]> CLIENT_STATUS = new HashMap<>();

	public int entityId;
	public byte state;
	public int seconds;

	public ScavStatusPacket() {
	}

	public ScavStatusPacket(int entityId, byte state, int seconds) {
		this.entityId = entityId;
		this.state = state;
		this.seconds = seconds;
	}

	public static void encode(ScavStatusPacket packet, FriendlyByteBuf buf) {
		buf.writeInt(packet.entityId);
		buf.writeByte(packet.state);
		buf.writeInt(packet.seconds);
	}

	public static ScavStatusPacket decode(FriendlyByteBuf buf) {
		return new ScavStatusPacket(buf.readInt(), buf.readByte(), buf.readInt());
	}

	public static void handle(ScavStatusPacket packet, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			if (ctx.get().getDirection().getReceptionSide().isClient()) {
				var level = Minecraft.getInstance().level;
				if (level == null) return;
				CLIENT_STATUS.put(packet.entityId,
						new long[]{packet.state, packet.seconds, level.getGameTime()});
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
