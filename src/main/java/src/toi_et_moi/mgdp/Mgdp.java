package src.toi_et_moi.mgdp;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
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
import net.minecraftforge.registries.ForgeRegistries;
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

@Mod(Mgdp.MODID)
public class Mgdp {

	public static final String MODID = "mgdp";
	private static final Logger LOGGER = LogUtils.getLogger();

	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

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
						output.accept(IRON_CURTAIN.get());
						output.accept(MGDPItems.VERSATILITY.get());
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

	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event) {
		LOGGER.info("MGDP server starting");
	}

	@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ClientModEvents {

		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event) {
			LOGGER.info("MGDP client setup");
			LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
		}

		@SubscribeEvent
		public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
			event.register(MGDPKeyMappings.FLIGHT_DESCEND);
				event.register(MGDPKeyMappings.FLIGHT_SPRINT);
		}
	}
}
