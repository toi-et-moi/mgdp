package src.toi_et_moi.mgdp.modifier.combat;

import dev.xkmc.mob_weapon_api.registry.WeaponRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import src.toi_et_moi.mgdp.Mgdp;

@Mod.EventBusSubscriber(modid = Mgdp.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CleaverCompatHandler {

	@SubscribeEvent
	public static void onCommonSetup(FMLCommonSetupEvent event) {
		TagKey<Item> tagDD = TagKey.create(Registries.ITEM, new ResourceLocation("dungeonsdelight", "cleavers"));

		dev.xkmc.mob_weapon_api.registry.RangedStatusPredicate pred =
				(net.minecraft.world.item.ItemStack s) -> s.is(tagDD)
						? java.util.Optional.of(dev.xkmc.mob_weapon_api.registry.WeaponStatus.RANGED)
						: java.util.Optional.empty();
		dev.xkmc.mob_weapon_api.registry.RangedBehaviorFactory<dev.xkmc.mob_weapon_api.api.simple.IHoldWeaponBehavior> factory =
				(net.minecraft.world.entity.LivingEntity user, net.minecraft.world.item.ItemStack stack) -> new CleaverThrowBehavior();

		WeaponRegistry.HOLD.register(
				new ResourceLocation("mgdp", "cleaver"),
				pred, factory, 5);
	}
}
