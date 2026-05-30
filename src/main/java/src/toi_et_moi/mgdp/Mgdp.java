package src.toi_et_moi.mgdp;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import src.toi_et_moi.mgdp.item.IronCurtainItem;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import src.toi_et_moi.mgdp.init.MGDPItems;
import src.toi_et_moi.mgdp.init.MGDPKeyMappings;
import src.toi_et_moi.mgdp.init.MGDPModifiers;
import src.toi_et_moi.mgdp.modifier.ChargedShieldModifier;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.entity.common.GolemFlags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Items;
import dev.xkmc.modulargolems.content.item.equipments.MetalGolemWeaponItem;
import dev.xkmc.modulargolems.content.entity.dog.DogGolemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

@Mod(Mgdp.MODID)
public class Mgdp {

	public static final String MODID = "mgdp";
	private static final Logger LOGGER = LogUtils.getLogger();

	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

	public static final RegistryObject<MetalGolemWeaponItem> SIMPLE_GOLEM_SPEAR = ITEMS.register("simple_golem_spear",
			() -> new MetalGolemWeaponItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 10, 0, 10.0F, 2.0F));
	public static final RegistryObject<MetalGolemWeaponItem> SIMPLE_IRON_GOLEM_SPEAR = ITEMS.register("simple_iron_golem_spear",
			() -> new MetalGolemWeaponItem(new Item.Properties().stacksTo(1), 6, 0.3, 4.0F, 2.0F));
	public static final RegistryObject<MetalGolemWeaponItem> SIMPLE_DIAMOND_GOLEM_SPEAR = ITEMS.register("simple_diamond_golem_spear",
			() -> new MetalGolemWeaponItem(new Item.Properties().stacksTo(1), 8, 0.4, 4.0F, 2.0F));
	public static final RegistryObject<MetalGolemWeaponItem> SIMPLE_NETHERITE_GOLEM_SPEAR = ITEMS.register("simple_netherite_golem_spear",
			() -> new MetalGolemWeaponItem(new Item.Properties().stacksTo(1), 10, 0.5, 4.0F, 2.0F));

		public static final RegistryObject<IronCurtainItem> IRON_CURTAIN = ITEMS.register("iron_curtain",
				() -> new IronCurtainItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

	public static final RegistryObject<CreativeModeTab> MGDP_TAB = CREATIVE_MODE_TABS.register("mgdp_tab",
			() -> CreativeModeTab.builder()
					.title(Component.translatable("itemGroup.mgdp"))
					.icon(() -> MGDPItems.FLIGHT.get().getDefaultInstance())
					.displayItems((params, output) -> {
						output.accept(MGDPItems.HARVEST_CROP.get());
						output.accept(MGDPItems.FLIGHT.get());
						output.accept(MGDPItems.POTION_AURA.get());
						output.accept(MGDPItems.REBIRTH.get());
						output.accept(MGDPItems.UNSTOPPABLE.get());
						output.accept(MGDPItems.SPIRIT.get());
						output.accept(MGDPItems.NETHERITE_GOLD.get());
						output.accept(MGDPItems.ENCHANTED_NETHERITE_GOLD.get());
						output.accept(MGDPItems.BELL_OF_AVICI.get());
						output.accept(MGDPItems.DIAMOND_ATTACK.get());
						output.accept(MGDPItems.ENCHANTED_DIAMOND_ATTACK.get());
						output.accept(MGDPItems.CRIMSON_ATTACK.get());
						output.accept(MGDPItems.ENCHANTED_CRIMSON_ATTACK.get());
						output.accept(MGDPItems.LIGHTNING_STORM.get());
						output.accept(MGDPItems.ROCKET_FLIGHT.get());
						output.accept(MGDPItems.DRAGON_BREATH.get());
						output.accept(MGDPItems.WITHER_EXTINCTION.get());
						output.accept(MGDPItems.CHARGED_SHIELD.get());
						output.accept(SIMPLE_GOLEM_SPEAR.get());
						output.accept(SIMPLE_IRON_GOLEM_SPEAR.get());
						output.accept(SIMPLE_NETHERITE_GOLEM_SPEAR.get());
						output.accept(SIMPLE_DIAMOND_GOLEM_SPEAR.get());
						output.accept(IRON_CURTAIN.get());
						output.accept(MGDPItems.HYPOTHERMIA.get());
						output.accept(MGDPItems.SELF_REPAIR.get());
						output.accept(MGDPItems.SONIC_BOOM.get());
						output.accept(MGDPItems.FOCUSED_DEFENSE.get());
						output.accept(MGDPItems.EXECUTIONER.get());
						output.accept(MGDPItems.INVISIBILITY.get());
						if (net.minecraftforge.fml.ModList.get().isLoaded("irons_spellbooks"))
							output.accept(MGDPItems.TRUE_INVISIBILITY.get());
						output.accept(MGDPItems.ARMOR_PIERCE.get());
						output.accept(MGDPItems.MAGIC_RESISTANCE.get());
						output.accept(MGDPItems.VERSATILITY.get());
						output.accept(MGDPItems.DAMAGE_CAP.get());
						output.accept(MGDPItems.TOTEMIC.get());
						output.accept(MGDPItems.ENCHANTED_TOTEMIC.get());
					})
					.build());

	public Mgdp() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		modEventBus.addListener(this::commonSetup);

		BLOCKS.register(modEventBus);
		ITEMS.register(modEventBus);
		CREATIVE_MODE_TABS.register(modEventBus);

		MinecraftForge.EVENT_BUS.register(this);

		MGDPModifiers.register();
		MGDPItems.register();

		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
	}

	private void commonSetup(final FMLCommonSetupEvent event) {
		LOGGER.info("MGDP common setup");
	}

	@SubscribeEvent
	public void onLivingAttack(LivingAttackEvent event) {
		if (IronCurtainItem.isProtected(event.getEntity())) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void mgdp$lightningShieldRecharge(LivingAttackEvent event) {
		if (event.getSource().is(DamageTypes.LIGHTNING_BOLT)
				&& event.getEntity() instanceof AbstractGolemEntity<?, ?> golem
				&& golem.hasFlag(GolemFlags.THUNDER_IMMUNE)
				&& golem.getModifiers().containsKey(MGDPModifiers.CHARGED_SHIELD.get())) {
			int lv = golem.getModifiers().get(MGDPModifiers.CHARGED_SHIELD.get());
			ChargedShieldModifier.recharge(golem, lv);
		}
	}


	@SubscribeEvent(priority = EventPriority.HIGH)
	public void mgdp$voidRescue(LivingAttackEvent event) {
		if (!(event.getEntity() instanceof Player player)) return;
		if (!event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD)) return;

		AABB area = player.getBoundingBox().inflate(64);
		for (var golem : player.level().getEntitiesOfClass(DogGolemEntity.class, area,
				e -> e.isAlive() && (e.getModifiers().containsKey(MGDPModifiers.FLIGHT.get())
						|| e.getModifiers().containsKey(MGDPModifiers.ROCKET_FLIGHT.get())))) {
			if (!golem.hasPassenger(player)) {
				event.setCanceled(true);
				player.startRiding(golem, true);
				break;
			}
		}
	}



	private static int mgdp$flintGuard = 0;

	@SubscribeEvent
	public void mgdp$flintExplosion(LivingAttackEvent event) {
		if (mgdp$flintGuard > 0) return;
		if (!(event.getSource().getDirectEntity() instanceof AbstractGolemEntity<?, ?> golem)) return;
		if (!golem.getMainHandItem().is(Items.FLINT_AND_STEEL)
				&& !golem.getOffhandItem().is(Items.FLINT_AND_STEEL)) return;
		if (event.getEntity().level().isClientSide()) return;

		event.getEntity().setSecondsOnFire(5);
		mgdp$flintGuard++;
		event.getEntity().level().explode(golem, event.getEntity().getX(), event.getEntity().getY(),
				event.getEntity().getZ(), 2.0F, Level.ExplosionInteraction.NONE);
		mgdp$flintGuard--;
	}



	@SubscribeEvent
	public void mgdp$balloonSlayer(LivingHurtEvent event) {
		if (!(event.getSource().getDirectEntity() instanceof AbstractGolemEntity<?, ?> golem)) return;
		if (!golem.getMainHandItem().is(SIMPLE_GOLEM_SPEAR.get())) return;

		String name = event.getEntity().getName().getString().toLowerCase();
		if (name.contains("balloon") || name.contains("气球")) {
			event.setAmount(event.getAmount() * 661F);
		}
	}

	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event) {
		LOGGER.info("MGDP server starting");
	}

	@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ClientModEvents {

		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event) {
		}

		@SubscribeEvent
		public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
			event.register(MGDPKeyMappings.FLIGHT_DESCEND);
				event.register(MGDPKeyMappings.FLIGHT_SPRINT);
		}
	}
}
