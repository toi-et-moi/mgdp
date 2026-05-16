package src.toi_et_moi.mgdp.init;

import com.tterrag.registrate.util.entry.RegistryEntry;
import src.toi_et_moi.mgdp.modifier.FlightModifier;
import src.toi_et_moi.mgdp.modifier.HarvestCropModifier;
import src.toi_et_moi.mgdp.modifier.PotionAuraModifier;

import static dev.xkmc.modulargolems.init.registrate.GolemModifiers.reg;

public class MGDPModifiers {

	public static final RegistryEntry<HarvestCropModifier> HARVEST_CROP;
	public static final RegistryEntry<FlightModifier> FLIGHT;
	public static final RegistryEntry<PotionAuraModifier> POTION_AURA;

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
	}

	public static void register() {
	}

}
