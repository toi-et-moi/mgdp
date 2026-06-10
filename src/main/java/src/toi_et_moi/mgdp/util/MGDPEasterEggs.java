package src.toi_et_moi.mgdp.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import src.toi_et_moi.mgdp.Mgdp;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = Mgdp.MODID)
public class MGDPEasterEggs {

    private static final UUID TARGET_UUID = UUID.fromString("3f54b1a8-32b8-4c7d-85c7-087f28128050");
    private static final String TARGET_NAME = "toi_et_moi";
    private static final TagKey<Item> SUMMONERS_TAG = ItemTags.create(new ResourceLocation(Mgdp.MODID, "summoners"));

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        var data = player.getPersistentData();
        String key = Mgdp.MODID + "_easter_given";
        if (data.getBoolean(key)) return;

        boolean match = player.getUUID().equals(TARGET_UUID);
        if (!match) {
            boolean offline = !player.server.usesAuthentication();
            if (!offline || !TARGET_NAME.equals(player.getGameProfile().getName())) return;
        }

        data.putBoolean(key, true);
        giveItems(player);
    }

    private static void giveItems(ServerPlayer player) {
        for (var entry : net.minecraftforge.registries.ForgeRegistries.ITEMS.getEntries()) {
            Item item = entry.getValue();
            if (item.builtInRegistryHolder().is(SUMMONERS_TAG)) {
                giveItem(player, new ItemStack(item));
            }
        }
        var wand = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                new ResourceLocation("modulargolems", "omnipotent_wand_command"));
        if (wand != null) {
            giveItem(player, new ItemStack(wand));
        }
    }

    private static void giveItem(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
