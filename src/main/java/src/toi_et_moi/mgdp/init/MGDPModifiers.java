package src.toi_et_moi.mgdp.init;

import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.xkmc.modulargolems.content.modifier.base.PotionDefenseModifier;
import src.toi_et_moi.mgdp.modifier.CompatUtil;
import net.minecraft.world.effect.MobEffects;
import src.toi_et_moi.mgdp.modifier.FlightModifier;
import src.toi_et_moi.mgdp.modifier.HarvestCropModifier;
import src.toi_et_moi.mgdp.modifier.PotionAuraModifier;
import src.toi_et_moi.mgdp.modifier.RebirthModifier;
import src.toi_et_moi.mgdp.modifier.EnchantedNetheriteGoldModifier;
import src.toi_et_moi.mgdp.modifier.NetheriteGoldModifier;
import src.toi_et_moi.mgdp.modifier.SpiritModifier;
import src.toi_et_moi.mgdp.modifier.BellOfAviciModifier;
import src.toi_et_moi.mgdp.modifier.ChargedShieldModifier;
import src.toi_et_moi.mgdp.modifier.CrimsonAttackModifier;
import src.toi_et_moi.mgdp.modifier.DiamondAttackModifier;
import src.toi_et_moi.mgdp.modifier.DragonBreathModifier;
import src.toi_et_moi.mgdp.modifier.EnchantedCrimsonAttackModifier;
import src.toi_et_moi.mgdp.modifier.WitherExtinctionModifier;
import src.toi_et_moi.mgdp.modifier.EnchantedDiamondAttackModifier;
import src.toi_et_moi.mgdp.modifier.LightningStormModifier;
import src.toi_et_moi.mgdp.modifier.RocketFlightModifier;
import src.toi_et_moi.mgdp.modifier.UnstoppableModifier;
import src.toi_et_moi.mgdp.modifier.HypothermiaModifier;
import src.toi_et_moi.mgdp.modifier.SelfRepairModifier;
import src.toi_et_moi.mgdp.modifier.ExecutionerModifier;
import src.toi_et_moi.mgdp.modifier.FocusedDefenseModifier;
import src.toi_et_moi.mgdp.modifier.SonicBoomModifier;
import src.toi_et_moi.mgdp.modifier.VersatilityModifier;
import src.toi_et_moi.mgdp.modifier.TotemicModifier;

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
	public static final RegistryEntry<HypothermiaModifier> HYPOTHERMIA;
	public static final RegistryEntry<SelfRepairModifier> SELF_REPAIR;
	public static final RegistryEntry<PotionDefenseModifier> TRUE_INVISIBILITY;
	public static final RegistryEntry<PotionDefenseModifier> INVISIBILITY;
	public static final RegistryEntry<ExecutionerModifier> EXECUTIONER;
	public static final RegistryEntry<FocusedDefenseModifier> FOCUSED_DEFENSE;
	public static final RegistryEntry<SonicBoomModifier> SONIC_BOOM;
	public static final RegistryEntry<TotemicModifier> TOTEMIC;

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

			HYPOTHERMIA = reg("hypothermia", HypothermiaModifier::new,
					"Hypothermia",
					"Freezes enemies on hit and extinguishes fire in a 48-block radius.");

			VERSATILITY = reg("versatility", VersatilityModifier::new,
					"Versatility",
					"The first 5 non-blue MGDP upgrades don't consume upgrade slots.");

			TOTEMIC = reg("totemic", TotemicModifier::new,
				"Totemic Apple",
				"Every %ss, grants absorption hearts equal to 10%% of max health per level.");

	}

	public static void register() {
	}


}
