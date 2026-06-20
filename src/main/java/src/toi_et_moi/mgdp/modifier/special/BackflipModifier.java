package src.toi_et_moi.mgdp.modifier.special;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.AttributeGolemModifier;
import dev.xkmc.modulargolems.init.registrate.GolemTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import src.toi_et_moi.mgdp.init.IFlipData;

import java.util.List;

public class BackflipModifier extends AttributeGolemModifier {

	private static final String TAG_FLIP = "mgdp_flipping";
	private static final String TAG_FLIP_TICK = "mgdp_flip_tick";

	public BackflipModifier() {
		super(1, new AttrEntry(() -> GolemTypes.STAT_ARMOR.get(), () -> 1.0));
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		var data = golem.getPersistentData();

		if (data.getBoolean(TAG_FLIP)) {
			int tick = data.getInt(TAG_FLIP_TICK) + 1;
			data.putInt(TAG_FLIP_TICK, tick);

			((IFlipData) golem).mgdp$setFlipProgress(Math.min(tick * 25, 400));

			if (tick >= 16 || golem.onGround()) {
				((IFlipData) golem).mgdp$setFlipProgress(0);
				golem.setXRot(0);
				data.remove(TAG_FLIP);
				data.remove(TAG_FLIP_TICK);
			}
			return;
		}

		if (golem.getTarget() != null) return;
		if (!golem.onGround()) return;
		if (golem.tickCount % 80 != 0) return;

		golem.setDeltaMovement(0, 0.7, 0);
		golem.hasImpulse = true;
		data.putBoolean(TAG_FLIP, true);
		data.putInt(TAG_FLIP_TICK, 0);
	}

	@Override
	public List<MutableComponent> getDetail(int v) {
		var list = super.getDetail(v);
		list.add(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GREEN));
		return list;
	}
}
