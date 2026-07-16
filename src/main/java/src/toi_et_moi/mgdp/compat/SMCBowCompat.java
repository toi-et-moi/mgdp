package src.toi_et_moi.mgdp.compat;

import dev.xkmc.mob_weapon_api.registry.WeaponRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import src.toi_et_moi.mgdp.Mgdp;
import src.toi_et_moi.mgdp.compat.goety_revelation.RevelationBowBehavior;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = Mgdp.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SMCBowCompat {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        if (ModList.get().isLoaded("smc")) {
            // Rainbow Bow: fires RainbowArrow
            registerSMCBow("rainbow_bow");
            // Frostium/Perfrostite Bows: triple arrow spread
            registerSMCBow("frostium_bow");
            registerSMCBow("perfrostite_bow");
        }
        // Revelation Bow: fires DeathArrow with 2x speed/damage + debuffs
        if (ModList.get().isLoaded("goety_revelation")) {
            registerRevelationBow();
        }
    }

    private static void registerSMCBow(String bowId) {
        ResourceLocation id = new ResourceLocation("smc", bowId);
        var item = ForgeRegistries.ITEMS.getValue(id);
        if (item == null) return;

        WeaponRegistry.BOW.register(
                new ResourceLocation("mgdp", "smc_" + bowId),
                (ItemStack stack) -> stack.is(item)
                        ? Optional.of(dev.xkmc.mob_weapon_api.registry.WeaponStatus.RANGED)
                        : Optional.empty(),
                (LivingEntity user, ItemStack stack) -> new SMCBowBehavior(() -> true),
                10);
    }

    private static void registerRevelationBow() {
        var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("goety_revelation", "bow_of_revelation"));
        if (item == null) return;

        WeaponRegistry.BOW.register(
                new ResourceLocation("mgdp", "revelation_bow"),
                (ItemStack stack) -> stack.is(item)
                        ? Optional.of(dev.xkmc.mob_weapon_api.registry.WeaponStatus.RANGED)
                        : Optional.empty(),
                (LivingEntity user, ItemStack stack) -> new RevelationBowBehavior(),
                10);
    }
}
