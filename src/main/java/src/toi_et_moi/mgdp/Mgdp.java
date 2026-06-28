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
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import src.toi_et_moi.mgdp.item.IronCurtainItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import src.toi_et_moi.mgdp.init.MGDPItems;
import src.toi_et_moi.mgdp.advancement.InitTrigger;
import src.toi_et_moi.mgdp.init.MGDPKeyMappings;
import src.toi_et_moi.mgdp.init.MGDPModifiers;
import src.toi_et_moi.mgdp.init.MGDPStats;
import src.toi_et_moi.mgdp.modifier.defense.ChargedShieldModifier;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.entity.common.GolemFlags;
import net.minecraft.world.damagesource.DamageTypes;
import dev.xkmc.modulargolems.content.item.equipments.MetalGolemWeaponItem;

@Mod(Mgdp.MODID)
public class Mgdp {

	public static final String MODID = "mgdp";
	public static final Logger LOGGER = LogUtils.getLogger();

	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

	public static SimpleChannel PACKET_HANDLER;



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
						output.accept(MGDPItems.CONDUIT.get());
						output.accept(MGDPItems.OVERWORLD.get());
						output.accept(MGDPItems.NETHER.get());
						output.accept(MGDPItems.SUNLIGHT.get());
						output.accept(MGDPItems.CORONA.get());
						output.accept(MGDPItems.MOON_SHADOW.get());
						output.accept(MGDPItems.TIME_AXIS.get());
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
						if (net.minecraftforge.fml.ModList.get().isLoaded("l2hostility")) {
							output.accept(MGDPItems.ADAPTIVE.get());
							output.accept(MGDPItems.DISPELL.get());
							output.accept(MGDPItems.PULLING.get());
							output.accept(MGDPItems.REPELLING.get());
							output.accept(MGDPItems.DEMENTOR.get());
							output.accept(MGDPItems.DRAIN.get());
							output.accept(MGDPItems.REPRINT.get());
						output.accept(MGDPItems.SELF_DESTRUCT.get());
						}
						output.accept(MGDPItems.FIREBALL.get());
						output.accept(MGDPItems.HERO.get());
						output.accept(MGDPItems.FLARE.get());
						output.accept(MGDPItems.UNBREAKABLE.get());
						output.accept(MGDPItems.INFINITE_AMMO.get());
						output.accept(MGDPItems.QUICK_STRIKE.get());
						output.accept(MGDPItems.GRENADE.get());
						output.accept(MGDPItems.UNDYING.get());
						output.accept(MGDPItems.ANGLER.get());
						output.accept(MGDPItems.ANVIL_SLAM.get());
						output.accept(MGDPItems.TRIDENT_FESTIVAL.get());
						output.accept(MGDPItems.IRON_UPGRADE.get());
						output.accept(MGDPItems.DISARM.get());
						output.accept(MGDPItems.RIPTIDE.get());
						output.accept(MGDPItems.END_VOID.get());
						output.accept(MGDPItems.END_OF_BEGINNING.get());
						output.accept(MGDPItems.DEATH_KNELL.get());
						output.accept(MGDPItems.ECHO_TRIO.get());
						if (net.minecraftforge.fml.ModList.get().isLoaded("cataclysm")) {
							output.accept(MGDPItems.HARBINGER_BEAM.get());
							output.accept(MGDPItems.HARBINGER_MISSILE.get());
							output.accept(MGDPItems.IGNIS_ATTACK.get());
							output.accept(MGDPItems.IGNIS_FIREBALL.get());
							output.accept(MGDPItems.IGNIS_JUMP.get());
						}
						output.accept(MGDPItems.MIND_CONTROL.get());
						output.accept(MGDPItems.CREATIVE_SLOT.get());
						output.accept(MGDPItems.CREATIVE_SLOT_100.get());
						output.accept(MGDPItems.BRUSH.get());
						output.accept(MGDPItems.BOMB_DISPOSAL.get());
						output.accept(MGDPItems.PROJECTILE_DODGE.get());
						output.accept(MGDPItems.BACKSTEP.get());
						output.accept(MGDPItems.PROSPERITY.get());
						output.accept(MGDPItems.LIQUID_CLEAR.get());
						output.accept(MGDPItems.BLAST_FURNACE.get());
							output.accept(MGDPItems.MAGIC_IMMUNE.get());
							if (net.minecraftforge.fml.ModList.get().isLoaded("twilightforest")) output.accept(MGDPItems.IRONWOOD.get());
							if (net.minecraftforge.fml.ModList.get().isLoaded("twilightforest")) output.accept(MGDPItems.STEELEAF.get());
							if (net.minecraftforge.fml.ModList.get().isLoaded("twilightforest")) output.accept(MGDPItems.FIERY.get());
							if (net.minecraftforge.fml.ModList.get().isLoaded("twilightforest")) output.accept(MGDPItems.KNIGHTMETAL.get());
							if (net.minecraftforge.fml.ModList.get().isLoaded("twilightforest")) output.accept(MGDPItems.CARMINITE.get());
							if (net.minecraftforge.fml.ModList.get().isLoaded("goety")) output.accept(MGDPItems.CRONE.get());
							if (net.minecraftforge.fml.ModList.get().isLoaded("goety")) output.accept(MGDPItems.BOTTLING.get());
							if (net.minecraftforge.fml.ModList.get().isLoaded("goety")) output.accept(MGDPItems.NECROMANCER.get());
							if (net.minecraftforge.fml.ModList.get().isLoaded("youkaishomecoming")) output.accept(MGDPItems.PHANTOM.get());
						if (net.minecraftforge.fml.ModList.get().isLoaded("twilightforest"))
							output.accept(MGDPItems.LAST_LINE.get());
						if (net.minecraftforge.fml.ModList.get().isLoaded("curseofpandora"))
							output.accept(MGDPItems.REALITY_SUPPRESSION.get());
						if (net.minecraftforge.fml.ModList.get().isLoaded("golemmagicka"))
							output.accept(MGDPItems.MANA_OVERLOAD.get());
							if (net.minecraftforge.fml.ModList.get().isLoaded("goety")) output.accept(MGDPItems.VOID_ECHO.get());
							if (net.minecraftforge.fml.ModList.get().isLoaded("create")) output.accept(MGDPItems.COATING.get());
							if (net.minecraftforge.fml.ModList.get().isLoaded("create")) output.accept(MGDPItems.MECHANICAL_ENGINE.get());
							if (net.minecraftforge.fml.ModList.get().isLoaded("create")) output.accept(MGDPItems.MECHANICAL_FORCE.get());
							if (net.minecraftforge.fml.ModList.get().isLoaded("create")) output.accept(MGDPItems.MECHANICAL_MOBILITY.get());
							if (net.minecraftforge.fml.ModList.get().isLoaded("cataclysm")) output.accept(MGDPItems.CATACLYSMFARMER_TEMPLATE.get());
							if (net.minecraftforge.fml.ModList.get().isLoaded("goety")) output.accept(MGDPItems.DARK_TEMPLATE.get());
							if (net.minecraftforge.fml.ModList.get().isLoaded("irons_spellbooks")) output.accept(MGDPItems.PYRIUM_TEMPLATE.get());
							if (net.minecraftforge.fml.ModList.get().isLoaded("l2complements")) output.accept(MGDPItems.SCULKIUM_TEMPLATE.get());
							if (net.minecraftforge.fml.ModList.get().isLoaded("jerotesvillage")) output.accept(MGDPItems.MEROR_TEMPLATE.get());
							if (net.minecraftforge.fml.ModList.get().isLoaded("jerotesvillage")) output.accept(MGDPItems.REFINE_MEROR_TEMPLATE.get());
							output.accept(MGDPItems.LORD.get());
							output.accept(MGDPItems.SNOW_TRAIL.get());
							output.accept(MGDPItems.SWAP.get());
						output.accept(MGDPItems.BACKFLIP.get());
						output.accept(MGDPItems.WINDMILL.get());
							output.accept(MGDPItems.WITCH.get());
							if (net.minecraftforge.fml.ModList.get().isLoaded("twilightforest")) output.accept(MGDPItems.PENGUIN.get());
						output.accept(MGDPItems.REMNANT_GOLEM.get());
						output.accept(MGDPItems.ILLAGER_GOLEM.get());
						output.accept(MGDPItems.PIGLIN_GOLEM.get());
						output.accept(MGDPItems.SCULK_GOLEM.get());
							if (net.minecraftforge.fml.ModList.get().isLoaded("twilightforest")) {
								output.accept(MGDPItems.TWILIGHT_GOLEM.get());
							}

							if (net.minecraftforge.fml.ModList.get().isLoaded("cataclysm")) {
								output.accept(MGDPItems.HARBINGER_GOLEM.get());
								output.accept(MGDPItems.MONSTROSITY_GOLEM.get());
								output.accept(MGDPItems.ENDER_GUARDIAN_GOLEM.get());
								output.accept(MGDPItems.IGNIS_GOLEM.get());
								output.accept(MGDPItems.SCYLLA_GOLEM.get());
							}
							if (net.minecraftforge.fml.ModList.get().isLoaded("jerotes_village_golems")) {
								output.accept(MGDPItems.CARVED_GOLEM.get());
								output.accept(MGDPItems.ENHANCED_CARVED_GOLEM.get());
								output.accept(MGDPItems.QOAIKU_GOLEM.get());
								output.accept(MGDPItems.MEROR_GOLEM.get());
								output.accept(MGDPItems.REFINE_MEROR_GOLEM.get());
							}
						})
						.build());

	public Mgdp() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		modEventBus.addListener(this::commonSetup);

		BLOCKS.register(modEventBus);
		ITEMS.register(modEventBus);
		CREATIVE_MODE_TABS.register(modEventBus);
			src.toi_et_moi.mgdp.init.MgdpMenus.MENUS.register(modEventBus);

		MinecraftForge.EVENT_BUS.register(this);

		MGDPModifiers.register();
		MGDPItems.register();
		InitTrigger.init();
		src.toi_et_moi.mgdp.network.MGDPNetwork.register();
			PACKET_HANDLER = src.toi_et_moi.mgdp.network.MGDPNetwork.CHANNEL;
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
	}

	private void commonSetup(final FMLCommonSetupEvent event) {
		LOGGER.info("MGDP common setup");
	}

	@SubscribeEvent
	public void onLivingAttack(LivingAttackEvent evt) {
		if (IronCurtainItem.isProtected(evt.getEntity())) {
			evt.setCanceled(true);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void mgdp$lightningShieldRecharge(LivingAttackEvent evt) {
		if (evt.getSource().is(DamageTypes.LIGHTNING_BOLT)
				&& evt.getEntity() instanceof AbstractGolemEntity<?, ?> golem
				&& golem.hasFlag(GolemFlags.THUNDER_IMMUNE)
				&& golem.getModifiers().containsKey(MGDPModifiers.CHARGED_SHIELD.get())) {
			int lv = golem.getModifiers().get(MGDPModifiers.CHARGED_SHIELD.get());
			ChargedShieldModifier.recharge(golem, lv);
		}
	}


	@SubscribeEvent
    public void mgdp$startTracking(PlayerEvent.StartTracking evt) {
        if (!(evt.getTarget() instanceof src.toi_et_moi.mgdp.jukebox.JukeboxGolem jb)) return;
        if (!jb.mgdp$isPlaying()) return;
        if (!(evt.getEntity() instanceof net.minecraft.server.level.ServerPlayer sp)) return;
        var disc = jb.mgdp$getDisc();
        if (disc.isEmpty()) return;
        if (src.toi_et_moi.mgdp.jukebox.NetMusicCompat.isNetMusicDisc(disc)) {
            src.toi_et_moi.mgdp.jukebox.NetMusicCompat.playForPlayer(
                    sp, evt.getTarget().getId(), disc);
            return;
        }
        if (disc.getItem() instanceof net.minecraft.world.item.RecordItem ri) {
            src.toi_et_moi.mgdp.jukebox.JukeboxPacket.playRecordForPlayer(
                    sp, ri.getSound().getLocation(), evt.getTarget().getId());
        }
    }
    public void mgdp$onEntityJoin(net.minecraftforge.event.entity.EntityJoinLevelEvent evt) {
        if (!(evt.getEntity() instanceof src.toi_et_moi.mgdp.jukebox.JukeboxGolem jb)) return;
        if (evt.getLevel().isClientSide()) return;
        if (!jb.mgdp$isPlaying()) return;
        var disc = jb.mgdp$getDisc();
        if (disc.isEmpty()) return;
        if (evt.getEntity() instanceof dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity golem) {
            if (src.toi_et_moi.mgdp.jukebox.NetMusicCompat.isNetMusicDisc(disc)) {
                if (golem.getOwnerUUID() != null && golem.getServer() != null) {
                    var owner = golem.getServer().getPlayerList().getPlayer(golem.getOwnerUUID());
                    if (owner != null) {
                        src.toi_et_moi.mgdp.jukebox.NetMusicCompat.playForPlayer(
                                owner, golem.getId(), disc);
                    }
                }
                return;
            }
            if (disc.getItem() instanceof net.minecraft.world.item.RecordItem ri
                    && golem.getOwnerUUID() != null && golem.getServer() != null) {
                var owner = golem.getServer().getPlayerList().getPlayer(golem.getOwnerUUID());
                if (owner != null) {
                    src.toi_et_moi.mgdp.jukebox.JukeboxPacket.playRecordForPlayer(
                            owner, ri.getSound().getLocation(), golem.getId());
                }
            }
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
			event.enqueueWork(() -> {
			src.toi_et_moi.mgdp.jukebox.JukeboxClientRegister.register();
			net.minecraft.client.gui.screens.MenuScreens.register(
				src.toi_et_moi.mgdp.init.MgdpMenus.JUKEBOX.get(),
				src.toi_et_moi.mgdp.jukebox.JukeboxScreen::new);
		});
		}

		@SubscribeEvent
		public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
			event.register(MGDPKeyMappings.FLIGHT_DESCEND);
				event.register(MGDPKeyMappings.FLIGHT_SPRINT);
				event.register(MGDPKeyMappings.SWAP);
		}
	}

	@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
	public static class ClientTickHandler {
		static java.util.List<? extends dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity> cachedLowHpGolems = java.util.Collections.emptyList();
		static long lastScanTick = -1;
		@SubscribeEvent
		public static void onClientTick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
			if (MGDPKeyMappings.SWAP.consumeClick()) {
				Mgdp.PACKET_HANDLER.sendToServer(new src.toi_et_moi.mgdp.modifier.SwapPacket());
			}
        }
        @SubscribeEvent
        public static void onRenderLivingPost(net.minecraftforge.client.event.RenderLivingEvent.Post<?, ?> event) {
            if (!(event.getEntity() instanceof dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity)) return;
            int eid = event.getEntity().getId();
            if (eid != src.toi_et_moi.mgdp.jukebox.packet.GolemNetMusicSound.activeEntityId) return;
            String lyric = src.toi_et_moi.mgdp.jukebox.packet.GolemNetMusicSound.currentLyricLine;
            String trans = src.toi_et_moi.mgdp.jukebox.packet.GolemNetMusicSound.currentTransLyric;
            if ((lyric == null || lyric.isEmpty()) && (trans == null || trans.isEmpty())) return;
            var pose = event.getPoseStack();
            var camera = net.minecraft.client.Minecraft.getInstance().getBlockEntityRenderDispatcher().camera;
            var font = net.minecraft.client.Minecraft.getInstance().font;
            int bgColor = (int)(net.minecraft.client.Minecraft.getInstance().options.getBackgroundOpacity(0.25F) * 255) << 24;
            var buf = event.getMultiBufferSource();
            pose.pushPose();
            double yOff = event.getEntity().getBbHeight() + 0.75;
            if (trans != null && !trans.isEmpty()) yOff += 0.3;
            pose.translate(0, yOff, 0);
            pose.mulPose(camera.rotation());
            pose.scale(-0.025F, -0.025F, 0.025F);
            if (lyric != null && !lyric.isEmpty()) {
                float lw = (float) (-font.width(lyric) / 2);
                font.drawInBatch(lyric, lw, 0, 0xFFFFFF, false, pose.last().pose(), buf,
                        net.minecraft.client.gui.Font.DisplayMode.NORMAL, bgColor, 0xF000F0);
            }
            if (trans != null && !trans.isEmpty()) {
                float tw = (float) (-font.width(trans) / 2);
                int tY = (lyric != null && !lyric.isEmpty()) ? 12 : 0;
                font.drawInBatch(trans, tw, tY, 0xFFFFAA, false, pose.last().pose(), buf,
                        net.minecraft.client.gui.Font.DisplayMode.NORMAL, bgColor, 0xF000F0);
            }
            pose.popPose();
        }
        
        @SubscribeEvent
        public static void onRenderLivingPre(net.minecraftforge.client.event.RenderLivingEvent.Pre<?, ?> event) {
            if (!(event.getEntity() instanceof dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity golem)) return;
            int progress = ((src.toi_et_moi.mgdp.init.IFlipData) golem).mgdp$getFlipProgress();
            if (progress <= 0) return;
            float angle = (float) java.lang.Math.toRadians(progress * 0.9f);
            float yaw = (float) java.lang.Math.toRadians(-golem.yBodyRot);
            var stack = event.getPoseStack();
            stack.mulPose(new org.joml.Quaternionf().rotationY(yaw));
            stack.mulPose(new org.joml.Quaternionf().rotationX(-angle));
            stack.mulPose(new org.joml.Quaternionf().rotationY(-yaw));
        }

        @SubscribeEvent
        public static void onRenderPre(net.minecraftforge.client.event.RenderLivingEvent.Pre<?, ?> event) {
            if (!(event.getEntity() instanceof dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity golem)) return;
            float windmill = ((src.toi_et_moi.mgdp.init.IFlipData) golem).mgdp$getWindmill();
            if (windmill <= 0) return;
            event.getPoseStack().mulPose(new org.joml.Quaternionf().rotationY((float) java.lang.Math.toRadians(windmill)));
        }

        @SubscribeEvent
        public static void onRenderOverlay(net.minecraftforge.client.event.RenderGuiOverlayEvent event) {
            if (event.getOverlay() != net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.SUBTITLES.type()) return;
            if (!src.toi_et_moi.mgdp.Config.golemHealthWarning) return;
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.player == null || mc.level == null) return;
            // Cache entity scan to every 10 ticks for performance
            long gameTime = mc.level.getGameTime();
            if (gameTime - Mgdp.ClientTickHandler.lastScanTick >= 10 || Mgdp.ClientTickHandler.lastScanTick < 0) {
                Mgdp.ClientTickHandler.lastScanTick = gameTime;
                Mgdp.ClientTickHandler.cachedLowHpGolems = mc.level.getEntitiesOfClass(
                    dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity.class,
                    mc.player.getBoundingBox().inflate(64),
                    g -> g.isAlive() && !g.isHostile() && g.getHealth() / g.getMaxHealth() < 0.25f);
            }
            var cache = Mgdp.ClientTickHandler.cachedLowHpGolems;
            java.util.List<net.minecraft.network.chat.MutableComponent> lines = new java.util.ArrayList<>();
            for (var g : cache) {
                lines.add(net.minecraft.network.chat.Component.literal("! ")
                    .append(g.getDisplayName())
                    .append(net.minecraft.network.chat.Component.literal(" HP: " + (int)g.getHealth() + "/" + (int)g.getMaxHealth())));
            }

            if (lines.isEmpty()) return;
            int warned = 0;
            int w = event.getWindow().getGuiScaledWidth();
            int h = event.getWindow().getGuiScaledHeight();
            var font = mc.font;
            var startY = h - 65 - 12 * (lines.size() - 1);
            for (var text : lines) {
                int tw = font.width(text);
                event.getGuiGraphics().drawString(font, text, (w - tw) / 2, startY + 12 * warned, 0xFF4444);
                warned++;
            }
        }
		}
}
