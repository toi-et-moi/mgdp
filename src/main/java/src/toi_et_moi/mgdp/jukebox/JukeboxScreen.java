package src.toi_et_moi.mgdp.jukebox;

import dev.xkmc.l2library.base.menu.base.BaseContainerScreen;
import dev.xkmc.modulargolems.content.menu.registry.EquipmentGroup;
import dev.xkmc.modulargolems.content.menu.tabs.GolemTabManager;
import dev.xkmc.modulargolems.content.menu.tabs.ITabScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import src.toi_et_moi.mgdp.Mgdp;

public class JukeboxScreen extends BaseContainerScreen<JukeboxMenu> implements ITabScreen {

    private static final ResourceLocation CONTAINER_BG = new ResourceLocation("textures/gui/container/inventory.png");
    private Button playPauseBtn;

    public JukeboxScreen(JukeboxMenu cont, Inventory plInv, Component title) {
        super(cont, plInv, title);
    }

    @Override
    protected void init() {
        super.init();
        if (menu.golem != null) {
            new GolemTabManager<>(this, new EquipmentGroup(menu.golem))
                    .init(this::addRenderableWidget, JukeboxClientRegister.JUKEBOX_TAB);
        }

        this.playPauseBtn = Button.builder(
                        Component.literal(">"),
                        btn -> onPlayPause())
                .bounds(leftPos + 106, topPos + 19, 40, 20)
                .build();
        this.addRenderableWidget(playPauseBtn);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mx, int my) {
        var sr = menu.sprite.get().getRenderer(this);
        sr.start(g);

        // Draw slot background for disc slot
        g.blit(CONTAINER_BG, leftPos + 79, topPos + 19, 7, 7, 18, 18);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mx, int my) {
        g.drawString(font, title, titleLabelX, titleLabelY, 0x404040);
    }

    @Override
    protected void renderTooltip(GuiGraphics g, int mx, int my) {
        super.renderTooltip(g, mx, my);
        if (menu.getCarried().isEmpty() && hoveredSlot != null && !hoveredSlot.hasItem()
                && hoveredSlot.index == 0) {
            g.renderTooltip(font, java.util.List.of(
                    Component.translatable("gui.mgdp.jukebox.disc_slot")),
                    java.util.Optional.empty(), ItemStack.EMPTY, mx, my);
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (playPauseBtn != null) {
            boolean playing = menu.golem instanceof JukeboxGolem jb && jb.mgdp$isPlaying();
            playPauseBtn.setMessage(Component.literal(playing ? "||" : ">"));
        }
    }

    private void onPlayPause() {
        if (menu.golem != null) {
            Mgdp.PACKET_HANDLER.sendToServer(new JukeboxPacket(
                    JukeboxPacket.Action.TOGGLE_PLAY, menu.golem.getId()));
        }
    }

    @Override
    public int getGuiLeft() {
        return leftPos;
    }

    @Override
    public int getGuiTop() {
        return topPos;
    }

    @Override
    public int screenWidth() {
        return width;
    }

    @Override
    public int screenHeight() {
        return height;
    }
}
