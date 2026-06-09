package src.toi_et_moi.mgdp.advancement;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import src.toi_et_moi.mgdp.Mgdp;
import src.toi_et_moi.mgdp.init.MGDPStats;

public class GolemKillTrigger extends SimpleCriterionTrigger<GolemKillTrigger.Instance> {

	private static final ResourceLocation ID = new ResourceLocation(Mgdp.MODID, "golem_kill");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	protected Instance createInstance(JsonObject json, ContextAwarePredicate predicate, DeserializationContext ctx) {
		int threshold = json.get("threshold").getAsInt();
		return new Instance(threshold);
	}

	public void trigger(ServerPlayer player) {
		super.trigger(player, inst -> inst.matches(player));
	}

	public static class Instance extends AbstractCriterionTriggerInstance {
		private final int threshold;

		public Instance(int threshold) {
			super(ID, ContextAwarePredicate.ANY);
			this.threshold = threshold;
		}

		public boolean matches(ServerPlayer player) {
			int count = player.getStats().getValue(Stats.CUSTOM.get(MGDPStats.GOLEM_KILLS));
			return count >= threshold;
		}

		@Override
		public JsonObject serializeToJson(SerializationContext ctx) {
			JsonObject json = super.serializeToJson(ctx);
			json.addProperty("threshold", threshold);
			return json;
		}
	}
}
