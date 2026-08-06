package src.toi_et_moi.mgdp.modifier.goety_revelation;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;

public class ApocalypseModifier extends GolemModifier {

	private static final int RANGE = 35;
	private static final int DURATION = 200;
	private static final int AMP = 4;

	public ApocalypseModifier() {
		super(StatFilterType.ATTACK, 1);
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		if (!net.minecraftforge.fml.ModList.get().isLoaded("goety")) return;
		if (golem.tickCount % 20 != 0) return;

		// Erosion V + Curse V, strip all target buffs
		var sapped = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("goety", "sapped"));
		var cursed = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("goety", "cursed"));
		var box = golem.getBoundingBox().inflate(RANGE);
		for (var entity : golem.level().getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive())) {
			if (entity.distanceToSqr(golem) > RANGE * RANGE) continue;
			if (entity == golem || entity.isAlliedTo(golem) || entity == golem.getOwner()) continue;
			if (!golem.canAttack(entity)) continue;
			for (var eff : new ArrayList<>(entity.getActiveEffects())) {
				if (eff.getEffect().isBeneficial()) {
					entity.removeEffect(eff.getEffect());
				}
			}
			if (sapped != null) entity.addEffect(new MobEffectInstance(sapped, DURATION, AMP));
			if (cursed != null) entity.addEffect(new MobEffectInstance(cursed, DURATION, AMP));
		}
	}

	@Override
	public void onHurtTarget(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		if (golem.level().isClientSide()) return;
		var target = event.getEntity();
		if (!golem.canAttack(target)) return;
		if (target == golem || target == golem.getOwner()) return;

		// +2000% universal damage
		event.setAmount(event.getAmount() * 21.0F);

		// Doom XX for 1 frame: the expiry kill-check at amplifier 19 executes at 100% max HP
		var doom = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("goety", "doom"));
		if (doom != null) {
			target.addEffect(new MobEffectInstance(doom, 1, 19));
		}
	}

	@Override
	public void onHurt(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		// -50% damage taken while a target is locked
		if (golem.level().isClientSide()) return;
		if (golem.getTarget() != null) {
			event.setAmount(event.getAmount() * 0.5F);
		}
	}
}
