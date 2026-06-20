package src.toi_et_moi.mgdp.init;

import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.xkmc.modulargolems.content.modifier.base.PotionDefenseModifier;
import net.minecraft.world.effect.MobEffects;
import src.toi_et_moi.mgdp.modifier.movement.FlightModifier;
import src.toi_et_moi.mgdp.modifier.farming.HarvestCropModifier;
import src.toi_et_moi.mgdp.modifier.special.PotionAuraModifier;
import src.toi_et_moi.mgdp.modifier.buff.RebirthModifier;
import src.toi_et_moi.mgdp.modifier.buff.EnchantedNetheriteGoldModifier;
import src.toi_et_moi.mgdp.modifier.buff.NetheriteGoldModifier;
import src.toi_et_moi.mgdp.modifier.movement.SpiritModifier;
import src.toi_et_moi.mgdp.modifier.special.BellOfAviciModifier;
import src.toi_et_moi.mgdp.modifier.defense.ChargedShieldModifier;
import src.toi_et_moi.mgdp.modifier.combat.CrimsonAttackModifier;
import src.toi_et_moi.mgdp.modifier.combat.DiamondAttackModifier;
import src.toi_et_moi.mgdp.modifier.combat.DragonBreathModifier;
import src.toi_et_moi.mgdp.modifier.combat.EnchantedCrimsonAttackModifier;
import src.toi_et_moi.mgdp.modifier.combat.WitherExtinctionModifier;
import src.toi_et_moi.mgdp.modifier.combat.EnchantedDiamondAttackModifier;
import src.toi_et_moi.mgdp.modifier.combat.LightningStormModifier;
import src.toi_et_moi.mgdp.modifier.movement.RocketFlightModifier;
import src.toi_et_moi.mgdp.modifier.movement.UnstoppableModifier;
import src.toi_et_moi.mgdp.modifier.defense.HypothermiaModifier;
import src.toi_et_moi.mgdp.modifier.defense.OverworldModifier;
import src.toi_et_moi.mgdp.modifier.defense.NetherModifier;
import src.toi_et_moi.mgdp.modifier.defense.ConduitModifier;
import src.toi_et_moi.mgdp.modifier.defense.SunlightModifier;
import src.toi_et_moi.mgdp.modifier.buff.SelfRepairModifier;
import src.toi_et_moi.mgdp.modifier.combat.ExecutionerModifier;
import src.toi_et_moi.mgdp.modifier.defense.FocusedDefenseModifier;
import src.toi_et_moi.mgdp.modifier.combat.SonicBoomModifier;
import src.toi_et_moi.mgdp.modifier.special.VersatilityModifier;
import src.toi_et_moi.mgdp.modifier.defense.ProjectileDodgeModifier;
import src.toi_et_moi.mgdp.modifier.defense.BackstepModifier;
import src.toi_et_moi.mgdp.modifier.hostility.DementorModifier;
import src.toi_et_moi.mgdp.modifier.hostility.DrainModifier;
import src.toi_et_moi.mgdp.modifier.hostility.ReprintModifier;
import src.toi_et_moi.mgdp.modifier.hostility.SelfDestructModifier;
import src.toi_et_moi.mgdp.modifier.farming.BrushModifier;
import src.toi_et_moi.mgdp.modifier.defense.BombDisposalModifier;
import src.toi_et_moi.mgdp.modifier.combat.FireballModifier;
import src.toi_et_moi.mgdp.modifier.buff.TotemicModifier;
import src.toi_et_moi.mgdp.modifier.farming.HeroModifier;
import src.toi_et_moi.mgdp.modifier.farming.FlareModifier;
import src.toi_et_moi.mgdp.modifier.farming.BlastFurnaceModifier;
import src.toi_et_moi.mgdp.modifier.farming.AnglerModifier;
import src.toi_et_moi.mgdp.modifier.hostility.UndyingModifier;
import src.toi_et_moi.mgdp.modifier.hostility.GrenadeModifier;
import src.toi_et_moi.mgdp.modifier.defense.UnbreakableModifier;
import src.toi_et_moi.mgdp.modifier.defense.InfiniteAmmoModifier;
import src.toi_et_moi.mgdp.modifier.combat.QuickStrikeModifier;
import src.toi_et_moi.mgdp.modifier.combat.DeathKnellModifier;
import src.toi_et_moi.mgdp.modifier.combat.EchoTrioModifier;
import src.toi_et_moi.mgdp.modifier.combat.AnvilSlamModifier;
import src.toi_et_moi.mgdp.modifier.combat.IronUpgradeModifier;
import src.toi_et_moi.mgdp.modifier.combat.TridentFestivalModifier;
import src.toi_et_moi.mgdp.modifier.combat.EndVoidModifier;
import src.toi_et_moi.mgdp.modifier.combat.RiptideModifier;
import src.toi_et_moi.mgdp.modifier.farming.ProsperityModifier;
import src.toi_et_moi.mgdp.modifier.farming.LiquidClearModifier;
import src.toi_et_moi.mgdp.modifier.special.LordModifier;
import src.toi_et_moi.mgdp.modifier.buff.SnowTrailModifier;
import src.toi_et_moi.mgdp.modifier.buff.WitchModifier;
import src.toi_et_moi.mgdp.modifier.special.SwapModifier;
import src.toi_et_moi.mgdp.modifier.special.BackflipModifier;
import src.toi_et_moi.mgdp.modifier.special.WindmillModifier;
import src.toi_et_moi.mgdp.modifier.special.PenguinModifier;
import src.toi_et_moi.mgdp.modifier.special.EndOfBeginningModifier;
import src.toi_et_moi.mgdp.modifier.special.TimeAxisModifier;
import src.toi_et_moi.mgdp.modifier.daytime.CoronaModifier;
import src.toi_et_moi.mgdp.modifier.nighttime.MoonShadowModifier;
import src.toi_et_moi.mgdp.modifier.buff.CroneModifier;
import src.toi_et_moi.mgdp.modifier.buff.BottlingModifier;
import src.toi_et_moi.mgdp.modifier.MGDPAddSlotModifier;
import dev.xkmc.modulargolems.content.modifier.common.AddSlotModifier;

import static dev.xkmc.modulargolems.init.registrate.GolemModifiers.reg;

public class MGDPModifiers {

	public static final RegistryEntry<HarvestCropModifier> HARVEST_CROP;
	public static final RegistryEntry<FlightModifier> FLIGHT;
	public static final RegistryEntry<PotionAuraModifier> POTION_AURA;
	public static final RegistryEntry<RebirthModifier> REBIRTH;
	public static final RegistryEntry<UnstoppableModifier> UNSTOPPABLE;
	public static final RegistryEntry<SpiritModifier> SPIRIT;
	public static final RegistryEntry<NetheriteGoldModifier> NETHERITE_GOLD;
	public static final RegistryEntry<EnchantedNetheriteGoldModifier> ENCHANTED_NETHERITE_GOLD;
	public static final RegistryEntry<BellOfAviciModifier> BELL_OF_AVICI;
	public static final RegistryEntry<DiamondAttackModifier> DIAMOND_ATTACK;
	public static final RegistryEntry<EnchantedDiamondAttackModifier> ENCHANTED_DIAMOND_ATTACK;
	public static final RegistryEntry<CrimsonAttackModifier> CRIMSON_ATTACK;
	public static final RegistryEntry<EnchantedCrimsonAttackModifier> ENCHANTED_CRIMSON_ATTACK;
	public static final RegistryEntry<LightningStormModifier> LIGHTNING_STORM;
	public static final RegistryEntry<RocketFlightModifier> ROCKET_FLIGHT;
	public static final RegistryEntry<DragonBreathModifier> DRAGON_BREATH;
	public static final RegistryEntry<WitherExtinctionModifier> WITHER_EXTINCTION;
	public static final RegistryEntry<ChargedShieldModifier> CHARGED_SHIELD;
	public static final RegistryEntry<VersatilityModifier> VERSATILITY;
	public static final RegistryEntry<ConduitModifier> CONDUIT;
	public static final RegistryEntry<OverworldModifier> OVERWORLD;
	public static final RegistryEntry<NetherModifier> NETHER;
	public static final RegistryEntry<SunlightModifier> SUNLIGHT;
	public static final RegistryEntry<HypothermiaModifier> HYPOTHERMIA;
	public static final RegistryEntry<SelfRepairModifier> SELF_REPAIR;
	public static final RegistryEntry<PotionDefenseModifier> TRUE_INVISIBILITY;
	public static final RegistryEntry<PotionDefenseModifier> INVISIBILITY;
	public static final RegistryEntry<ExecutionerModifier> EXECUTIONER;
	public static final RegistryEntry<FocusedDefenseModifier> FOCUSED_DEFENSE;
	public static final RegistryEntry<SonicBoomModifier> SONIC_BOOM;
	public static final RegistryEntry<ProjectileDodgeModifier> PROJECTILE_DODGE;
	public static final RegistryEntry<BackstepModifier> BACKSTEP;
	public static final RegistryEntry<SelfDestructModifier> SELF_DESTRUCT;
	public static final RegistryEntry<DementorModifier> DEMENTOR;
	public static final RegistryEntry<DrainModifier> DRAIN;
	public static final RegistryEntry<ReprintModifier> REPRINT;
	public static final RegistryEntry<BrushModifier> BRUSH;
	public static final RegistryEntry<BombDisposalModifier> BOMB_DISPOSAL;
	public static final RegistryEntry<FireballModifier> FIREBALL;
	public static final RegistryEntry<TotemicModifier> TOTEMIC;
	public static final RegistryEntry<HeroModifier> HERO;
	public static final RegistryEntry<FlareModifier> FLARE;
	public static final RegistryEntry<BlastFurnaceModifier> BLAST_FURNACE;
	public static final RegistryEntry<AnglerModifier> ANGLER;
	public static final RegistryEntry<UndyingModifier> UNDYING;
	public static final RegistryEntry<GrenadeModifier> GRENADE;
	public static final RegistryEntry<UnbreakableModifier> UNBREAKABLE;
	public static final RegistryEntry<InfiniteAmmoModifier> INFINITE_AMMO;
	public static final RegistryEntry<QuickStrikeModifier> QUICK_STRIKE;
	public static final RegistryEntry<DeathKnellModifier> DEATH_KNELL;
	public static final RegistryEntry<EchoTrioModifier> ECHO_TRIO;
	public static final RegistryEntry<AnvilSlamModifier> ANVIL_SLAM;
	public static final RegistryEntry<IronUpgradeModifier> IRON_UPGRADE;
	public static final RegistryEntry<TridentFestivalModifier> TRIDENT_FESTIVAL;
	public static final RegistryEntry<RiptideModifier> RIPTIDE;
	public static final RegistryEntry<EndVoidModifier> END_VOID;
	public static final RegistryEntry<ProsperityModifier> PROSPERITY;
	public static final RegistryEntry<LiquidClearModifier> LIQUID_CLEAR;
	public static final RegistryEntry<LordModifier> LORD;
	public static final RegistryEntry<SnowTrailModifier> SNOW_TRAIL;
	public static final RegistryEntry<SwapModifier> SWAP;
	public static final RegistryEntry<BackflipModifier> BACKFLIP;
	public static final RegistryEntry<WindmillModifier> WINDMILL;
	public static final RegistryEntry<WitchModifier> WITCH;
	public static final RegistryEntry<CroneModifier> CRONE;
	public static final RegistryEntry<BottlingModifier> BOTTLING;
	public static final RegistryEntry<PenguinModifier> PENGUIN;
	public static final RegistryEntry<EndOfBeginningModifier> END_OF_BEGINNING;
	public static final RegistryEntry<CoronaModifier> CORONA;
	public static final RegistryEntry<MoonShadowModifier> MOON_SHADOW;
	public static final RegistryEntry<TimeAxisModifier> TIME_AXIS;
	public static final RegistryEntry<AddSlotModifier> CATACLYSMFARMER_ADD, DARK_ADD, PYRIUM_ADD, SCULKIUM_ADD;
	public static final RegistryEntry<MGDPAddSlotModifier> MEROR_ADD, REFINE_MEROR_ADD;

	static {
		HARVEST_CROP = reg("harvest_crop", HarvestCropModifier::new,
				"Harvest Crop",
				"Auto harvest and replant crops within %s block(s) per Pickup level. Requires Pickup upgrade to work.");

		FLIGHT = reg("flight", FlightModifier::new,
				"Flight",
				"Golem gains creative-style flight. Can move freely in all three dimensions.");

		POTION_AURA = reg("potion_aura", PotionAuraModifier::new,
				"Potion Aura",
				"Shares positive effects with allies and inflicts negative effects on enemies within 48 blocks.");

		REBIRTH = reg("rebirth", RebirthModifier::new,
				"Rebirth",
				"Golem gains the ability to revive after death and recycle itself.");

		UNSTOPPABLE = reg("unstoppable", UnstoppableModifier::new,
				"Unstoppable",
				"Golem ignores terrain slowdown, knockback, entity collision, and cramming damage.");

		SPIRIT = reg("spirit", SpiritModifier::new,
				"Spirit",
				"Requires Flight. Golem phases through all blocks like a Vex, becoming completely intangible.");

		NETHERITE_GOLD = reg("netherite_gold", NetheriteGoldModifier::new,
				"Netherite Gold Apple",
				"Regeneration V and fire immunity in one upgrade.");

		ENCHANTED_NETHERITE_GOLD = reg("enchanted_netherite_gold", EnchantedNetheriteGoldModifier::new,
				"Enchanted Netherite Gold Apple",
				"Regeneration V, fire immune, explosion resistant, lava walking, and +10% healing.");

		BELL_OF_AVICI = reg("bell_of_avici", BellOfAviciModifier::new,
				"Bell of Avici",
				"Enhanced Bell: 64-block radius, enemies that target the golem are teleported nearby every 2s.");

		DIAMOND_ATTACK = reg("diamond_attack", DiamondAttackModifier::new,
				"Diamond Attack",
				"+30% attack damage.");

		ENCHANTED_DIAMOND_ATTACK = reg("enchanted_diamond_attack", EnchantedDiamondAttackModifier::new,
				"Enchanted Diamond Attack",
				"+60% attack damage, all attacks are critical hits.");

		CRIMSON_ATTACK = reg("crimson_attack", CrimsonAttackModifier::new,
				"Crimson Attack",
				"-5% current HP per hit, +50% attack damage, min attack speed 1.0.");

		ENCHANTED_CRIMSON_ATTACK = reg("enchanted_crimson_attack", EnchantedCrimsonAttackModifier::new,
				"Enchanted Crimson Attack",
				"-10% current HP per hit, +100% attack damage, 50% lifesteal, min attack speed 1.0.");

		LIGHTNING_STORM = reg("lighting_storm", LightningStormModifier::new,
				"Lightning Storm",
				"Periodically strikes lightning on enemies. Damage = golem attack. 3x in thunderstorms.");

		ROCKET_FLIGHT = reg("rocket_flight", RocketFlightModifier::new,
				"Rocket Flight",
				"Same as Flight, but -100% armor and toughness.");

		DRAGON_BREATH = reg("dragon_breath", DragonBreathModifier::new,
				"Dragon Breath",
				"Fires homing dragon fireballs at enemies. Explosion + breath cloud on impact.");

		WITHER_EXTINCTION = reg("wither_extinction", WitherExtinctionModifier::new,
				"Wither Extinction",
				"Charges 10s, then deals 100% max HP as wither + explosion damage to all enemies in 64 blocks. 1min cooldown.");

		CHARGED_SHIELD = reg("charged_shield", ChargedShieldModifier::new,
				"Charged Shield",
				"5 damage-absorbing shields. Each blocks 1 hit. Recharges every 15/10/5 seconds per level.");

			TRUE_INVISIBILITY = reg("true_invisibility", () -> new PotionDefenseModifier(1, src.toi_et_moi.mgdp.modifier.CompatUtil::getTrueInvisibility),
					"True Invisibility",
					"Golem becomes truly invisible (Iron's Spells 'n Spellbooks).");

			INVISIBILITY = reg("invisibility", () -> new PotionDefenseModifier(1, () -> MobEffects.INVISIBILITY),
					"Invisibility",
					"Golem becomes invisible.");

			EXECUTIONER = reg("executioner", ExecutionerModifier::new,
					"Executioner",
					"Stronger against weakened enemies. Takes reduced/no damage from low-health attackers.");

			FOCUSED_DEFENSE = reg("focused_defense", FocusedDefenseModifier::new,
					"Focused Defense",
					"Only takes damage from the golem's own target.");

			SONIC_BOOM = reg("sonic_boom", SonicBoomModifier::new,
					"Sonic Boom",
					"Golem can use Sonic Boom Attack. Deals 10 damage per level and knocks back enemies.");

			SELF_REPAIR = reg("self_repair", SelfRepairModifier::new,
					"Self Repair",
					"When idle for 10s, regenerates 1 HP per second and reduces forge count.");

			CONDUIT = reg("conduit", ConduitModifier::new,
				"Conduit",
				"Golem grants Conduit Power to allies, gains attack and dodge in water/rain. Synergizes with Lightning Storm.");

			OVERWORLD = reg("overworld", OverworldModifier::new,
				"Primordial Earth",
				"Golem receives double healing and ignores knockback in the Overworld, underground (Y≤0) deals double damage and takes 30% less damage.");

			NETHER = reg("nether", NetherModifier::new,
				"Blazing Inferno",
				"Golem repairs once per second when on fire, in lava, or above Y=128 in the Nether, and deals double damage.");

			SUNLIGHT = reg("sunlight", SunlightModifier::new,
				"Radiance",
				"Golem regenerates in bright areas and deals bonus damage in darkness.");

			HYPOTHERMIA = reg("hypothermia", HypothermiaModifier::new,
					"Hypothermia",
					"Freezes enemies on hit and extinguishes fire in a 48-block radius.");

			VERSATILITY = reg("versatility", VersatilityModifier::new,
					"Versatility",
					"The first 5 non-blue MGDP upgrades don't consume upgrade slots.");

			DEMENTOR = reg("hostility_dementor", DementorModifier::new,
				"Hostility Upgrade: Dementor",
				"Reduces incoming damage with a nonlinear formula.");

			DRAIN = reg("hostility_drain", DrainModifier::new,
				"Hostility Upgrade: Drain",
				"Deals extra damage per negative effect on target and steals beneficial effects.");

			FIREBALL = reg("fireball", FireballModifier::new,
				"Fireball Attack",
				"Golem shoots fireballs at enemies within range.");

			BRUSH = reg("brush", BrushModifier::new,
				"Archaeology",
				"Requires Pickup. Golem with Brush automatically extracts items from suspicious blocks.");

			BOMB_DISPOSAL = reg("bomb_disposal", BombDisposalModifier::new,
				"Bomb Disposal Expert",
				"Nullifies non-friendly explosions and defuses primed TNT.");

			REPRINT = reg("hostility_reprint", ReprintModifier::new,
				"Hostility Upgrade: Reprint",
				"Deals bonus damage based on target's total enchantment levels.");

			SELF_DESTRUCT = reg("self_destruct", SelfDestructModifier::new,
				"Self Destruct",
				"Golem explodes upon death or retrieval, dealing damage equal to max HP.");

			PROJECTILE_DODGE = reg("projectile_dodge", ProjectileDodgeModifier::new,
				"Projectile Dodge",
				"Dodges incoming projectiles and fast-moving threats.");

			BACKSTEP = reg("backstep", BackstepModifier::new,
				"Backstep",
				"Golem backsteps away when a target gets too close. If Backflip is also installed, the backstep is stylish.");

			TOTEMIC = reg("totemic", TotemicModifier::new,
				"Totemic Apple",
				"Every %ss, grants absorption hearts equal to 10%% of max health per level.");

			HERO = reg("hero", HeroModifier::new,
				"Hero of the Village",
				"Grants the owner 1 minute of Hero of the Village per raider slain. Stacks duration. Compatible with Emerald upgrade.");

			FLARE = reg("flare", FlareModifier::new,
				"Illumination",
				"Golem automatically places torches in nearby dark areas. Makes cave exploration safer.");

			BLAST_FURNACE = reg("blast_furnace", BlastFurnaceModifier::new,
				"Blast Furnace",
				"Golem smelts ores in hand like a blast furnace.");

			ANGLER = reg("angler", AnglerModifier::new,
				"Angler",
				"Golem automatically fishes when holding a fishing rod near water.");

			UNDYING = reg("hostility_undying", UndyingModifier::new,
				"Hostility Upgrade: Undying",
				"When the golem would die, it instead revives with full health. Cannot bypass creative/kill damage.");

			GRENADE = reg("hostility_grenade", GrenadeModifier::new,
				"Hostility Upgrade: Grenade",
				"Shoots fast homing explosive grenades at targets within 40 blocks.");

			UNBREAKABLE = reg("unbreakable", UnbreakableModifier::new,
				"Unbreakable",
				"Golem equipment takes no durability damage.");

			INFINITE_AMMO = reg("infinite_ammo", InfiniteAmmoModifier::new,
				"Infinite Ammo",
				"Golem has unlimited ammunition for ranged weapons.");

			QUICK_STRIKE = reg("quick_strike", QuickStrikeModifier::new,
				"Quick Strike",
				"Golem attacks instantly with no cooldown for both melee and ranged.");

			DEATH_KNELL = reg("death_knell", DeathKnellModifier::new,
				"Death Knell",
				"Deals bonus damage equal to 2%% of target's max health.");

			ECHO_TRIO = reg("echo_trio", EchoTrioModifier::new,
				"Echo Trio",
				"Locks onto 3 targets within 35 blocks, fires sonic booms at them, and counter-attacks when damaged.");

			ANVIL_SLAM = reg("anvil_slam", AnvilSlamModifier::new,
				"Anvil Slam",
				"Golem leaps toward the target and slams an anvil, dealing AOE damage to nearby enemies.");

			IRON_UPGRADE = reg("iron_upgrade", IronUpgradeModifier::new,
				"Iron Upgrade",
				"Pure stat upgrade: +5 armor, +2 damage, +0.5 range, +0.5 sweep per level. Max level 2.");

			TRIDENT_FESTIVAL = reg("trident_festival", TridentFestivalModifier::new,
				"Trident Festival",
				"Golem summons tridents that explode and strike lightning on impact.");

			END_VOID = reg("end_void", EndVoidModifier::new,
				"End Void",
				"Golem deals void damage in The End and is healed by end crystals like the Ender Dragon.");

			RIPTIDE = reg("riptide", RiptideModifier::new,
				"Riptide",
				"Golem with a Riptide trident launches toward targets in water or rain.");


			PROSPERITY = reg("prosperity", ProsperityModifier::new,
				"Prosperity",
				"Crop range +%s/level (current %s). Loot x(level+1).");

		LORD = reg("lord", LordModifier::new,
				"Lord",
				"Golem displays a personal boss bar showing its name and health.");

			SNOW_TRAIL = reg("snow_trail", SnowTrailModifier::new,
				"Snow Trail",
				"Golem leaves a trail of snow and freezes water like Frost Walker as it moves.");

			SWAP = reg("swap", SwapModifier::new,
				"Swap",
				"Press the swap key (default: R) to switch places with the golem. When taking fatal damage, passively swaps with a random golem with this upgrade. 10s cooldown.");

			BACKFLIP = reg("backflip", BackflipModifier::new,
				"Backflip",
				"Golem does a stylish backflip while idling.");

			WINDMILL = reg("windmill", WindmillModifier::new,
				"Windmill",
				"Golem spins constantly like a windmill. Pure cosmetic.");


			WITCH = reg("witch", WitchModifier::new,
				"Witch",
				"Golem buffs itself and throws potions at enemies and allies like a witch.");
			PENGUIN = reg("penguin", PenguinModifier::new,
			"Penguin",
				"Golem spawns a penguin at the target death location. Penguin! Penguin!");

		END_OF_BEGINNING = reg("end_of_beginning", EndOfBeginningModifier::new,
			"§6End of Beginning",
			"§5Combines all 3 dimension upgrades. Effects work everywhere. Nearby End Crystals make the golem invulnerable and need 13 hits to destroy.");

		CORONA = reg("corona", CoronaModifier::new,
			"Corona",
			"During the day, the golem ignores all negative potion effects.");

		MOON_SHADOW = reg("moon_shadow", MoonShadowModifier::new,
			"Moon Shadow",
			"During the night, prevents hostile mob spawning within 32 blocks.");

		TIME_AXIS = reg("time_axis", TimeAxisModifier::new,
			"§6Time Axis",
			"§5Combines Radiance, Corona, and Moon Shadow. Effects ignore light/time restrictions. Accelerates block growth and random ticks nearby.");


			CRONE = reg("crone", CroneModifier::new,
				"Crone",
				"Golem attacks with Goety brew effects and buffs allies.");

			BOTTLING = reg("bottling", BottlingModifier::new,
				"Bottling",
				"Golem applies bottling effect to its owner. Effect level equals upgrade level.");


			CATACLYSMFARMER_ADD = reg("add_slot_cataclysmfarer", () -> new src.toi_et_moi.mgdp.modifier.MGDPAddSlotModifier(1, 4),
				"Cataclysmfarer Expansion",
				"Add 3 upgrade slots.");

			DARK_ADD = reg("add_slot_dark", () -> new src.toi_et_moi.mgdp.modifier.MGDPAddSlotModifier(1, 1),
				"Dark Expansion",
				"Add 1 upgrade slots.");

			PYRIUM_ADD = reg("add_slot_pyrium", () -> new src.toi_et_moi.mgdp.modifier.MGDPAddSlotModifier(1, 2),
				"Pyrium Expansion",
				"Add 1 upgrade slots.");

			SCULKIUM_ADD = reg("add_slot_sculkium", () -> new src.toi_et_moi.mgdp.modifier.MGDPAddSlotModifier(1, 1),
				"Sculkium Expansion",
				"Add 1 upgrade slots.");

		    MEROR_ADD = reg("add_slot_meror", () -> new src.toi_et_moi.mgdp.modifier.MGDPAddSlotModifier(1, 1),
				"Meror Expansion",
				"Add 1 upgrade slot.");

			REFINE_MEROR_ADD = reg("add_slot_refine_meror", () -> new src.toi_et_moi.mgdp.modifier.MGDPAddSlotModifier(1, 2),
				"Refined Meror Expansion",
				"Add 1 upgrade slot.");

			LIQUID_CLEAR = reg("liquid_clear", LiquidClearModifier::new,
				"Liquid Clear",
				"Clear range +%s/level (current %s).");

		}


	public static void register() {
	}


}
