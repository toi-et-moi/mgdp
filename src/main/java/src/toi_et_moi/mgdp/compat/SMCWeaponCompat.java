package src.toi_et_moi.mgdp.compat;

import dev.xkmc.mob_weapon_api.registry.WeaponRegistry;
import dev.xkmc.mob_weapon_api.registry.WeaponStatus;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import src.toi_et_moi.mgdp.Mgdp;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = Mgdp.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SMCWeaponCompat {

	@SubscribeEvent
	public static void onCommonSetup(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			if (ModList.get().isLoaded("smc")) {
				registerMeowmere();
				registerExcalibur();
			}
		});
	}

	private static void registerMeowmere() {
		var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("smc", "meowmere"));
		if (item == null) return;

		WeaponRegistry.INSTANT.register(
				new ResourceLocation("mgdp", "meowmere"),
				stack -> stack.is(item) ? Optional.of(WeaponStatus.OFFENSIVE) : Optional.empty(),
				(user, stack) -> new MeowmereBehavior(),
				5);
	}

	private static void registerExcalibur() {
		var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("smc", "excalibur"));
		if (item == null) return;

		WeaponRegistry.INSTANT.register(
				new ResourceLocation("mgdp", "excalibur"),
				stack -> stack.is(item) ? Optional.of(WeaponStatus.OFFENSIVE) : Optional.empty(),
				(user, stack) -> new ExcaliburBehavior(),
				5);
	}
}
