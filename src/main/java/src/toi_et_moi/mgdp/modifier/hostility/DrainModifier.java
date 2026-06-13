package src.toi_et_moi.mgdp.modifier.hostility;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.ArrayList;
import java.util.List;

public class DrainModifier extends GolemModifier {

	public DrainModifier() {
		super(StatFilterType.ATTACK, 5);
	}

	@Override
	public void onHurtTarget(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		if (!(event.getEntity() instanceof LivingEntity)) return;
		LivingEntity target = (LivingEntity) event.getEntity();

		// Extra damage per negative effect on target
		long negCount = target.getActiveEffects().stream()
				.filter(e -> e.getEffect().getCategory() == MobEffectCategory.HARMFUL)
				.count();
		float bonus = 0.1f * level * negCount;
		if (bonus > 0) {
			event.setAmount(event.getAmount() * (1 + bonus));
		}

		// Steal level beneficial effects from target
		var pos = new ArrayList<>(target.getActiveEffects().stream()
				.filter(e -> e.getEffect().getCategory() == MobEffectCategory.BENEFICIAL)
				.toList());
		for (int i = 0; i < level; i++) {
			if (pos.isEmpty()) break;
			var ins = pos.remove(golem.getRandom().nextInt(pos.size()));
			var stolen = new MobEffectInstance(ins);
			target.removeEffect(ins.getEffect());
			golem.addEffect(stolen);
		}

		// Extend negative effects on target
		var toExtend = new java.util.ArrayList<>(target.getActiveEffects());
		for (var effect : toExtend) {
			if (effect.getEffect().getCategory() != MobEffectCategory.HARMFUL) continue;
			int extra = (int) (effect.getDuration() * 0.5f * level);
			int maxExtra = level * 1200;
			if (extra > 0 && extra < maxExtra) {
				var extended = new MobEffectInstance(effect.getEffect(),
						Math.min(effect.getDuration() + extra, effect.getDuration() + maxExtra),
						effect.getAmplifier(), effect.isAmbient(), effect.isVisible());
				target.removeEffect(effect.getEffect());
				target.addEffect(extended);
			}
		}
	}

	@Override
	public List<MutableComponent> getDetail(int v) {
		int idx = Math.min(v, 5);
		int dmgPct = idx * 10;
		int durPct = idx * 50;
		int maxSec = idx * 60;
		int stolen = idx;
		return List.of(Component.translatable(getDescriptionId() + ".desc",
				dmgPct, durPct, maxSec, stolen).withStyle(ChatFormatting.GREEN));
	}
}
