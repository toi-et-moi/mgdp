package src.toi_et_moi.mgdp.jukebox;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import src.toi_et_moi.mgdp.Mgdp;

import java.util.function.Supplier;

public class JukeboxPacket {

    public enum Action {
        OPEN_MENU,
        TOGGLE_PLAY,
        PLAY_RECORD,
        STOP_RECORD
    }

    private Action action;
    private int entityId;
    private ResourceLocation soundId;

    public JukeboxPacket() {
    }

    public JukeboxPacket(Action action, int entityId) {
        this.action = action;
        this.entityId = entityId;
        this.soundId = null;
    }

    public JukeboxPacket(Action action, int entityId, ResourceLocation soundId) {
        this.action = action;
        this.entityId = entityId;
        this.soundId = soundId;
    }

    public static void encode(JukeboxPacket packet, FriendlyByteBuf buf) {
        buf.writeEnum(packet.action);
        buf.writeInt(packet.entityId);
        buf.writeBoolean(packet.soundId != null);
        if (packet.soundId != null) {
            buf.writeResourceLocation(packet.soundId);
        }
    }

    public static JukeboxPacket decode(FriendlyByteBuf buf) {
        JukeboxPacket packet = new JukeboxPacket();
        packet.action = buf.readEnum(Action.class);
        packet.entityId = buf.readInt();
        if (buf.readBoolean()) {
            packet.soundId = buf.readResourceLocation();
        }
        return packet;
    }

    public static void handle(JukeboxPacket packet, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                ClientHandler.handle(packet);
            } else {
                handleServer(packet, context);
            }
        });
        context.setPacketHandled(true);
    }

    // ===== Server-side handlers =====

    private static void handleServer(JukeboxPacket packet, NetworkEvent.Context ctx) {
        switch (packet.action) {
            case OPEN_MENU -> handleOpenMenu(packet, ctx);
            case TOGGLE_PLAY -> handleTogglePlay(packet, ctx);
        }
    }

    private static void handleOpenMenu(JukeboxPacket packet, NetworkEvent.Context ctx) {
        ServerPlayer player = ctx.getSender();
        if (player == null) return;
        Entity entity = player.serverLevel().getEntity(packet.entityId);
        if (!(entity instanceof dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity golem)) return;
        if (!golem.canModify(player)) return;
        new JukeboxMenuPvd(golem).open(player);
    }

    private static void handleTogglePlay(JukeboxPacket packet, NetworkEvent.Context ctx) {
        ServerPlayer player = ctx.getSender();
        if (player == null) return;
        Entity entity = player.serverLevel().getEntity(packet.entityId);
        if (!(entity instanceof JukeboxGolem golem)) return;

        boolean nowPlaying = !golem.mgdp$isPlaying();
        golem.mgdp$setPlaying(nowPlaying);
        golem.mgdp$setTick(0);

        ItemStack disc = golem.mgdp$getDisc();
        if (!disc.isEmpty() && src.toi_et_moi.mgdp.jukebox.NetMusicCompat.isNetMusicDisc(disc)) {
            if (nowPlaying && entity != null) {
                src.toi_et_moi.mgdp.jukebox.NetMusicCompat.playForTracking(entity, disc);
            } else if (entity != null) {
                var stopPacket = new src.toi_et_moi.mgdp.jukebox.packet.NetMusicSoundPacket(
                        src.toi_et_moi.mgdp.jukebox.packet.NetMusicSoundPacket.Action.STOP, packet.entityId, "", 0, "");
                Mgdp.PACKET_HANDLER.send(
                        PacketDistributor.TRACKING_ENTITY.with(() -> entity), stopPacket);
            }
            return;
        }
        if (!disc.isEmpty() && disc.getItem() instanceof RecordItem ri) {
            ResourceLocation snd = ri.getSound().getLocation();
            if (nowPlaying) {
                playRecordForTracking(entity, snd);
            } else {
                stopRecordForTracking(entity, snd);
            }
        }
    }

    // ===== Helpers for server-side use =====

    public static void playRecordForTracking(Entity entity, ResourceLocation soundId) {
        Mgdp.PACKET_HANDLER.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity),
                new JukeboxPacket(Action.PLAY_RECORD, entity.getId(), soundId));
    }

    public static void stopRecordForTracking(Entity entity, ResourceLocation soundId) {
        Mgdp.PACKET_HANDLER.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity),
                new JukeboxPacket(Action.STOP_RECORD, entity.getId(), soundId));
    }

    public static void playRecordForPlayer(ServerPlayer player, ResourceLocation soundId, int entityId) {
        Mgdp.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player),
                new JukeboxPacket(Action.PLAY_RECORD, entityId, soundId));
    }

    public static void stopRecordForPlayer(ServerPlayer player, ResourceLocation soundId, int entityId) {
        Mgdp.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player),
                new JukeboxPacket(Action.STOP_RECORD, entityId, soundId));
    }

    // ===== Client-side handler =====

    @OnlyIn(Dist.CLIENT)
    private static class ClientHandler {

        private static final java.util.Map<Integer, java.util.UUID> activeSoundKeys = new java.util.HashMap<>();

        static void handle(JukeboxPacket packet) {
            switch (packet.action) {
                case PLAY_RECORD -> handlePlayRecord(packet);
                case STOP_RECORD -> handleStopRecord(packet);
            }
        }

        static void handlePlayRecord(JukeboxPacket packet) {
            if (packet.soundId == null) return;
            Minecraft mc = Minecraft.getInstance();
            mc.execute(() -> {
                if (activeSoundKeys.containsKey(packet.entityId)) {
                    mc.getSoundManager().stop(packet.soundId, SoundSource.MUSIC);
                    activeSoundKeys.remove(packet.entityId);
                }
                SoundEvent event = BuiltInRegistries.SOUND_EVENT.get(packet.soundId);
                if (event != null) {
                    java.util.UUID key = java.util.UUID.randomUUID();
                    activeSoundKeys.put(packet.entityId, key);
                    mc.getSoundManager().play(SimpleSoundInstance.forMusic(event));
                }
            });
        }

        static void handleStopRecord(JukeboxPacket packet) {
            if (packet.soundId == null) return;
            Minecraft mc = Minecraft.getInstance();
            mc.execute(() -> {
                activeSoundKeys.remove(packet.entityId);
                mc.getSoundManager().stop(packet.soundId, SoundSource.MUSIC);
            });
        }
    }
}
