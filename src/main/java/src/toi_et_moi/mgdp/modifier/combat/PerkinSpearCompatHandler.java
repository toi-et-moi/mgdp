package src.toi_et_moi.mgdp.modifier.combat;

import dev.xkmc.mob_weapon_api.registry.WeaponRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import src.toi_et_moi.mgdp.Mgdp;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = Mgdp.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PerkinSpearCompatHandler {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        if (!net.minecraftforge.fml.ModList.get().isLoaded("smc")) return;

        Item spearItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation("smc", "perkin_spear"));
        if (spearItem == null) return;

        WeaponRegistry.HOLD.register(
                new ResourceLocation("mgdp", "perkin_spear"),
                (net.minecraft.world.item.ItemStack s) -> s.is(spearItem)
                        ? Optional.of(dev.xkmc.mob_weapon_api.registry.WeaponStatus.RANGED)
                        : Optional.empty(),
                (net.minecraft.world.entity.LivingEntity user, net.minecraft.world.item.ItemStack stack) ->
                        new PerkinSpearThrowBehavior(),
                5);
    }
}
