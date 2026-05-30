package src.toi_et_moi.mgdp.modifier;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public class TotemicModifier extends GolemModifier {

	private static final int[] INTERVALS = {600, 500, 400, 300, 200}; // 30/25/20/15/10 seconds in ticks

	public TotemicModifier() {
		super(StatFilterType.HEALTH, 5);
	}

	@Override
	public List<MutableComponent> getDetail(int v) {
		int idx = Math.min(v - 1, INTERVALS.length - 1);
		int interval = INTERVALS[idx] / 20;
		return List.of(Component.translatable(getDescriptionId() + ".desc", interval).withStyle(ChatFormatting.GREEN));
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		if (level < 1) return;

		int idx = Math.min(level - 1, INTERVALS.length - 1);
		int interval = INTERVALS[idx];

		String key = "mgdp_totemic_" + golem.getId();
		var data = golem.getPersistentData();
		long last = data.getLong(key);
		long now = golem.level().getGameTime();
		if (now - last < interval) return;
		data.putLong(key, now);

		float absorption = golem.getMaxHealth() * 0.1f * level;
		golem.setAbsorptionAmount(absorption);
	}
}
