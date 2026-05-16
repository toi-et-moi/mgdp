package src.toi_et_moi.mgdp.init;

import com.tterrag.registrate.util.entry.RegistryEntry;
import src.toi_et_moi.mgdp.modifier.FlightModifier;
import src.toi_et_moi.mgdp.modifier.HarvestCropModifier;

import static dev.xkmc.modulargolems.init.registrate.GolemModifiers.reg;

public class MGDPModifiers {

	public static final RegistryEntry<HarvestCropModifier> HARVEST_CROP;
	public static final RegistryEntry<FlightModifier> FLIGHT;

	static {
		HARVEST_CROP = reg("harvest_crop", HarvestCropModifier::new,
				"Harvest Crop",
				"Auto harvest and replant crops within %s block(s) per Pickup level. Requires Pickup upgrade to work.");

		FLIGHT = reg("flight", FlightModifier::new,
				"Flight",
				"Golem gains creative-style flight. Can move freely in all three dimensions.");
	}

	public static void register() {
	}

}
