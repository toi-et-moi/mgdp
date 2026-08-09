package src.toi_et_moi.mgdp.util;

import dev.xkmc.modulargolems.events.event.GolemToOwnerEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import src.toi_et_moi.mgdp.Mgdp;

/**
 * Sends a chat notification when a golem is returned to the owner via the
 * parent mod's RECYCLE path. Subscribes to GolemToOwnerEvent, which is only
 * posted from AbstractGolemEntity.returnToInventory() — i.e. the recycle
 * branches in hurt/postHurt/untrackRemoved, plus our additive DISCARDED
 * mixin. Active retrieval via toItem() does NOT post this event, so it
 * naturally avoids the "false alarm" case.
 *
 * Localisation lives under {@code mgdp.recycle.recovered} in both zh_cn and
 * en_us (placeholder {0} = the golem's display name). Colour is applied via
 * withStyle so the language strings stay free of {@code §}-codes.
 */
@Mod.EventBusSubscriber(modid = Mgdp.MODID)
public class RecycleNotifyHandler {

    private static final String KEY = "mgdp.recycle.recovered";

    @SubscribeEvent
    public static void onGolemReturned(GolemToOwnerEvent event) {
        if (!(event.getOwner() instanceof ServerPlayer player)) return;
        ItemStack stack = event.getStack();
        if (stack.isEmpty()) return;

        player.sendSystemMessage(
                Component.translatable(KEY, stack.getHoverName().getString())
                        .withStyle(ChatFormatting.GREEN)
        );
    }
}
