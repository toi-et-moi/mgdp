package src.toi_et_moi.mgdp.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import src.toi_et_moi.mgdp.Mgdp;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = Mgdp.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MGDPEasterEggs {

    private static final UUID TARGET_UUID = UUID.fromString("3f54b1a8-32b8-4c7d-85c7-087f28128050");
    private static final String TARGET_NAME = "toi_et_moi";
    private static boolean mgdp$exploding = false;
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
        var workbench = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                new ResourceLocation("modulargolems", "golem_workbench"));
        if (workbench != null) {
            giveItem(player, new ItemStack(workbench));
        }
    }

    private static void giveItem(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    // Easter egg: Flint & Steel golem attack → explosion, no durability loss
    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (mgdp$exploding) return;
        if (!(event.getSource().getEntity() instanceof dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity golem)) return;
        ItemStack weapon = golem.getMainHandItem();
        if (weapon.isEmpty() || !weapon.is(Items.FLINT_AND_STEEL)) return;

        mgdp$exploding = true;
        event.getEntity().level().explode(golem, event.getEntity().getX(),
                event.getEntity().getY(), event.getEntity().getZ(), 2.0f,
                net.minecraft.world.level.Level.ExplosionInteraction.NONE);
        mgdp$exploding = false;
        weapon.setDamageValue(0);
    }

    // Easter egg: Simple Golem Spear ×661 vs named "balloon" / "气球"
    @SubscribeEvent
    public static void BalloonSlayer(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getSource().getEntity() instanceof dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity golem)) return;
        ItemStack weapon = golem.getMainHandItem();
        if (weapon.isEmpty()) return;
        if (!weapon.is(Mgdp.SIMPLE_GOLEM_SPEAR.get())) return;

        String name = event.getEntity().getName().getString().trim();
        if ("balloon".equalsIgnoreCase(name) || "气球".equals(name)) {
            event.setAmount(event.getAmount() * 661);
        }
    }
}
