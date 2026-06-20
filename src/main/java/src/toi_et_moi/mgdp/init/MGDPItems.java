package src.toi_et_moi.mgdp.init;

import dev.xkmc.modulargolems.content.item.upgrade.SimpleUpgradeItem;
import dev.xkmc.modulargolems.content.item.upgrade.AddSlotTemplate;
import dev.xkmc.modulargolems.init.registrate.GolemItems;
import dev.xkmc.modulargolems.init.registrate.GolemModifiers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import java.util.List;

import net.minecraft.world.item.Rarity;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;
import src.toi_et_moi.mgdp.Mgdp;
import src.toi_et_moi.mgdp.item.GolemSummonItem;

public class MGDPItems {

	public static final RegistryObject<SimpleUpgradeItem> HARVEST_CROP;
	public static final RegistryObject<SimpleUpgradeItem> FLIGHT;
	public static final RegistryObject<SimpleUpgradeItem> POTION_AURA;
	public static final RegistryObject<SimpleUpgradeItem> REBIRTH;
	public static final RegistryObject<SimpleUpgradeItem> UNSTOPPABLE;
	public static final RegistryObject<SimpleUpgradeItem> SPIRIT;
	public static final RegistryObject<SimpleUpgradeItem> NETHERITE_GOLD;
	public static final RegistryObject<SimpleUpgradeItem> ENCHANTED_NETHERITE_GOLD;
	public static final RegistryObject<SimpleUpgradeItem> BELL_OF_AVICI;
	public static final RegistryObject<SimpleUpgradeItem> DIAMOND_ATTACK;
	public static final RegistryObject<SimpleUpgradeItem> ENCHANTED_DIAMOND_ATTACK;
	public static final RegistryObject<SimpleUpgradeItem> CRIMSON_ATTACK;
	public static final RegistryObject<SimpleUpgradeItem> ENCHANTED_CRIMSON_ATTACK;
	public static final RegistryObject<SimpleUpgradeItem> LIGHTNING_STORM;
	public static final RegistryObject<SimpleUpgradeItem> ROCKET_FLIGHT;
	public static final RegistryObject<SimpleUpgradeItem> DRAGON_BREATH;
	public static final RegistryObject<SimpleUpgradeItem> WITHER_EXTINCTION;
	public static final RegistryObject<SimpleUpgradeItem> CHARGED_SHIELD;
	public static final RegistryObject<SimpleUpgradeItem> VERSATILITY;
	public static final RegistryObject<SimpleUpgradeItem> CONDUIT;
	public static final RegistryObject<SimpleUpgradeItem> OVERWORLD;
	public static final RegistryObject<SimpleUpgradeItem> NETHER;
	public static final RegistryObject<SimpleUpgradeItem> SUNLIGHT;
	public static final RegistryObject<SimpleUpgradeItem> HYPOTHERMIA;
	public static final RegistryObject<SimpleUpgradeItem> SELF_REPAIR;
	public static final RegistryObject<SimpleUpgradeItem> SONIC_BOOM;
	public static final RegistryObject<SimpleUpgradeItem> FOCUSED_DEFENSE;
	public static final RegistryObject<SimpleUpgradeItem> EXECUTIONER;
	public static final RegistryObject<SimpleUpgradeItem> INVISIBILITY;
	public static final RegistryObject<SimpleUpgradeItem> TRUE_INVISIBILITY;
	public static final RegistryObject<SimpleUpgradeItem> ARMOR_PIERCE;
	public static final RegistryObject<SimpleUpgradeItem> MAGIC_RESISTANCE;
	public static final RegistryObject<SimpleUpgradeItem> DAMAGE_CAP;
	public static final RegistryObject<SimpleUpgradeItem> TOTEMIC;
	public static final RegistryObject<SimpleUpgradeItem> HERO;
	public static final RegistryObject<SimpleUpgradeItem> FLARE;
	public static final RegistryObject<SimpleUpgradeItem> UNDYING;
	public static final RegistryObject<SimpleUpgradeItem> GRENADE;
	public static final RegistryObject<SimpleUpgradeItem> UNBREAKABLE;
	public static final RegistryObject<SimpleUpgradeItem> INFINITE_AMMO;
	public static final RegistryObject<SimpleUpgradeItem> PROSPERITY;
	public static final RegistryObject<SimpleUpgradeItem> LIQUID_CLEAR;
	public static final RegistryObject<SimpleUpgradeItem> MAGIC_IMMUNE;
	public static final RegistryObject<SimpleUpgradeItem> IRONWOOD;
	public static final RegistryObject<SimpleUpgradeItem> STEELEAF;
	public static final RegistryObject<SimpleUpgradeItem> FIERY;
	public static final RegistryObject<SimpleUpgradeItem> KNIGHTMETAL;
	public static final RegistryObject<SimpleUpgradeItem> CARMINITE;
	public static final RegistryObject<SimpleUpgradeItem> COATING;
	public static final RegistryObject<SimpleUpgradeItem> LORD;
	public static final RegistryObject<SimpleUpgradeItem> SNOW_TRAIL;
	public static final RegistryObject<SimpleUpgradeItem> SWAP;
	public static final RegistryObject<SimpleUpgradeItem> BACKFLIP;
	public static final RegistryObject<SimpleUpgradeItem> WINDMILL;
	public static final RegistryObject<SimpleUpgradeItem> WITCH;
	public static final RegistryObject<SimpleUpgradeItem> CRONE;
	public static final RegistryObject<SimpleUpgradeItem> BOTTLING;
	public static final RegistryObject<SimpleUpgradeItem> PENGUIN;
	public static final RegistryObject<SimpleUpgradeItem> QUICK_STRIKE;
	public static final RegistryObject<SimpleUpgradeItem> ANGLER;
	public static final RegistryObject<SimpleUpgradeItem> DEATH_KNELL;
	public static final RegistryObject<SimpleUpgradeItem> ECHO_TRIO;
	public static final RegistryObject<SimpleUpgradeItem> ANVIL_SLAM;
	public static final RegistryObject<SimpleUpgradeItem> IRON_UPGRADE;
	public static final RegistryObject<SimpleUpgradeItem> TRIDENT_FESTIVAL;
	public static final RegistryObject<SimpleUpgradeItem> RIPTIDE;
	public static final RegistryObject<SimpleUpgradeItem> END_VOID;
	public static final RegistryObject<SimpleUpgradeItem> ENCHANTED_TOTEMIC;
	public static final RegistryObject<SimpleUpgradeItem> SELF_DESTRUCT;
	public static final RegistryObject<SimpleUpgradeItem> DEMENTOR;
	public static final RegistryObject<SimpleUpgradeItem> DRAIN;
	public static final RegistryObject<SimpleUpgradeItem> REPRINT;
	public static final RegistryObject<SimpleUpgradeItem> FIREBALL;
	public static final RegistryObject<SimpleUpgradeItem> BRUSH;
	public static final RegistryObject<SimpleUpgradeItem> BOMB_DISPOSAL;
	public static final RegistryObject<SimpleUpgradeItem> PROJECTILE_DODGE;
	public static final RegistryObject<SimpleUpgradeItem> BACKSTEP;
	public static final RegistryObject<SimpleUpgradeItem> ADAPTIVE;
	public static final RegistryObject<SimpleUpgradeItem> DISPELL;

	public static final RegistryObject<SimpleUpgradeItem> MECHANICAL_ENGINE;
	public static final RegistryObject<SimpleUpgradeItem> MECHANICAL_FORCE;
	public static final RegistryObject<SimpleUpgradeItem> MECHANICAL_MOBILITY;
	public static final RegistryObject<SimpleUpgradeItem> BLAST_FURNACE;

	public static final RegistryObject<GolemSummonItem> REMNANT_GOLEM;
	public static final RegistryObject<GolemSummonItem> ILLAGER_GOLEM;
	public static final RegistryObject<GolemSummonItem> PIGLIN_GOLEM;
	public static final RegistryObject<GolemSummonItem> SCULK_GOLEM;
	public static final RegistryObject<GolemSummonItem> TWILIGHT_GOLEM;
	public static final RegistryObject<GolemSummonItem> HARBINGER_GOLEM;
	public static final RegistryObject<GolemSummonItem> MONSTROSITY_GOLEM;
	public static final RegistryObject<GolemSummonItem> ENDER_GUARDIAN_GOLEM;
	public static final RegistryObject<GolemSummonItem> IGNIS_GOLEM;
	public static final RegistryObject<GolemSummonItem> SCYLLA_GOLEM;
	public static final RegistryObject<GolemSummonItem> CARVED_GOLEM;
	public static final RegistryObject<GolemSummonItem> ENHANCED_CARVED_GOLEM;
	public static final RegistryObject<GolemSummonItem> QOAIKU_GOLEM;
	public static final RegistryObject<GolemSummonItem> MEROR_GOLEM;
	public static final RegistryObject<GolemSummonItem> REFINE_MEROR_GOLEM;
	public static final RegistryObject<AddSlotTemplate> CATACLYSMFARMER_TEMPLATE;
	public static final RegistryObject<AddSlotTemplate> MEROR_TEMPLATE;
	public static final RegistryObject<AddSlotTemplate> REFINE_MEROR_TEMPLATE;
	public static final RegistryObject<AddSlotTemplate> DARK_TEMPLATE;
	public static final RegistryObject<AddSlotTemplate> PYRIUM_TEMPLATE;
	public static final RegistryObject<AddSlotTemplate> SCULKIUM_TEMPLATE;

	public static final RegistryObject<SimpleUpgradeItem> END_OF_BEGINNING;
	public static final RegistryObject<SimpleUpgradeItem> CORONA;
	public static final RegistryObject<SimpleUpgradeItem> MOON_SHADOW;
	public static final RegistryObject<SimpleUpgradeItem> TIME_AXIS;

	static {
		HARVEST_CROP = Mgdp.ITEMS.register("harvest_crop",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.HARVEST_CROP.get(), 1, false));

		FLIGHT = Mgdp.ITEMS.register("flight",
				() -> new SimpleUpgradeItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC), () -> MGDPModifiers.FLIGHT.get(), 1, true));

		POTION_AURA = Mgdp.ITEMS.register("potion_aura",
				() -> new SimpleUpgradeItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC), () -> MGDPModifiers.POTION_AURA.get(), 1, true));

		REBIRTH = Mgdp.ITEMS.register("rebirth",
				() -> new SimpleUpgradeItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC), () -> MGDPModifiers.REBIRTH.get(), 1, true));

		UNSTOPPABLE = Mgdp.ITEMS.register("unstoppable",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.UNSTOPPABLE.get(), 1, true));

		SPIRIT = Mgdp.ITEMS.register("spirit",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.SPIRIT.get(), 1, true));

		NETHERITE_GOLD = Mgdp.ITEMS.register("netherite_gold",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.NETHERITE_GOLD.get(), 1, false));

		ENCHANTED_NETHERITE_GOLD = Mgdp.ITEMS.register("enchanted_netherite_gold",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.ENCHANTED_NETHERITE_GOLD.get(), 1, true));

		BELL_OF_AVICI = Mgdp.ITEMS.register("bell_of_avici",
				() -> new SimpleUpgradeItem(new Item.Properties().rarity(Rarity.UNCOMMON), () -> MGDPModifiers.BELL_OF_AVICI.get(), 1, true));

		DIAMOND_ATTACK = Mgdp.ITEMS.register("diamond_attack",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.DIAMOND_ATTACK.get(), 1, false));

		ENCHANTED_DIAMOND_ATTACK = Mgdp.ITEMS.register("enchanted_diamond_attack",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.ENCHANTED_DIAMOND_ATTACK.get(), 1, true));

		CRIMSON_ATTACK = Mgdp.ITEMS.register("crimson_attack",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.CRIMSON_ATTACK.get(), 1, false));

		ENCHANTED_CRIMSON_ATTACK = Mgdp.ITEMS.register("enchanted_crimson_attack",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.ENCHANTED_CRIMSON_ATTACK.get(), 1, true));

		LIGHTNING_STORM = Mgdp.ITEMS.register("lighting_storm",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.LIGHTNING_STORM.get(), 1, true));

		ROCKET_FLIGHT = Mgdp.ITEMS.register("rocket_flight",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.ROCKET_FLIGHT.get(), 1, false));

		DRAGON_BREATH = Mgdp.ITEMS.register("dragon_breath",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.DRAGON_BREATH.get(), 1, false));

		WITHER_EXTINCTION = Mgdp.ITEMS.register("wither_extinction",
				() -> new SimpleUpgradeItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC), () -> MGDPModifiers.WITHER_EXTINCTION.get(), 1, true));

		CHARGED_SHIELD = Mgdp.ITEMS.register("charged_shield",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.CHARGED_SHIELD.get(), 1, false));
		ARMOR_PIERCE = Mgdp.ITEMS.register("armor_pierce",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> dev.xkmc.modulargolems.init.registrate.GolemModifiers.ARMOR_BYPASS.get(), 1, false));

		MAGIC_RESISTANCE = Mgdp.ITEMS.register("magic_resistance",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> dev.xkmc.modulargolems.init.registrate.GolemModifiers.MAGIC_RES.get(), 1, false));

		DAMAGE_CAP = Mgdp.ITEMS.register("damage_cap",
				() -> new SimpleUpgradeItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC), () -> dev.xkmc.modulargolems.init.registrate.GolemModifiers.DAMAGE_CAP.get(), 1, true));

		TOTEMIC = Mgdp.ITEMS.register("totemic",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.TOTEMIC.get(), 1, false));

		ENCHANTED_TOTEMIC = Mgdp.ITEMS.register("enchanted_totemic",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.TOTEMIC.get(), 2, true));

		HERO = Mgdp.ITEMS.register("hero",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.HERO.get(), 1, false));

		FLARE = Mgdp.ITEMS.register("flare",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.FLARE.get(), 1, false));

		UNDYING = Mgdp.ITEMS.register("hostility_undying",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.UNDYING.get(), 1, false));

		GRENADE = Mgdp.ITEMS.register("hostility_grenade",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.GRENADE.get(), 1, false));

		UNBREAKABLE = Mgdp.ITEMS.register("unbreakable",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.UNBREAKABLE.get(), 1, false));

		INFINITE_AMMO = Mgdp.ITEMS.register("infinite_ammo",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.INFINITE_AMMO.get(), 1, false));

		PROSPERITY = Mgdp.ITEMS.register("prosperity",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.PROSPERITY.get(), 1, false));
	LORD = Mgdp.ITEMS.register("lord",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.LORD.get(), 1, false));


		SNOW_TRAIL = Mgdp.ITEMS.register("snow_trail",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.SNOW_TRAIL.get(), 1, false));

		WINDMILL = Mgdp.ITEMS.register("windmill",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.WINDMILL.get(), 1, false));

		BACKFLIP = Mgdp.ITEMS.register("backflip",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.BACKFLIP.get(), 1, false));

		SWAP = Mgdp.ITEMS.register("swap",
			() -> new SimpleUpgradeItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC), () -> MGDPModifiers.SWAP.get(), 1, false));

		WITCH = Mgdp.ITEMS.register("witch",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.WITCH.get(), 1, false));

		CRONE = Mgdp.ITEMS.register("crone",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> (net.minecraftforge.fml.ModList.get().isLoaded("goety"))
				? MGDPModifiers.CRONE.get() : null, 1, false));

		BOTTLING = Mgdp.ITEMS.register("bottling",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> (net.minecraftforge.fml.ModList.get().isLoaded("goety"))
				? MGDPModifiers.BOTTLING.get() : null, 1, false));


		PENGUIN = Mgdp.ITEMS.register("penguin",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.PENGUIN.get(), 1, false));

		LIQUID_CLEAR = Mgdp.ITEMS.register("liquid_clear",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.LIQUID_CLEAR.get(), 1, false));
		MAGIC_IMMUNE = Mgdp.ITEMS.register("magic_immune",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> dev.xkmc.modulargolems.init.registrate.GolemModifiers.MAGIC_IMMUNE.get(), 1, true));
		IRONWOOD = Mgdp.ITEMS.register("ironwood",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> (net.minecraftforge.fml.ModList.get().isLoaded("twilightforest")) ? dev.xkmc.modulargolems.compat.materials.twilightforest.TFCompatRegistry.TF_HEALING.get() : null, 5, true));

		STEELEAF = Mgdp.ITEMS.register("steeleaf",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> (net.minecraftforge.fml.ModList.get().isLoaded("twilightforest")) ? dev.xkmc.modulargolems.compat.materials.twilightforest.TFCompatRegistry.TF_DAMAGE.get() : null, 5, true));
		FIERY = Mgdp.ITEMS.register("fiery",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> (net.minecraftforge.fml.ModList.get().isLoaded("twilightforest"))
				? dev.xkmc.modulargolems.compat.materials.twilightforest.TFCompatRegistry.FIERY.get() : null, 5, true));

		KNIGHTMETAL = Mgdp.ITEMS.register("knightmetal",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> (net.minecraftforge.fml.ModList.get().isLoaded("twilightforest"))
				? dev.xkmc.modulargolems.init.registrate.GolemModifiers.THORN.get() : null, 5, true));
		CARMINITE = Mgdp.ITEMS.register("carminite",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> (net.minecraftforge.fml.ModList.get().isLoaded("twilightforest"))
				? dev.xkmc.modulargolems.compat.materials.twilightforest.TFCompatRegistry.CARMINITE.get() : null, 5, true));

		COATING = Mgdp.ITEMS.register("coating",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> (net.minecraftforge.fml.ModList.get().isLoaded("create"))
				? dev.xkmc.modulargolems.compat.materials.create.CreateCompatRegistry.COATING.get() : null, 5, true));






		QUICK_STRIKE = Mgdp.ITEMS.register("quick_strike",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.QUICK_STRIKE.get(), 1, false));

		ANGLER = Mgdp.ITEMS.register("angler",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.ANGLER.get(), 1, false));

		DEATH_KNELL = Mgdp.ITEMS.register("death_knell",
				() -> new SimpleUpgradeItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC), () -> MGDPModifiers.DEATH_KNELL.get(), 1, true));

		ECHO_TRIO = Mgdp.ITEMS.register("echo_trio",
				() -> new SimpleUpgradeItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC), () -> MGDPModifiers.ECHO_TRIO.get(), 1, true));
		END_VOID = Mgdp.ITEMS.register("end_void",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.END_VOID.get(), 1, false));

		RIPTIDE = Mgdp.ITEMS.register("riptide",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.RIPTIDE.get(), 1, false));

		IRON_UPGRADE = Mgdp.ITEMS.register("iron_upgrade",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.IRON_UPGRADE.get(), 1, false));

		TRIDENT_FESTIVAL = Mgdp.ITEMS.register("trident_festival",
			() -> new SimpleUpgradeItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC), () -> MGDPModifiers.TRIDENT_FESTIVAL.get(), 1, true));

		ANVIL_SLAM = Mgdp.ITEMS.register("anvil_slam",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.ANVIL_SLAM.get(), 1, false));

		ADAPTIVE = Mgdp.ITEMS.register("hostility_adaptive",
				() -> new SimpleUpgradeItem(new Item.Properties(),
				() -> net.minecraftforge.fml.ModList.get().isLoaded("l2hostility") ? dev.xkmc.modulargolems.compat.materials.l2hostility.LHCompatRegistry.LH_ADAPTIVE.get() : null, 1, false));

		DISPELL = Mgdp.ITEMS.register("hostility_dispell",
				() -> new SimpleUpgradeItem(new Item.Properties(),
				() -> net.minecraftforge.fml.ModList.get().isLoaded("l2hostility") ? dev.xkmc.modulargolems.compat.materials.l2hostility.LHCompatRegistry.LH_DISPELL.get() : null, 1, false));

		SELF_DESTRUCT = Mgdp.ITEMS.register("self_destruct",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.SELF_DESTRUCT.get(), 1, false));

		DEMENTOR = Mgdp.ITEMS.register("hostility_dementor",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.DEMENTOR.get(), 1, false));

		DRAIN = Mgdp.ITEMS.register("hostility_drain",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.DRAIN.get(), 1, false));

		REPRINT = Mgdp.ITEMS.register("hostility_reprint",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.REPRINT.get(), 1, false));

		FIREBALL = Mgdp.ITEMS.register("fireball",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.FIREBALL.get(), 1, false));

		BRUSH = Mgdp.ITEMS.register("brush",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.BRUSH.get(), 1, false));

		BOMB_DISPOSAL = Mgdp.ITEMS.register("bomb_disposal",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.BOMB_DISPOSAL.get(), 1, false));

		BACKSTEP = Mgdp.ITEMS.register("backstep",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.BACKSTEP.get(), 1, false));

		PROJECTILE_DODGE = Mgdp.ITEMS.register("projectile_dodge",
				() -> new SimpleUpgradeItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE).fireResistant(), () -> MGDPModifiers.PROJECTILE_DODGE.get(), 1, true));

		MECHANICAL_ENGINE = Mgdp.ITEMS.register("mechanical_engine",
				() -> new SimpleUpgradeItem(new Item.Properties(),
				() -> net.minecraftforge.fml.ModList.get().isLoaded("create") ? dev.xkmc.modulargolems.compat.materials.create.CreateCompatRegistry.BODY.get() : null, 1, false));

		MECHANICAL_FORCE = Mgdp.ITEMS.register("mechanical_force",
				() -> new SimpleUpgradeItem(new Item.Properties(),
				() -> net.minecraftforge.fml.ModList.get().isLoaded("create") ? dev.xkmc.modulargolems.compat.materials.create.CreateCompatRegistry.FORCE.get() : null, 1, false));

		MECHANICAL_MOBILITY = Mgdp.ITEMS.register("mechanical_mobility",
				() -> new SimpleUpgradeItem(new Item.Properties(),
				() -> net.minecraftforge.fml.ModList.get().isLoaded("create") ? dev.xkmc.modulargolems.compat.materials.create.CreateCompatRegistry.MOBILE.get() : null, 1, false));

		BLAST_FURNACE = Mgdp.ITEMS.register("blast_furnace",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.BLAST_FURNACE.get(), 1, false));


			REMNANT_GOLEM = Mgdp.ITEMS.register("remnant_golem",
				() -> new GolemSummonItem(new Item.Properties().stacksTo(1).defaultDurability(20).rarity(net.minecraft.world.item.Rarity.EPIC),
						new ResourceLocation("golemdungeons", "factory_remnant")));

			ILLAGER_GOLEM = Mgdp.ITEMS.register("illager_golem",
				() -> new GolemSummonItem(new Item.Properties().stacksTo(1).defaultDurability(20).rarity(net.minecraft.world.item.Rarity.EPIC),
						new ResourceLocation("golemdungeons", "illagers_creation")));

			PIGLIN_GOLEM = Mgdp.ITEMS.register("piglin_golem",
				() -> new GolemSummonItem(new Item.Properties().stacksTo(1).defaultDurability(20).rarity(net.minecraft.world.item.Rarity.EPIC),
						new ResourceLocation("golemdungeons", "piglin_legacy")));

			SCULK_GOLEM = Mgdp.ITEMS.register("sculk_golem",
				() -> new GolemSummonItem(new Item.Properties().stacksTo(1).defaultDurability(20).rarity(net.minecraft.world.item.Rarity.EPIC),
						new ResourceLocation("golemdungeons", "sculk_infestation")));

			TWILIGHT_GOLEM = Mgdp.ITEMS.register("twilight_golem",
				() -> new GolemSummonItem(new Item.Properties().stacksTo(1).defaultDurability(20).rarity(net.minecraft.world.item.Rarity.EPIC),
						new ResourceLocation("twilightforest", "twilight_invasion")));

		HARBINGER_GOLEM = Mgdp.ITEMS.register("harbinger_golem",
				() -> new GolemSummonItem(new Item.Properties().stacksTo(1).defaultDurability(4).rarity(net.minecraft.world.item.Rarity.EPIC),
						new ResourceLocation("cataclysm", "harbingers_revenge")));

		MONSTROSITY_GOLEM = Mgdp.ITEMS.register("monstrosity_golem",
				() -> new GolemSummonItem(new Item.Properties().stacksTo(1).defaultDurability(4).rarity(net.minecraft.world.item.Rarity.EPIC),
						new ResourceLocation("cataclysm", "monstrosity_expanded")));

		ENDER_GUARDIAN_GOLEM = Mgdp.ITEMS.register("ender_guardian_golem",
				() -> new GolemSummonItem(new Item.Properties().stacksTo(1).defaultDurability(4).rarity(net.minecraft.world.item.Rarity.EPIC),
						new ResourceLocation("cataclysm", "meknight_of_the_end")));

		IGNIS_GOLEM = Mgdp.ITEMS.register("ignis_golem",
				() -> new GolemSummonItem(new Item.Properties().stacksTo(1).defaultDurability(4).rarity(net.minecraft.world.item.Rarity.EPIC),
						new ResourceLocation("cataclysm", "resurgent_flame")));

		SCYLLA_GOLEM = Mgdp.ITEMS.register("scylla_golem",
				() -> new GolemSummonItem(new Item.Properties().stacksTo(1).defaultDurability(4).rarity(net.minecraft.world.item.Rarity.EPIC),
						new ResourceLocation("cataclysm", "heavenly_storm")));

		CARVED_GOLEM = Mgdp.ITEMS.register("carved_golem",
			() -> new GolemSummonItem(new Item.Properties().stacksTo(1).defaultDurability(20).rarity(net.minecraft.world.item.Rarity.EPIC),
				new ResourceLocation("jerotes_village_golems", "villager_metal")));

		ENHANCED_CARVED_GOLEM = Mgdp.ITEMS.register("enhanced_carved_golem",
			() -> new GolemSummonItem(new Item.Properties().stacksTo(1).defaultDurability(20).rarity(net.minecraft.world.item.Rarity.EPIC),
				new ResourceLocation("jerotes_village_golems", "enhanced_villager_metal")));

		QOAIKU_GOLEM = Mgdp.ITEMS.register("qoaiku_golem",
			() -> new GolemSummonItem(new Item.Properties().stacksTo(1).defaultDurability(20).rarity(net.minecraft.world.item.Rarity.EPIC),
				new ResourceLocation("jerotes_village_golems", "qoaiku")));

		MEROR_GOLEM = Mgdp.ITEMS.register("meror_golem",
			() -> new GolemSummonItem(new Item.Properties().stacksTo(1).defaultDurability(20).rarity(net.minecraft.world.item.Rarity.EPIC),
				new ResourceLocation("jerotes_village_golems", "meror")));

		REFINE_MEROR_GOLEM = Mgdp.ITEMS.register("refine_meror_golem",
			() -> new GolemSummonItem(new Item.Properties().stacksTo(1).defaultDurability(20).rarity(net.minecraft.world.item.Rarity.EPIC),
				new ResourceLocation("jerotes_village_golems", "refine_meror")));

		CATACLYSMFARMER_TEMPLATE = Mgdp.ITEMS.register("cataclysmfarer_expansion_template",
			() -> new AddSlotTemplate(new Item.Properties(), () -> MGDPModifiers.CATACLYSMFARMER_ADD.get()));

		DARK_TEMPLATE = Mgdp.ITEMS.register("dark_expansion_template",
			() -> new AddSlotTemplate(new Item.Properties(), () -> MGDPModifiers.DARK_ADD.get()));

		PYRIUM_TEMPLATE = Mgdp.ITEMS.register("pyrium_expansion_template",
			() -> new AddSlotTemplate(new Item.Properties(), () -> MGDPModifiers.PYRIUM_ADD.get()));

		SCULKIUM_TEMPLATE = Mgdp.ITEMS.register("sculkium_expansion_template",
			() -> new AddSlotTemplate(new Item.Properties(), () -> MGDPModifiers.SCULKIUM_ADD.get()));
		MEROR_TEMPLATE = Mgdp.ITEMS.register("meror_expansion_template",
			() -> new AddSlotTemplate(new Item.Properties(), () -> MGDPModifiers.MEROR_ADD.get()));

		REFINE_MEROR_TEMPLATE = Mgdp.ITEMS.register("refine_meror_expansion_template",
			() -> new AddSlotTemplate(new Item.Properties(), () -> MGDPModifiers.REFINE_MEROR_ADD.get()));


		TRUE_INVISIBILITY = Mgdp.ITEMS.register("true_invisibility",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.TRUE_INVISIBILITY.get(), 1, false));

		INVISIBILITY = Mgdp.ITEMS.register("invisibility",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.INVISIBILITY.get(), 1, false));

		EXECUTIONER = Mgdp.ITEMS.register("executioner",
				() -> new SimpleUpgradeItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC), () -> MGDPModifiers.EXECUTIONER.get(), 1, true));

		FOCUSED_DEFENSE = Mgdp.ITEMS.register("focused_defense",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.FOCUSED_DEFENSE.get(), 1, false));

		SONIC_BOOM = Mgdp.ITEMS.register("sonic_boom",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.SONIC_BOOM.get(), 1, false));

		SELF_REPAIR = Mgdp.ITEMS.register("self_repair",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.SELF_REPAIR.get(), 1, false));

		SUNLIGHT = Mgdp.ITEMS.register("sunlight",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.SUNLIGHT.get(), 1, false));

		OVERWORLD = Mgdp.ITEMS.register("overworld",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.OVERWORLD.get(), 1, false));

		NETHER = Mgdp.ITEMS.register("nether",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.NETHER.get(), 1, false));

		CONDUIT = Mgdp.ITEMS.register("conduit",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.CONDUIT.get(), 1, false));

		HYPOTHERMIA = Mgdp.ITEMS.register("hypothermia",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.HYPOTHERMIA.get(), 1, false));

		VERSATILITY = Mgdp.ITEMS.register("versatility",
				() -> new SimpleUpgradeItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC), () -> MGDPModifiers.VERSATILITY.get(), 1, true));

		END_OF_BEGINNING = Mgdp.ITEMS.register("end_of_beginning",
			() -> new SimpleUpgradeItem(new Item.Properties().rarity(Rarity.EPIC), () -> MGDPModifiers.END_OF_BEGINNING.get(), 1, true));

		CORONA = Mgdp.ITEMS.register("corona",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.CORONA.get(), 1, false));

		MOON_SHADOW = Mgdp.ITEMS.register("moon_shadow",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.MOON_SHADOW.get(), 1, false));

		TIME_AXIS = Mgdp.ITEMS.register("time_axis",
			() -> new SimpleUpgradeItem(new Item.Properties().rarity(Rarity.EPIC), () -> MGDPModifiers.TIME_AXIS.get(), 1, true));
	}

	public static void register() {
	}

	@Mod.EventBusSubscriber(modid = Mgdp.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class EventHandler {

		@SubscribeEvent
		public static void addCreative(BuildCreativeModeTabContentsEvent event) {
			if (event.getTabKey() == GolemItems.UPGRADES.getKey()) {
				event.accept(HARVEST_CROP.get());
				event.accept(FLIGHT.get());
				event.accept(POTION_AURA.get());
				event.accept(REBIRTH.get());
				event.accept(UNSTOPPABLE.get());
				event.accept(SPIRIT.get());
				event.accept(NETHERITE_GOLD.get());
				event.accept(ENCHANTED_NETHERITE_GOLD.get());
				event.accept(BELL_OF_AVICI.get());
				event.accept(DIAMOND_ATTACK.get());
				event.accept(ENCHANTED_DIAMOND_ATTACK.get());
				event.accept(CRIMSON_ATTACK.get());
				event.accept(ENCHANTED_CRIMSON_ATTACK.get());
				event.accept(LIGHTNING_STORM.get());
				event.accept(ROCKET_FLIGHT.get());
				event.accept(DRAGON_BREATH.get());
				event.accept(WITHER_EXTINCTION.get());
				event.accept(CHARGED_SHIELD.get());
				event.accept(CONDUIT.get());
			event.accept(OVERWORLD.get());
			event.accept(NETHER.get());
			event.accept(SUNLIGHT.get());
			event.accept(HYPOTHERMIA.get());
				event.accept(SELF_REPAIR.get());
				event.accept(SONIC_BOOM.get());
				event.accept(FOCUSED_DEFENSE.get());
				event.accept(EXECUTIONER.get());
				event.accept(INVISIBILITY.get());
			if (net.minecraftforge.fml.ModList.get().isLoaded("cataclysm")) event.accept(CATACLYSMFARMER_TEMPLATE.get());
			if (net.minecraftforge.fml.ModList.get().isLoaded("goety")) event.accept(DARK_TEMPLATE.get());
			if (net.minecraftforge.fml.ModList.get().isLoaded("irons_spellbooks")) event.accept(PYRIUM_TEMPLATE.get());
			if (net.minecraftforge.fml.ModList.get().isLoaded("l2complements")) event.accept(SCULKIUM_TEMPLATE.get());
			if (net.minecraftforge.fml.ModList.get().isLoaded("jerotes_village_golems")) event.accept(MEROR_TEMPLATE.get());
			if (net.minecraftforge.fml.ModList.get().isLoaded("jerotes_village_golems")) event.accept(REFINE_MEROR_TEMPLATE.get());
			if (net.minecraftforge.fml.ModList.get().isLoaded("irons_spellbooks"))
				event.accept(TRUE_INVISIBILITY.get());
				event.accept(ARMOR_PIERCE.get());
				event.accept(MAGIC_RESISTANCE.get());
				event.accept(VERSATILITY.get());
				event.accept(DAMAGE_CAP.get());
				event.accept(TOTEMIC.get());
				event.accept(ENCHANTED_TOTEMIC.get());
				if (net.minecraftforge.fml.ModList.get().isLoaded("l2hostility")) {
					event.accept(ADAPTIVE.get());
					event.accept(DISPELL.get());
					event.accept(DEMENTOR.get());
					event.accept(DRAIN.get());
					event.accept(REPRINT.get());
					event.accept(GRENADE.get());
					event.accept(UNDYING.get());
				}
				event.accept(SELF_DESTRUCT.get());
				event.accept(FIREBALL.get());
				event.accept(HERO.get());
				event.accept(FLARE.get());
				event.accept(UNBREAKABLE.get());
				event.accept(BLAST_FURNACE.get());
				event.accept(ANVIL_SLAM.get());
			event.accept(TRIDENT_FESTIVAL.get());
			event.accept(IRON_UPGRADE.get());
				event.accept(RIPTIDE.get());
			event.accept(END_VOID.get());
				event.accept(INFINITE_AMMO.get());
				event.accept(PROSPERITY.get());
				event.accept(LIQUID_CLEAR.get());
			event.accept(MAGIC_IMMUNE.get());
			if (net.minecraftforge.fml.ModList.get().isLoaded("twilightforest")) event.accept(IRONWOOD.get());
			if (net.minecraftforge.fml.ModList.get().isLoaded("twilightforest")) event.accept(STEELEAF.get());
			if (net.minecraftforge.fml.ModList.get().isLoaded("twilightforest")) event.accept(FIERY.get());
			if (net.minecraftforge.fml.ModList.get().isLoaded("twilightforest")) event.accept(KNIGHTMETAL.get());
			if (net.minecraftforge.fml.ModList.get().isLoaded("twilightforest")) event.accept(CARMINITE.get());
			if (net.minecraftforge.fml.ModList.get().isLoaded("goety")) event.accept(CRONE.get());
			if (net.minecraftforge.fml.ModList.get().isLoaded("goety")) event.accept(BOTTLING.get());
			if (net.minecraftforge.fml.ModList.get().isLoaded("create")) event.accept(COATING.get());
				if (net.minecraftforge.fml.ModList.get().isLoaded("create")) event.accept(MECHANICAL_ENGINE.get());
				if (net.minecraftforge.fml.ModList.get().isLoaded("create")) event.accept(MECHANICAL_FORCE.get());
				if (net.minecraftforge.fml.ModList.get().isLoaded("create")) event.accept(MECHANICAL_MOBILITY.get());
				event.accept(LORD.get());
			event.accept(SNOW_TRAIL.get());
			event.accept(SWAP.get());
			event.accept(BACKFLIP.get());
			event.accept(WINDMILL.get());
			event.accept(WITCH.get());
			if (net.minecraftforge.fml.ModList.get().isLoaded("twilightforest")) event.accept(PENGUIN.get());
				event.accept(QUICK_STRIKE.get());
				event.accept(ANGLER.get());
				event.accept(DEATH_KNELL.get());
				event.accept(ECHO_TRIO.get());
				event.accept(BRUSH.get());
				event.accept(BOMB_DISPOSAL.get());
				event.accept(PROJECTILE_DODGE.get());
			event.accept(BACKSTEP.get());
			event.accept(END_OF_BEGINNING.get());
			event.accept(CORONA.get());
			event.accept(MOON_SHADOW.get());
			event.accept(TIME_AXIS.get());
			}
		}
	}
}
