package src.toi_et_moi.mgdp.advancement;

import net.minecraft.advancements.CriteriaTriggers;

public class InitTrigger {

	public static GolemKillTrigger GOLEM_KILL;

	public static void init() {
		GOLEM_KILL = CriteriaTriggers.register(new GolemKillTrigger());
	}
}
