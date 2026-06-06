package src.toi_et_moi.mgdp.init;

import dev.xkmc.modulargolems.content.item.upgrade.SimpleUpgradeItem;
import dev.xkmc.modulargolems.init.registrate.GolemItems;
import dev.xkmc.modulargolems.init.registrate.GolemModifiers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import java.util.List;
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
	public static final RegistryObject<SimpleUpgradeItem> QUICK_STRIKE;
	public static final RegistryObject<SimpleUpgradeItem> ANGLER;
	public static final RegistryObject<SimpleUpgradeItem> DEATH_KNELL;
	public static final RegistryObject<SimpleUpgradeItem> ECHO_TRIO;
	public static final RegistryObject<SimpleUpgradeItem> ENCHANTED_TOTEMIC;
	public static final RegistryObject<SimpleUpgradeItem> DEMENTOR;
	public static final RegistryObject<SimpleUpgradeItem> DRAIN;
	public static final RegistryObject<SimpleUpgradeItem> REPRINT;
	public static final RegistryObject<SimpleUpgradeItem> FIREBALL;
	public static final RegistryObject<SimpleUpgradeItem> BRUSH;
	public static final RegistryObject<SimpleUpgradeItem> BOMB_DISPOSAL;
	public static final RegistryObject<SimpleUpgradeItem> PROJECTILE_DODGE;
	public static final RegistryObject<SimpleUpgradeItem> ADAPTIVE;
	public static final RegistryObject<SimpleUpgradeItem> DISPELL;

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

	static {
		HARVEST_CROP = Mgdp.ITEMS.register("harvest_crop",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.HARVEST_CROP.get(), 1, false));

		FLIGHT = Mgdp.ITEMS.register("flight",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.FLIGHT.get(), 1, true));

		POTION_AURA = Mgdp.ITEMS.register("potion_aura",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.POTION_AURA.get(), 1, true));

		REBIRTH = Mgdp.ITEMS.register("rebirth",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.REBIRTH.get(), 1, true));

		UNSTOPPABLE = Mgdp.ITEMS.register("unstoppable",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.UNSTOPPABLE.get(), 1, true));

		SPIRIT = Mgdp.ITEMS.register("spirit",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.SPIRIT.get(), 1, true));

		NETHERITE_GOLD = Mgdp.ITEMS.register("netherite_gold",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.NETHERITE_GOLD.get(), 1, false));

		ENCHANTED_NETHERITE_GOLD = Mgdp.ITEMS.register("enchanted_netherite_gold",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.ENCHANTED_NETHERITE_GOLD.get(), 1, true));

		BELL_OF_AVICI = Mgdp.ITEMS.register("bell_of_avici",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.BELL_OF_AVICI.get(), 1, true));

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
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.WITHER_EXTINCTION.get(), 1, true));

		CHARGED_SHIELD = Mgdp.ITEMS.register("charged_shield",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.CHARGED_SHIELD.get(), 1, false));
		ARMOR_PIERCE = Mgdp.ITEMS.register("armor_pierce",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> dev.xkmc.modulargolems.init.registrate.GolemModifiers.ARMOR_BYPASS.get(), 1, false));

		MAGIC_RESISTANCE = Mgdp.ITEMS.register("magic_resistance",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> dev.xkmc.modulargolems.init.registrate.GolemModifiers.MAGIC_RES.get(), 1, false));

		DAMAGE_CAP = Mgdp.ITEMS.register("damage_cap",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> dev.xkmc.modulargolems.init.registrate.GolemModifiers.DAMAGE_CAP.get(), 1, true));

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

		QUICK_STRIKE = Mgdp.ITEMS.register("quick_strike",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.QUICK_STRIKE.get(), 1, false));

		ANGLER = Mgdp.ITEMS.register("angler",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.ANGLER.get(), 1, false));

		DEATH_KNELL = Mgdp.ITEMS.register("death_knell",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.DEATH_KNELL.get(), 1, true));

		ECHO_TRIO = Mgdp.ITEMS.register("echo_trio",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.ECHO_TRIO.get(), 1, true));

		ADAPTIVE = Mgdp.ITEMS.register("hostility_adaptive",
				() -> new SimpleUpgradeItem(new Item.Properties(),
				() -> net.minecraftforge.fml.ModList.get().isLoaded("l2hostility") ? dev.xkmc.modulargolems.compat.materials.l2hostility.LHCompatRegistry.LH_ADAPTIVE.get() : null, 1, false));

		DISPELL = Mgdp.ITEMS.register("hostility_dispell",
				() -> new SimpleUpgradeItem(new Item.Properties(),
				() -> net.minecraftforge.fml.ModList.get().isLoaded("l2hostility") ? dev.xkmc.modulargolems.compat.materials.l2hostility.LHCompatRegistry.LH_DISPELL.get() : null, 1, false));

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

		PROJECTILE_DODGE = Mgdp.ITEMS.register("projectile_dodge",
				() -> new SimpleUpgradeItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE).fireResistant(), () -> MGDPModifiers.PROJECTILE_DODGE.get(), 1, true));


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

		TRUE_INVISIBILITY = Mgdp.ITEMS.register("true_invisibility",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.TRUE_INVISIBILITY.get(), 1, false));

		INVISIBILITY = Mgdp.ITEMS.register("invisibility",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.INVISIBILITY.get(), 1, false));

		EXECUTIONER = Mgdp.ITEMS.register("executioner",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.EXECUTIONER.get(), 1, true));

		FOCUSED_DEFENSE = Mgdp.ITEMS.register("focused_defense",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.FOCUSED_DEFENSE.get(), 1, false));

		SONIC_BOOM = Mgdp.ITEMS.register("sonic_boom",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.SONIC_BOOM.get(), 1, false));

		SELF_REPAIR = Mgdp.ITEMS.register("self_repair",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.SELF_REPAIR.get(), 1, false));

		HYPOTHERMIA = Mgdp.ITEMS.register("hypothermia",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.HYPOTHERMIA.get(), 1, false));

		VERSATILITY = Mgdp.ITEMS.register("versatility",
				() -> new SimpleUpgradeItem(new Item.Properties(), () -> MGDPModifiers.VERSATILITY.get(), 1, true));
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
				event.accept(HYPOTHERMIA.get());
				event.accept(SELF_REPAIR.get());
				event.accept(SONIC_BOOM.get());
				event.accept(FOCUSED_DEFENSE.get());
				event.accept(EXECUTIONER.get());
				event.accept(INVISIBILITY.get());
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
				event.accept(FIREBALL.get());
				event.accept(HERO.get());
				event.accept(FLARE.get());
				event.accept(UNBREAKABLE.get());
				event.accept(INFINITE_AMMO.get());
				event.accept(QUICK_STRIKE.get());
				event.accept(ANGLER.get());
				event.accept(DEATH_KNELL.get());
				event.accept(ECHO_TRIO.get());
				event.accept(BRUSH.get());
				event.accept(BOMB_DISPOSAL.get());
				event.accept(PROJECTILE_DODGE.get());
			}
		}
	}
}
