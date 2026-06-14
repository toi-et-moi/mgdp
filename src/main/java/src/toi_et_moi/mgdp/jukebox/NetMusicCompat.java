package src.toi_et_moi.mgdp.jukebox;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.PacketDistributor;
import src.toi_et_moi.mgdp.Mgdp;
import src.toi_et_moi.mgdp.jukebox.packet.NetMusicSoundPacket;

import java.lang.reflect.Method;

public class NetMusicCompat {

    private static Boolean loaded = null;
    private static Method getSongInfoMethod;
    private static Class<?> songInfoClass;

    public static boolean isLoaded() {
        if (loaded == null) {
            loaded = ModList.get().isLoaded("netmusic");
            if (loaded) {
                try {
                    var cdClass = Class.forName("com.github.tartaricacid.netmusic.item.ItemMusicCD");
                    getSongInfoMethod = cdClass.getMethod("getSongInfo", ItemStack.class);
                    songInfoClass = Class.forName("com.github.tartaricacid.netmusic.item.ItemMusicCD$SongInfo");
                } catch (Exception e) {
                    loaded = false;
                }
            }
        }
        return loaded;
    }

    private static Object getSongInfo(ItemStack stack) {
        try {
            return getSongInfoMethod.invoke(null, stack);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isNetMusicDisc(ItemStack stack) {
        var info = getSongInfo(stack);
        return info != null && songInfoClass.isInstance(info);
    }

    private static NetMusicSoundPacket buildPlayPacket(int entityId, ItemStack disc) {
        try {
            var info = getSongInfo(disc);
            if (info == null) return null;
            String url = (String) info.getClass().getField("songUrl").get(info);
            String name = (String) info.getClass().getField("songName").get(info);
            int time = (int) info.getClass().getField("songTime").get(info);
            if (url == null || url.isEmpty()) return null;
            return new NetMusicSoundPacket(NetMusicSoundPacket.Action.PLAY, entityId, url, time, name);
        } catch (Exception e) {
            return null;
        }
    }

    /** Send PLAY to all players tracking the entity (for toggle-play, already includes owner) */
    public static void playForTracking(Entity entity, ItemStack disc) {
        var packet = buildPlayPacket(entity.getId(), disc);
        if (packet == null) return;
        Mgdp.PACKET_HANDLER.send(
                PacketDistributor.TRACKING_ENTITY.with(() -> entity), packet);
    }

    /** Send PLAY to a specific player (for re-summon, when trackers aren't ready yet) */
    public static void playForPlayer(ServerPlayer player, int entityId, ItemStack disc) {
        var packet = buildPlayPacket(entityId, disc);
        if (packet == null) return;
        Mgdp.PACKET_HANDLER.send(
                PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
