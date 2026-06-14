package src.toi_et_moi.mgdp.jukebox;

import dev.xkmc.l2library.base.menu.base.BaseContainerMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;

public class JukeboxContainer extends BaseContainerMenu.BaseContainer<JukeboxMenu> {

    public JukeboxContainer(JukeboxMenu menu) {
        super(1, menu);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (parent.golem instanceof JukeboxGolem jb && !parent.golem.level().isClientSide) {
            ItemStack oldDisc = jb.mgdp$getDisc().copy();
            ItemStack newDisc = getItem(0);
            jb.mgdp$setDisc(newDisc.copy());
            if (newDisc.isEmpty() && !oldDisc.isEmpty() && jb.mgdp$isPlaying()) {
                jb.mgdp$setPlaying(false);
                jb.mgdp$setTick(0);
                if (oldDisc.getItem() instanceof RecordItem ri) {
                    JukeboxPacket.stopRecordForTracking(parent.golem, ri.getSound().getLocation());
                } else if (src.toi_et_moi.mgdp.jukebox.NetMusicCompat.isNetMusicDisc(oldDisc)) {
                    var stopPacket = new src.toi_et_moi.mgdp.jukebox.packet.NetMusicSoundPacket(
                            src.toi_et_moi.mgdp.jukebox.packet.NetMusicSoundPacket.Action.STOP,
                            parent.golem.getId(), "", 0, "");
                    src.toi_et_moi.mgdp.Mgdp.PACKET_HANDLER.send(
                            net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY.with(() -> parent.golem), stopPacket);
                }
            }
        }
    }
}
