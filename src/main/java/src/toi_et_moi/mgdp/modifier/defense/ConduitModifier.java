package src.toi_et_moi.mgdp.modifier.defense;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class ConduitModifier extends GolemModifier {

	public ConduitModifier() {
		super(StatFilterType.HEALTH, 1);
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;

		// Conduit Power aura for nearby allies (level 3 equivalent)
		if (golem.tickCount % 40 == 0) {
			AABB area = golem.getBoundingBox().inflate(20);
			for (LivingEntity target : golem.level().getEntitiesOfClass(LivingEntity.class, area,
					e -> e.isAlive() && !e.isSpectator() && (
							e == golem ||
							(e instanceof Player p && p == golem.getOwner()) ||
							(e instanceof AbstractGolemEntity<?, ?> other
									&& other.getOwnerUUID() != null
									&& other.getOwnerUUID().equals(golem.getOwnerUUID()))))) {
				target.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 220, 2, false, false, true));
			}
		}

		// Weather lock synergy with Lightning Storm
		if (golem.tickCount % 20 == 0
				&& golem.getModifiers().containsKey(src.toi_et_moi.mgdp.init.MGDPModifiers.LIGHTNING_STORM.get())) {
			if (golem.level() instanceof ServerLevel sl && !sl.isThundering()) {
				sl.setWeatherParameters(0, 4800, true, true);
			}
		}
	}

	@Override
	public void onAttacked(AbstractGolemEntity<?, ?> golem, LivingAttackEvent event, int level) {
		if (golem.level().isClientSide()) return;
		if (!golem.isInWaterOrRain()) return;
		if (golem.getRandom().nextFloat() < 0.45f) {
			event.setCanceled(true);
		}
	}

	@Override
	public void onHurtTarget(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		if (golem.level().isClientSide()) return;
		if (golem.isInWaterOrRain()) {
			event.setAmount(event.getAmount() * 1.6f);
		}
	}
}
