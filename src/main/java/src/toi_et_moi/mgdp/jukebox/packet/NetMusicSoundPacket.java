package src.toi_et_moi.mgdp.jukebox.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class NetMusicSoundPacket {

    public enum Action { PLAY, STOP }

    private final Action action;
    private final int entityId;
    private final String url;
    private final int timeSecond;
    private final String songName;

    public NetMusicSoundPacket(Action action, int entityId, String url, int timeSecond, String songName) {
        this.action = action;
        this.entityId = entityId;
        this.url = url;
        this.timeSecond = timeSecond;
        this.songName = songName;
    }

    public static void encode(NetMusicSoundPacket p, FriendlyByteBuf b) {
        b.writeEnum(p.action);
        b.writeInt(p.entityId);
        b.writeUtf(p.url);
        b.writeInt(p.timeSecond);
        b.writeUtf(p.songName);
    }

    public static NetMusicSoundPacket decode(FriendlyByteBuf b) {
        return new NetMusicSoundPacket(
                b.readEnum(Action.class), b.readInt(), b.readUtf(), b.readInt(), b.readUtf());
    }

    public static void handle(NetMusicSoundPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                ClientHandler.handle(p);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static class ClientHandler {
        private static final Map<Integer, SoundInstance> ACTIVE = new HashMap<>();

        static void handle(NetMusicSoundPacket p) {
            Minecraft mc = Minecraft.getInstance();
            if (p.action == Action.STOP) {
                SoundInstance old = ACTIVE.remove(p.entityId);
                if (old != null) mc.getSoundManager().stop(old);
                src.toi_et_moi.mgdp.jukebox.packet.GolemNetMusicSound.onSoundStop(p.entityId);
                return;
            }
            // Stop any existing sound for this entity
            SoundInstance existing = ACTIVE.remove(p.entityId);
            if (existing != null) mc.getSoundManager().stop(existing);

            if (mc.level == null) return;
            var entity = mc.level.getEntity(p.entityId);
            if (entity == null) {
                // Entity not ready yet — retry next tick
                mc.execute(() -> tryPlay(p));
                return;
            }
            doPlay(mc, p, entity);
        }

        private static void tryPlay(NetMusicSoundPacket p) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;
            var entity = mc.level.getEntity(p.entityId);
            if (entity == null) return;
            SoundInstance existing = ACTIVE.remove(p.entityId);
            if (existing != null) mc.getSoundManager().stop(existing);
            doPlay(mc, p, entity);
        }

        private static void doPlay(Minecraft mc, NetMusicSoundPacket p, net.minecraft.world.entity.Entity entity) {
            var sound = new GolemNetMusicSound(entity, p.url, p.timeSecond, p.songName);
            ACTIVE.put(p.entityId, sound);
            mc.getSoundManager().play(sound);
        }
    }
}
