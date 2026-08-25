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
import src.toi_et_moi.mgdp.item.ConditionalUpgradeItem;
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
	public static final RegistryObject<SimpleUpgradeItem> GUARDIAN_LASER;
	public static final RegistryObject<SimpleUpgradeItem> FROST_BURST;
	public static final RegistryObject<SimpleUpgradeItem> TRUE_INVISIBILITY;
	public static final RegistryObject<SimpleUpgradeItem> ARMOR_PIERCE;
	public static final RegistryObject<SimpleUpgradeItem> MAGIC_RESISTANCE;
	public static final RegistryObject<SimpleUpgradeItem> DAMAGE_CAP;
	public static final RegistryObject<SimpleUpgradeItem> TOTEMIC;
	public static final RegistryObject<SimpleUpgradeItem> HERO;
	public static final RegistryObject<SimpleUpgradeItem> FLARE;
	public static final RegistryObject<SimpleUpgradeItem> UNDYING;
	public static final RegistryObject<SimpleUpgradeItem> GRENADE;
	public static final RegistryObject<SimpleUpgradeItem> KILLER_AURA;
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
	public static final RegistryObject<SimpleUpgradeItem> MIND_CONTROL;
	public static final RegistryObject<SimpleUpgradeItem> NECROMANCER;
	public static final RegistryObject<SimpleUpgradeItem> PHANTOM;
	public static final RegistryObject<SimpleUpgradeItem> LAST_LINE;
	public static final RegistryObject<SimpleUpgradeItem> REALITY_SUPPRESSION;
	public static final RegistryObject<SimpleUpgradeItem> MANA_OVERLOAD;
	public static final RegistryObject<SimpleUpgradeItem> THE_PYRE_LORD;
	public static final RegistryObject<SimpleUpgradeItem> THE_WITCH_KING;
	public static final RegistryObject<SimpleUpgradeItem> CREATIVE_SLOT_100;
	public static final RegistryObject<SimpleUpgradeItem> CREATIVE_SLOT;
	public static final RegistryObject<Item> HARBINGER_BEAM;
	public static final RegistryObject<Item> HARBINGER_MISSILE;
	public static final RegistryObject<Item> IGNIS_ATTACK;
	public static final RegistryObject<Item> IGNIS_FIREBALL;
	public static final RegistryObject<Item> IGNIS_JUMP;
	public static final RegistryObject<SimpleUpgradeItem> ANVIL_SLAM;
	public static final RegistryObject<SimpleUpgradeItem> IRON_UPGRADE;
	public static final RegistryObject<SimpleUpgradeItem> TRIDENT_FESTIVAL;
	public static final RegistryObject<SimpleUpgradeItem> RIPTIDE;
	public static final RegistryObject<SimpleUpgradeItem> END_VOID;
	public static final RegistryObject<SimpleUpgradeItem> ENCHANTED_TOTEMIC;
	public static final RegistryObject<SimpleUpgradeItem> SELF_DESTRUCT;
	public static final RegistryObject<SimpleUpgradeItem> PULLING;
	public static final RegistryObject<SimpleUpgradeItem> REPELLING;
	public static final RegistryObject<SimpleUpgradeItem> DEMENTOR;
	public static final RegistryObject<SimpleUpgradeItem> DRAIN;
	public static final RegistryObject<SimpleUpgradeItem> REPRINT;
	public static final RegistryObject<SimpleUpgradeItem> FIREBALL;
	public static final RegistryObject<SimpleUpgradeItem> BRUSH;
	public static final RegistryObject<SimpleUpgradeItem> BOMB_DISPOSAL;
	public static final RegistryObject<SimpleUpgradeItem> PROJECTILE_DODGE;
	public static final RegistryObject<SimpleUpgradeItem> CONQUEROR;
    public static final RegistryObject<SimpleUpgradeItem> SHIELD_BLOCK;
	public static final RegistryObject<SimpleUpgradeItem> BACKSTEP;
	public static final RegistryObject<SimpleUpgradeItem> ADAPTIVE;
	public static final RegistryObject<SimpleUpgradeItem> DISPELL;

	public static final RegistryObject<SimpleUpgradeItem> MECHANICAL_ENGINE;
	public static final RegistryObject<SimpleUpgradeItem> MECHANICAL_FORCE;
	public static final RegistryObject<SimpleUpgradeItem> MECHANICAL_MOBILITY;
	public static final RegistryObject<SimpleUpgradeItem> BLAST_FURNACE;
	public static final RegistryObject<SimpleUpgradeItem> FURNACE;
	public static final RegistryObject<SimpleUpgradeItem> MINER;
	public static final RegistryObject<SimpleUpgradeItem> SCAV_BOX;
	public static final RegistryObject<SimpleUpgradeItem> LUMBERJACK;

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
	public static final RegistryObject<SimpleUpgradeItem> VOID_ECHO;
	public static final RegistryObject<SimpleUpgradeItem> DISARM;
	public static final RegistryObject<SimpleUpgradeItem> CORONA;
	public static final RegistryObject<SimpleUpgradeItem> MOON_SHADOW;
	public static final RegistryObject<SimpleUpgradeItem> TIME_AXIS;
	public static final RegistryObject<SimpleUpgradeItem> UPSIDE_DOWN;
    public static final RegistryObject<SimpleUpgradeItem> THE_CRUEL;
    public static final RegistryObject<SimpleUpgradeItem> THE_GREAT_SHADOW;
    public static final RegistryObject<SimpleUpgradeItem> THE_DEFILER;
    public static final RegistryObject<SimpleUpgradeItem> THE_DARK;
    public static final RegistryObject<SimpleUpgradeItem> THE_GLORIOUS;
    public static final RegistryObject<SimpleUpgradeItem> THE_GENESIS;
    public static final RegistryObject<SimpleUpgradeItem> THE_APOCALYPSE;
	public static final RegistryObject<SimpleUpgradeItem> REVERSE;
	public static final RegistryObject<SimpleUpgradeItem> GHOST;
	public static final RegistryObject<SimpleUpgradeItem> SPYGLASS;
	public static final RegistryObject<SimpleUpgradeItem> SHRINK;

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

		UNDYING = net.minecraftforge.fml.ModList.get().isLoaded("l2hostility")
			? Mgdp.ITEMS.register("hostility_undying",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.UNDYING.get(), 1, false))
			: null;

		PULLING = net.minecraftforge.fml.ModList.get().isLoaded("l2hostility")
			? Mgdp.ITEMS.register("hostility_pulling",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.PULLING.get(), 1, false))
			: null;

		REPELLING = net.minecraftforge.fml.ModList.get().isLoaded("l2hostility")
			? Mgdp.ITEMS.register("hostility_repelling",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.REPELLING.get(), 1, false))
			: null;

		GRENADE = net.minecraftforge.fml.ModList.get().isLoaded("l2hostility")
			? Mgdp.ITEMS.register("hostility_grenade",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.GRENADE.get(), 1, false))
			: null;

		KILLER_AURA = net.minecraftforge.fml.ModList.get().isLoaded("l2hostility")
			? Mgdp.ITEMS.register("hostility_killer_aura",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.KILLER_AURA.get(), 1, false))
			: null;

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

		CRONE = net.minecraftforge.fml.ModList.get().isLoaded("goety")
			? Mgdp.ITEMS.register("crone",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.CRONE.get(), 1, false))
			: null;

		VOID_ECHO = net.minecraftforge.fml.ModList.get().isLoaded("goety")
			? Mgdp.ITEMS.register("void_echo",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.VOID_ECHO.get(), 1, true))
			: null;

		BOTTLING = net.minecraftforge.fml.ModList.get().isLoaded("goety")
			? Mgdp.ITEMS.register("bottling",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.BOTTLING.get(), 1, false))
			: null;


		PENGUIN = net.minecraftforge.fml.ModList.get().isLoaded("twilightforest")
			? Mgdp.ITEMS.register("penguin",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.PENGUIN.get(), 1, false))
			: null;

		LIQUID_CLEAR = Mgdp.ITEMS.register("liquid_clear",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.LIQUID_CLEAR.get(), 1, false));
		MAGIC_IMMUNE = Mgdp.ITEMS.register("magic_immune",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> dev.xkmc.modulargolems.init.registrate.GolemModifiers.MAGIC_IMMUNE.get(), 1, true));
		IRONWOOD = net.minecraftforge.fml.ModList.get().isLoaded("twilightforest")
			? Mgdp.ITEMS.register("ironwood",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> dev.xkmc.modulargolems.compat.materials.twilightforest.TFCompatRegistry.TF_HEALING.get(), 5, true))
			: null;

		STEELEAF = net.minecraftforge.fml.ModList.get().isLoaded("twilightforest")
			? Mgdp.ITEMS.register("steeleaf",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> dev.xkmc.modulargolems.compat.materials.twilightforest.TFCompatRegistry.TF_DAMAGE.get(), 5, true))
			: null;
		FIERY = net.minecraftforge.fml.ModList.get().isLoaded("twilightforest")
			? Mgdp.ITEMS.register("fiery",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> dev.xkmc.modulargolems.compat.materials.twilightforest.TFCompatRegistry.FIERY.get(), 5, true))
			: null;

		KNIGHTMETAL = net.minecraftforge.fml.ModList.get().isLoaded("twilightforest")
			? Mgdp.ITEMS.register("knightmetal",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> dev.xkmc.modulargolems.init.registrate.GolemModifiers.THORN.get(), 5, true))
			: null;
		CARMINITE = net.minecraftforge.fml.ModList.get().isLoaded("twilightforest")
			? Mgdp.ITEMS.register("carminite",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> dev.xkmc.modulargolems.compat.materials.twilightforest.TFCompatRegistry.CARMINITE.get(), 5, true))
			: null;

		COATING = net.minecraftforge.fml.ModList.get().isLoaded("create")
			? Mgdp.ITEMS.register("coating",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> dev.xkmc.modulargolems.compat.materials.create.CreateCompatRegistry.COATING.get(), 5, true))
			: null;






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

		ADAPTIVE = net.minecraftforge.fml.ModList.get().isLoaded("l2hostility")
				? Mgdp.ITEMS.register("hostility_adaptive",
					() -> new SimpleUpgradeItem(new Item.Properties(),
					() -> dev.xkmc.modulargolems.compat.materials.l2hostility.LHCompatRegistry.LH_ADAPTIVE.get(), 1, false))
				: null;

		DISPELL = net.minecraftforge.fml.ModList.get().isLoaded("l2hostility")
				? Mgdp.ITEMS.register("hostility_dispell",
					() -> new SimpleUpgradeItem(new Item.Properties(),
					() -> dev.xkmc.modulargolems.compat.materials.l2hostility.LHCompatRegistry.LH_DISPELL.get(), 1, false))
				: null;

		SELF_DESTRUCT = Mgdp.ITEMS.register("self_destruct",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.SELF_DESTRUCT.get(), 1, false));

		DEMENTOR = net.minecraftforge.fml.ModList.get().isLoaded("l2hostility")
				? Mgdp.ITEMS.register("hostility_dementor",
					() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.DEMENTOR.get(), 1, false))
				: null;

		DRAIN = net.minecraftforge.fml.ModList.get().isLoaded("l2hostility")
				? Mgdp.ITEMS.register("hostility_drain",
					() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.DRAIN.get(), 1, false))
				: null;

		REPRINT = net.minecraftforge.fml.ModList.get().isLoaded("l2hostility")
				? Mgdp.ITEMS.register("hostility_reprint",
					() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.REPRINT.get(), 1, false))
				: null;

		FIREBALL = Mgdp.ITEMS.register("fireball",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.FIREBALL.get(), 1, false));

		BRUSH = Mgdp.ITEMS.register("brush",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.BRUSH.get(), 1, false));

		BOMB_DISPOSAL = Mgdp.ITEMS.register("bomb_disposal",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.BOMB_DISPOSAL.get(), 1, false));

		BACKSTEP = Mgdp.ITEMS.register("backstep",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.BACKSTEP.get(), 1, false));

		CONQUEROR = Mgdp.ITEMS.register("conqueror",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.CONQUEROR.get(), 1, false));

		PROJECTILE_DODGE = Mgdp.ITEMS.register("projectile_dodge",
				() -> new SimpleUpgradeItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE).fireResistant(), () -> MGDPModifiers.PROJECTILE_DODGE.get(), 1, true));

		MECHANICAL_ENGINE = net.minecraftforge.fml.ModList.get().isLoaded("create")
				? Mgdp.ITEMS.register("mechanical_engine",
					() -> new SimpleUpgradeItem(new Item.Properties(),
					() -> dev.xkmc.modulargolems.compat.materials.create.CreateCompatRegistry.BODY.get(), 1, false))
				: null;

		MECHANICAL_FORCE = net.minecraftforge.fml.ModList.get().isLoaded("create")
				? Mgdp.ITEMS.register("mechanical_force",
					() -> new SimpleUpgradeItem(new Item.Properties(),
					() -> dev.xkmc.modulargolems.compat.materials.create.CreateCompatRegistry.FORCE.get(), 1, false))
				: null;

		MECHANICAL_MOBILITY = net.minecraftforge.fml.ModList.get().isLoaded("create")
				? Mgdp.ITEMS.register("mechanical_mobility",
					() -> new SimpleUpgradeItem(new Item.Properties(),
					() -> dev.xkmc.modulargolems.compat.materials.create.CreateCompatRegistry.MOBILE.get(), 1, false))
				: null;

		BLAST_FURNACE = Mgdp.ITEMS.register("blast_furnace",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.BLAST_FURNACE.get(), 1, false));

		FURNACE = Mgdp.ITEMS.register("furnace",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.FURNACE.get(), 1, false));

		MINER = Mgdp.ITEMS.register("mine",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.MINER.get(), 1, false));

		SCAV_BOX = Mgdp.ITEMS.register("scav_box",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.SCAV_BOX.get(), 1, false));

		LUMBERJACK = Mgdp.ITEMS.register("lumberjack",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.LUMBERJACK.get(), 1, false));


			REMNANT_GOLEM = net.minecraftforge.fml.ModList.get().isLoaded("golemdungeons")
				? Mgdp.ITEMS.register("remnant_golem",
					() -> new GolemSummonItem(new Item.Properties().stacksTo(1).defaultDurability(20).rarity(net.minecraft.world.item.Rarity.EPIC),
							new ResourceLocation("golemdungeons", "factory_remnant")))
				: null;

			ILLAGER_GOLEM = net.minecraftforge.fml.ModList.get().isLoaded("golemdungeons")
				? Mgdp.ITEMS.register("illager_golem",
					() -> new GolemSummonItem(new Item.Properties().stacksTo(1).defaultDurability(20).rarity(net.minecraft.world.item.Rarity.EPIC),
							new ResourceLocation("golemdungeons", "illagers_creation")))
				: null;

			PIGLIN_GOLEM = net.minecraftforge.fml.ModList.get().isLoaded("golemdungeons")
				? Mgdp.ITEMS.register("piglin_golem",
					() -> new GolemSummonItem(new Item.Properties().stacksTo(1).defaultDurability(20).rarity(net.minecraft.world.item.Rarity.EPIC),
							new ResourceLocation("golemdungeons", "piglin_legacy")))
				: null;

			SCULK_GOLEM = net.minecraftforge.fml.ModList.get().isLoaded("golemdungeons")
				? Mgdp.ITEMS.register("sculk_golem",
					() -> new GolemSummonItem(new Item.Properties().stacksTo(1).defaultDurability(20).rarity(net.minecraft.world.item.Rarity.EPIC),
							new ResourceLocation("golemdungeons", "sculk_infestation")))
				: null;

			TWILIGHT_GOLEM = net.minecraftforge.fml.ModList.get().isLoaded("twilightforest")
				? Mgdp.ITEMS.register("twilight_golem",
					() -> new GolemSummonItem(new Item.Properties().stacksTo(1).defaultDurability(20).rarity(net.minecraft.world.item.Rarity.EPIC),
							new ResourceLocation("twilightforest", "twilight_invasion")))
				: null;

		HARBINGER_GOLEM = net.minecraftforge.fml.ModList.get().isLoaded("cataclysm")
				? Mgdp.ITEMS.register("harbinger_golem",
					() -> new GolemSummonItem(new Item.Properties().stacksTo(1).defaultDurability(4).rarity(net.minecraft.world.item.Rarity.EPIC),
							new ResourceLocation("cataclysm", "harbingers_revenge")))
				: null;

		MONSTROSITY_GOLEM = net.minecraftforge.fml.ModList.get().isLoaded("cataclysm")
				? Mgdp.ITEMS.register("monstrosity_golem",
					() -> new GolemSummonItem(new Item.Properties().stacksTo(1).defaultDurability(4).rarity(net.minecraft.world.item.Rarity.EPIC),
							new ResourceLocation("cataclysm", "monstrosity_expanded")))
				: null;

		ENDER_GUARDIAN_GOLEM = net.minecraftforge.fml.ModList.get().isLoaded("cataclysm")
				? Mgdp.ITEMS.register("ender_guardian_golem",
					() -> new GolemSummonItem(new Item.Properties().stacksTo(1).defaultDurability(4).rarity(net.minecraft.world.item.Rarity.EPIC),
							new ResourceLocation("cataclysm", "meknight_of_the_end")))
				: null;

		IGNIS_GOLEM = net.minecraftforge.fml.ModList.get().isLoaded("cataclysm")
				? Mgdp.ITEMS.register("ignis_golem",
					() -> new GolemSummonItem(new Item.Properties().stacksTo(1).defaultDurability(4).rarity(net.minecraft.world.item.Rarity.EPIC),
							new ResourceLocation("cataclysm", "resurgent_flame")))
				: null;

		SCYLLA_GOLEM = net.minecraftforge.fml.ModList.get().isLoaded("cataclysm")
				? Mgdp.ITEMS.register("scylla_golem",
					() -> new GolemSummonItem(new Item.Properties().stacksTo(1).defaultDurability(4).rarity(net.minecraft.world.item.Rarity.EPIC),
							new ResourceLocation("cataclysm", "heavenly_storm")))
				: null;

		CARVED_GOLEM = net.minecraftforge.fml.ModList.get().isLoaded("jerotes_village_golems")
			? Mgdp.ITEMS.register("carved_golem",
				() -> new GolemSummonItem(new Item.Properties().stacksTo(1).defaultDurability(20).rarity(net.minecraft.world.item.Rarity.EPIC),
					new ResourceLocation("jerotes_village_golems", "villager_metal")))
			: null;

		ENHANCED_CARVED_GOLEM = net.minecraftforge.fml.ModList.get().isLoaded("jerotes_village_golems")
			? Mgdp.ITEMS.register("enhanced_carved_golem",
				() -> new GolemSummonItem(new Item.Properties().stacksTo(1).defaultDurability(20).rarity(net.minecraft.world.item.Rarity.EPIC),
					new ResourceLocation("jerotes_village_golems", "enhanced_villager_metal")))
			: null;

		QOAIKU_GOLEM = net.minecraftforge.fml.ModList.get().isLoaded("jerotes_village_golems")
			? Mgdp.ITEMS.register("qoaiku_golem",
				() -> new GolemSummonItem(new Item.Properties().stacksTo(1).defaultDurability(20).rarity(net.minecraft.world.item.Rarity.EPIC),
					new ResourceLocation("jerotes_village_golems", "qoaiku")))
			: null;

		MEROR_GOLEM = net.minecraftforge.fml.ModList.get().isLoaded("jerotes_village_golems")
			? Mgdp.ITEMS.register("meror_golem",
				() -> new GolemSummonItem(new Item.Properties().stacksTo(1).defaultDurability(20).rarity(net.minecraft.world.item.Rarity.EPIC),
					new ResourceLocation("jerotes_village_golems", "meror")))
			: null;

		REFINE_MEROR_GOLEM = net.minecraftforge.fml.ModList.get().isLoaded("jerotes_village_golems")
			? Mgdp.ITEMS.register("refine_meror_golem",
				() -> new GolemSummonItem(new Item.Properties().stacksTo(1).defaultDurability(20).rarity(net.minecraft.world.item.Rarity.EPIC),
					new ResourceLocation("jerotes_village_golems", "refine_meror")))
			: null;

		CATACLYSMFARMER_TEMPLATE = net.minecraftforge.fml.ModList.get().isLoaded("cataclysm")
			? Mgdp.ITEMS.register("cataclysmfarer_expansion_template",
				() -> new AddSlotTemplate(new Item.Properties(), () -> MGDPModifiers.CATACLYSMFARMER_ADD.get()))
			: null;

		DARK_TEMPLATE = net.minecraftforge.fml.ModList.get().isLoaded("goety")
			? Mgdp.ITEMS.register("dark_expansion_template",
				() -> new AddSlotTemplate(new Item.Properties(), () -> MGDPModifiers.DARK_ADD.get()))
			: null;

		PYRIUM_TEMPLATE = net.minecraftforge.fml.ModList.get().isLoaded("irons_spellbooks")
			? Mgdp.ITEMS.register("pyrium_expansion_template",
				() -> new AddSlotTemplate(new Item.Properties(), () -> MGDPModifiers.PYRIUM_ADD.get()))
			: null;

		SCULKIUM_TEMPLATE = net.minecraftforge.fml.ModList.get().isLoaded("l2complements")
			? Mgdp.ITEMS.register("sculkium_expansion_template",
				() -> new AddSlotTemplate(new Item.Properties(), () -> MGDPModifiers.SCULKIUM_ADD.get()))
			: null;
		MEROR_TEMPLATE = net.minecraftforge.fml.ModList.get().isLoaded("jerotes_village_golems")
			? Mgdp.ITEMS.register("meror_expansion_template",
				() -> new AddSlotTemplate(new Item.Properties(), () -> MGDPModifiers.MEROR_ADD.get()))
			: null;

		REFINE_MEROR_TEMPLATE = net.minecraftforge.fml.ModList.get().isLoaded("jerotes_village_golems")
			? Mgdp.ITEMS.register("refine_meror_expansion_template",
				() -> new AddSlotTemplate(new Item.Properties(), () -> MGDPModifiers.REFINE_MEROR_ADD.get()))
			: null;


		CREATIVE_SLOT_100 = Mgdp.ITEMS.register("creative_slot_100",
			() -> new SimpleUpgradeItem(new Item.Properties().stacksTo(64), () -> MGDPModifiers.CREATIVE_SLOT_100.get(), 1, false));

		CREATIVE_SLOT = Mgdp.ITEMS.register("creative_slot",
			() -> new SimpleUpgradeItem(new Item.Properties().stacksTo(64), () -> MGDPModifiers.CREATIVE_SLOT.get(), 1, false));

		MIND_CONTROL = Mgdp.ITEMS.register("mind_control",
			() -> new SimpleUpgradeItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC), () -> MGDPModifiers.MIND_CONTROL.get(), 1, false));

		PHANTOM = net.minecraftforge.fml.ModList.get().isLoaded("youkaishomecoming")
			? Mgdp.ITEMS.register("phantom",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.PHANTOM.get(), 1, false))
			: null;

		NECROMANCER = net.minecraftforge.fml.ModList.get().isLoaded("goety")
			? Mgdp.ITEMS.register("necromancer",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.NECROMANCER.get(), 1, false))
			: null;

		LAST_LINE = net.minecraftforge.fml.ModList.get().isLoaded("twilightforest")
			? Mgdp.ITEMS.register("last_line",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.LAST_LINE.get(), 1, false))
			: null;

		REALITY_SUPPRESSION = net.minecraftforge.fml.ModList.get().isLoaded("curseofpandora")
			? Mgdp.ITEMS.register("reality_suppression",
				() -> new SimpleUpgradeItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC), () -> MGDPModifiers.REALITY_SUPPRESSION.get(), 1, false))
			: null;

		MANA_OVERLOAD = net.minecraftforge.fml.ModList.get().isLoaded("golemmagicka")
			? Mgdp.ITEMS.register("mana_overload",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.MANA_OVERLOAD.get(), 1, false))
			: null;


		THE_PYRE_LORD = net.minecraftforge.fml.ModList.get().isLoaded("goety_revelation")
			? Mgdp.ITEMS.register("the_pyre_lord",
				() -> new SimpleUpgradeItem(new Item.Properties().rarity(Rarity.EPIC), () -> MGDPModifiers.THE_PYRE_LORD.get(), 1, false))
			: null;

		THE_WITCH_KING = net.minecraftforge.fml.ModList.get().isLoaded("goety_revelation")
			? Mgdp.ITEMS.register("the_witch_king",
				() -> new SimpleUpgradeItem(new Item.Properties().rarity(Rarity.EPIC), () -> MGDPModifiers.THE_WITCH_KING.get(), 1, false))
			: null;

        THE_CRUEL = net.minecraftforge.fml.ModList.get().isLoaded("goety_revelation")
                ? Mgdp.ITEMS.register("the_cruel",
                        () -> new SimpleUpgradeItem(new Item.Properties().rarity(Rarity.EPIC), () -> MGDPModifiers.THE_CRUEL.get(), 1, false))
                : null;

        THE_GREAT_SHADOW = net.minecraftforge.fml.ModList.get().isLoaded("goety_revelation")
                ? Mgdp.ITEMS.register("the_great_shadow",
                        () -> new SimpleUpgradeItem(new Item.Properties().rarity(Rarity.EPIC), () -> MGDPModifiers.THE_GREAT_SHADOW.get(), 1, false))
                : null;

        THE_DEFILER = net.minecraftforge.fml.ModList.get().isLoaded("goety_revelation")
                ? Mgdp.ITEMS.register("the_defiler",
                        () -> new SimpleUpgradeItem(new Item.Properties().rarity(Rarity.EPIC), () -> MGDPModifiers.THE_DEFILER.get(), 1, false))
                : null;

        THE_DARK = net.minecraftforge.fml.ModList.get().isLoaded("goety_revelation")
                ? Mgdp.ITEMS.register("the_dark",
                        () -> new SimpleUpgradeItem(new Item.Properties().rarity(Rarity.EPIC), () -> MGDPModifiers.THE_DARK.get(), 1, false))
                : null;

        THE_GLORIOUS = net.minecraftforge.fml.ModList.get().isLoaded("goety_revelation")
                ? Mgdp.ITEMS.register("the_glorious",
                        () -> new SimpleUpgradeItem(new Item.Properties().rarity(Rarity.EPIC), () -> MGDPModifiers.THE_GLORIOUS.get(), 1, false))
                : null;

        THE_GENESIS = net.minecraftforge.fml.ModList.get().isLoaded("goety_revelation")
                ? Mgdp.ITEMS.register("the_genesis",
                        () -> new SimpleUpgradeItem(new Item.Properties().rarity(Rarity.EPIC), () -> MGDPModifiers.THE_GENESIS.get(), 1, false))
                : null;

        THE_APOCALYPSE = net.minecraftforge.fml.ModList.get().isLoaded("goety_revelation")
                ? Mgdp.ITEMS.register("the_apocalypse",
                        () -> new SimpleUpgradeItem(new Item.Properties().rarity(Rarity.EPIC), () -> MGDPModifiers.THE_APOCALYPSE.get(), 1, false))
                : null;

		HARBINGER_BEAM = net.minecraftforge.fml.ModList.get().isLoaded("cataclysm")
			? Mgdp.ITEMS.register("harbinger_beam",
				() -> new ConditionalUpgradeItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC),
					() -> dev.xkmc.modulargolems.compat.materials.cataclysm.CataCompatRegistry.HARBINGER_BEAM.get(), 1, false))
			: null;

		HARBINGER_MISSILE = net.minecraftforge.fml.ModList.get().isLoaded("cataclysm")
			? Mgdp.ITEMS.register("harbinger_missile",
				() -> new ConditionalUpgradeItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC),
					() -> dev.xkmc.modulargolems.compat.materials.cataclysm.CataCompatRegistry.HARBINGER_MISSILE.get(), 1, false))
			: null;

		IGNIS_ATTACK = net.minecraftforge.fml.ModList.get().isLoaded("cataclysm")
			? Mgdp.ITEMS.register("ignis_attack",
				() -> new ConditionalUpgradeItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC),
					() -> dev.xkmc.modulargolems.compat.materials.cataclysm.CataCompatRegistry.IGNIS_ATTACK.get(), 1, false))
			: null;

		IGNIS_FIREBALL = net.minecraftforge.fml.ModList.get().isLoaded("cataclysm")
			? Mgdp.ITEMS.register("ignis_fireball",
				() -> new ConditionalUpgradeItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC),
					() -> dev.xkmc.modulargolems.compat.materials.cataclysm.CataCompatRegistry.IGNIS_FIREBALL.get(), 1, false))
			: null;

		IGNIS_JUMP = net.minecraftforge.fml.ModList.get().isLoaded("cataclysm")
			? Mgdp.ITEMS.register("ignis_jump",
				() -> new ConditionalUpgradeItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC),
					() -> dev.xkmc.modulargolems.compat.materials.cataclysm.CataCompatRegistry.IGNIS_JUMP.get(), 1, false))
			: null;


		GUARDIAN_LASER = Mgdp.ITEMS.register("guardian_laser",
			() -> new SimpleUpgradeItem(new Item.Properties().rarity(Rarity.UNCOMMON), () -> MGDPModifiers.GUARDIAN_LASER.get(), 1, false));

		FROST_BURST = net.minecraftforge.fml.ModList.get().isLoaded("smc")
			? Mgdp.ITEMS.register("frost_burst",
				() -> new SimpleUpgradeItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE), () -> MGDPModifiers.FROST_BURST.get(), 1, false))
			: null;

			TRUE_INVISIBILITY = net.minecraftforge.fml.ModList.get().isLoaded("irons_spellbooks")
				? Mgdp.ITEMS.register("true_invisibility",
					() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.TRUE_INVISIBILITY.get(), 1, false))
				: null;

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

		CONDUIT = Mgdp.ITEMS.register("heart_of_the_sea",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.CONDUIT.get(), 1, false));

		HYPOTHERMIA = Mgdp.ITEMS.register("hypothermia",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.HYPOTHERMIA.get(), 1, false));

		VERSATILITY = Mgdp.ITEMS.register("versatility",
				() -> new SimpleUpgradeItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC), () -> MGDPModifiers.VERSATILITY.get(), 1, true));

		DISARM = Mgdp.ITEMS.register("disarm",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.DISARM.get(), 1, false));

		END_OF_BEGINNING = Mgdp.ITEMS.register("end_of_beginning",
			() -> new SimpleUpgradeItem(new Item.Properties().rarity(Rarity.EPIC), () -> MGDPModifiers.END_OF_BEGINNING.get(), 1, true));

		CORONA = Mgdp.ITEMS.register("corona",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.CORONA.get(), 1, false));

		MOON_SHADOW = Mgdp.ITEMS.register("moon_shadow",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.MOON_SHADOW.get(), 1, false));

		TIME_AXIS = Mgdp.ITEMS.register("time_axis",
			() -> new SimpleUpgradeItem(new Item.Properties().rarity(Rarity.EPIC), () -> MGDPModifiers.TIME_AXIS.get(), 1, true));

		UPSIDE_DOWN = Mgdp.ITEMS.register("upside_down",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.UPSIDE_DOWN.get(), 1, false));

		REVERSE = Mgdp.ITEMS.register("reverse",
			() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.REVERSE.get(), 1, false));

			GHOST = Mgdp.ITEMS.register("ghost",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.GHOST.get(), 1, false));

			SPYGLASS = Mgdp.ITEMS.register("spyglass",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.SPYGLASS.get(), 1, false));

			SHRINK = Mgdp.ITEMS.register("shrink",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.SHRINK.get(), 1, false));
			SHIELD_BLOCK = Mgdp.ITEMS.register("shield_block",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.SHIELD_BLOCK.get(), 1, false));

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
			if (CATACLYSMFARMER_TEMPLATE != null) event.accept(CATACLYSMFARMER_TEMPLATE.get());
		if (HARBINGER_BEAM != null) event.accept(HARBINGER_BEAM.get());
		if (HARBINGER_MISSILE != null) event.accept(HARBINGER_MISSILE.get());
		if (IGNIS_ATTACK != null) event.accept(IGNIS_ATTACK.get());
		if (IGNIS_FIREBALL != null) event.accept(IGNIS_FIREBALL.get());
		if (IGNIS_JUMP != null) event.accept(IGNIS_JUMP.get());
        if (DARK_TEMPLATE != null) event.accept(DARK_TEMPLATE.get());
        if (PYRIUM_TEMPLATE != null) event.accept(PYRIUM_TEMPLATE.get());
        if (SCULKIUM_TEMPLATE != null) event.accept(SCULKIUM_TEMPLATE.get());
        if (MEROR_TEMPLATE != null) event.accept(MEROR_TEMPLATE.get());
        if (REFINE_MEROR_TEMPLATE != null) event.accept(REFINE_MEROR_TEMPLATE.get());
        event.accept(GUARDIAN_LASER.get());
        if (FROST_BURST != null) event.accept(FROST_BURST.get());
				event.accept(ARMOR_PIERCE.get());
				event.accept(MAGIC_RESISTANCE.get());
				event.accept(VERSATILITY.get());
				event.accept(DAMAGE_CAP.get());
				event.accept(TOTEMIC.get());
				event.accept(ENCHANTED_TOTEMIC.get());
				if (ADAPTIVE != null) event.accept(ADAPTIVE.get());
				if (DISPELL != null) event.accept(DISPELL.get());
				if (PULLING != null) event.accept(PULLING.get());
				if (REPELLING != null) event.accept(REPELLING.get());
				if (DEMENTOR != null) event.accept(DEMENTOR.get());
				if (DRAIN != null) event.accept(DRAIN.get());
				if (REPRINT != null) event.accept(REPRINT.get());
				if (GRENADE != null) event.accept(GRENADE.get());
				if (KILLER_AURA != null) event.accept(KILLER_AURA.get());
				if (UNDYING != null) event.accept(UNDYING.get());
				event.accept(SELF_DESTRUCT.get());
				event.accept(FIREBALL.get());
				event.accept(HERO.get());
				event.accept(FLARE.get());
				event.accept(UNBREAKABLE.get());
				event.accept(BLAST_FURNACE.get());
				event.accept(FURNACE.get());
				event.accept(MINER.get());
				event.accept(SCAV_BOX.get());
				event.accept(LUMBERJACK.get());
				event.accept(ANVIL_SLAM.get());
			event.accept(TRIDENT_FESTIVAL.get());
			event.accept(IRON_UPGRADE.get());
			if (THE_PYRE_LORD != null) event.accept(THE_PYRE_LORD.get());
			if (THE_WITCH_KING != null) event.accept(THE_WITCH_KING.get());
	        if (THE_CRUEL != null) event.accept(THE_CRUEL.get());
	        if (THE_GREAT_SHADOW != null) event.accept(THE_GREAT_SHADOW.get());
	        if (THE_DEFILER != null) event.accept(THE_DEFILER.get());
	        if (THE_DARK != null) event.accept(THE_DARK.get());
	        if (THE_GLORIOUS != null) event.accept(THE_GLORIOUS.get());
	        if (THE_GENESIS != null) event.accept(THE_GENESIS.get());
	        if (THE_APOCALYPSE != null) event.accept(THE_APOCALYPSE.get());
				event.accept(RIPTIDE.get());
			event.accept(END_VOID.get());
				event.accept(INFINITE_AMMO.get());
				event.accept(PROSPERITY.get());
				event.accept(LIQUID_CLEAR.get());
			event.accept(MAGIC_IMMUNE.get());
			if (IRONWOOD != null) event.accept(IRONWOOD.get());
			if (STEELEAF != null) event.accept(STEELEAF.get());
			if (FIERY != null) event.accept(FIERY.get());
			if (KNIGHTMETAL != null) event.accept(KNIGHTMETAL.get());
			if (CARMINITE != null) event.accept(CARMINITE.get());
			if (CRONE != null) event.accept(CRONE.get());
			if (BOTTLING != null) event.accept(BOTTLING.get());
			if (VOID_ECHO != null) event.accept(VOID_ECHO.get());
			if (COATING != null) event.accept(COATING.get());
				if (MECHANICAL_ENGINE != null) event.accept(MECHANICAL_ENGINE.get());
				if (MECHANICAL_FORCE != null) event.accept(MECHANICAL_FORCE.get());
				if (MECHANICAL_MOBILITY != null) event.accept(MECHANICAL_MOBILITY.get());
				event.accept(LORD.get());
			event.accept(SNOW_TRAIL.get());
			event.accept(SWAP.get());
			event.accept(BACKFLIP.get());
			event.accept(WINDMILL.get());
			event.accept(WITCH.get());
			if (PENGUIN != null) event.accept(PENGUIN.get());
				event.accept(QUICK_STRIKE.get());
				event.accept(ANGLER.get());
				event.accept(DEATH_KNELL.get());
				event.accept(ECHO_TRIO.get());
				event.accept(MIND_CONTROL.get());
				if (PHANTOM != null) event.accept(PHANTOM.get());
				event.accept(CREATIVE_SLOT.get());
				event.accept(CREATIVE_SLOT_100.get());
				event.accept(BRUSH.get());
				event.accept(BOMB_DISPOSAL.get());
				event.accept(PROJECTILE_DODGE.get());
			event.accept(SHIELD_BLOCK.get());
				event.accept(CONQUEROR.get());
			event.accept(BACKSTEP.get());
			event.accept(END_OF_BEGINNING.get());
			event.accept(DISARM.get());
			event.accept(CORONA.get());
			event.accept(MOON_SHADOW.get());
			event.accept(TIME_AXIS.get());
			event.accept(UPSIDE_DOWN.get());
			event.accept(REVERSE.get());
			event.accept(GHOST.get());
			event.accept(SPYGLASS.get());
			event.accept(SHRINK.get());
			}
		}
	}
}
