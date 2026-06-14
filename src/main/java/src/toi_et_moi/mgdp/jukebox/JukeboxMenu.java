package src.toi_et_moi.mgdp.jukebox;

import dev.xkmc.l2library.base.menu.base.BaseContainerMenu;
import dev.xkmc.l2library.base.menu.base.SpriteManager;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;

public class JukeboxMenu extends BaseContainerMenu<JukeboxMenu> {

    public static final SpriteManager MANAGER = new SpriteManager(src.toi_et_moi.mgdp.Mgdp.MODID, "jukebox");

    public final AbstractGolemEntity<?, ?> golem;
    public final Player player;

    public static JukeboxMenu fromNetwork(int wid, Inventory inv, FriendlyByteBuf buf) {
        var entity = inv.player.level().getEntity(buf.readInt());
        return new JukeboxMenu(src.toi_et_moi.mgdp.init.MgdpMenus.JUKEBOX.get(), wid, inv,
                entity instanceof AbstractGolemEntity<?, ?> golem ? golem : null);
    }

    public JukeboxMenu(MenuType<?> type, int wid, Inventory plInv, AbstractGolemEntity<?, ?> golem) {
        super(type, wid, plInv, MANAGER, menu -> new JukeboxContainer((JukeboxMenu) menu), true);
        this.golem = golem;
        this.player = plInv.player;

        addSlot("disc", stack -> stack.getItem() instanceof RecordItem
                || net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem())
                        .equals(new net.minecraft.resources.ResourceLocation("netmusic", "music_cd")));

        // Initialize disc from golem data
        if (golem instanceof JukeboxGolem jb && !jb.mgdp$getDisc().isEmpty()) {
            container.setItem(0, jb.mgdp$getDisc().copy());
        }
    }

    @Override
    public void removed(Player player) {
        // Clear container slot without triggering setChanged() to avoid dropping
        // disc back to player (disc is already stored on the golem)
        if (container != null) {
            container.removeItemNoUpdate(0);
        }
        super.removed(player);
    }

    @Override
    protected void securedServerSlotChange(net.minecraft.world.Container cont) {
        if (golem instanceof JukeboxGolem jb && !golem.level().isClientSide) {
            ItemStack oldDisc = jb.mgdp$getDisc().copy();
            ItemStack newDisc = cont.getItem(0);
            jb.mgdp$setDisc(newDisc.copy());
            if (newDisc.isEmpty() && !oldDisc.isEmpty() && jb.mgdp$isPlaying()) {
                jb.mgdp$setPlaying(false);
                jb.mgdp$setTick(0);
                if (oldDisc.getItem() instanceof RecordItem ri) {
                    JukeboxPacket.stopRecordForTracking(golem, ri.getSound().getLocation());
                } else if (src.toi_et_moi.mgdp.jukebox.NetMusicCompat.isNetMusicDisc(oldDisc)) {
                    var stopPacket = new src.toi_et_moi.mgdp.jukebox.packet.NetMusicSoundPacket(
                            src.toi_et_moi.mgdp.jukebox.packet.NetMusicSoundPacket.Action.STOP,
                            golem.getId(), "", 0, "");
                    src.toi_et_moi.mgdp.Mgdp.PACKET_HANDLER.send(
                            net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY.with(() -> golem), stopPacket);
                }
            }
        }
    }
}
