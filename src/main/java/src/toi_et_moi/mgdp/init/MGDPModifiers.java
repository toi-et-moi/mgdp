package src.toi_et_moi.mgdp.init;

import com.tterrag.registrate.util.entry.RegistryEntry;
import src.toi_et_moi.mgdp.modifier.FlightModifier;
import src.toi_et_moi.mgdp.modifier.HarvestCropModifier;
import src.toi_et_moi.mgdp.modifier.PotionAuraModifier;
import src.toi_et_moi.mgdp.modifier.RebirthModifier;
import src.toi_et_moi.mgdp.modifier.EnchantedNetheriteGoldModifier;
import src.toi_et_moi.mgdp.modifier.NetheriteGoldModifier;
import src.toi_et_moi.mgdp.modifier.SpiritModifier;
import src.toi_et_moi.mgdp.modifier.UnstoppableModifier;

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
	}

	public static void register() {
	}

}
