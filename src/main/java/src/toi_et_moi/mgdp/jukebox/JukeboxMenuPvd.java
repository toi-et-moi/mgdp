package src.toi_et_moi.mgdp.jukebox;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.menu.registry.IMenuPvd;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkHooks;

public record JukeboxMenuPvd(AbstractGolemEntity<?, ?> golem) implements IMenuPvd {

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.mgdp.jukebox");
    }

    @Override
    public void writeBuffer(FriendlyByteBuf buf) {
        buf.writeInt(golem.getId());
    }

    public void open(ServerPlayer player) {
        NetworkHooks.openScreen(player, this, this::writeBuffer);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new JukeboxMenu(src.toi_et_moi.mgdp.init.MgdpMenus.JUKEBOX.get(), id, inv, golem);
    }
}
