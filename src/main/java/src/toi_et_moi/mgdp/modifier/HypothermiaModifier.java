package src.toi_et_moi.mgdp.modifier;

import dev.xkmc.l2damagetracker.init.data.L2DamageTypes;
import dev.xkmc.l2library.init.events.GeneralEventHandler;
import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;

public class HypothermiaModifier extends GolemModifier {

	private static final int INTERVAL = 40;
	private static final double RANGE = 48.0;
	private static final int FREEZE_TICKS = 300;
	private static int looping = 0;

	public HypothermiaModifier() {
		super(StatFilterType.ATTACK, 1);
	}

	@Override
	public void onAttackTarget(AbstractGolemEntity<?, ?> golem, LivingAttackEvent event, int level) {
		if (looping > 0) return;
		if (!event.getSource().is(L2DamageTypes.DIRECT)) return;

		GeneralEventHandler.schedule(() -> {
			var type = golem.level().registryAccess()
					.lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.FREEZE);
			var source = new DamageSource(type, golem);
			looping++;
			if (!event.getEntity().hurt(source, event.getAmount())) {
				event.getEntity().hurt(event.getSource(), event.getAmount());
			}
			looping--;
		});
		event.setCanceled(true);
	}

	@Override
	public void onHurtTarget(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		event.getEntity().clearFire();
		event.getEntity().setTicksFrozen(FREEZE_TICKS);
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		if (golem.tickCount % INTERVAL != 0) return;

		AABB area = golem.getBoundingBox().inflate(RANGE);

		BlockPos.betweenClosedStream(area).forEach(pos -> {
			if (golem.level().getBlockState(pos).is(Blocks.FIRE)
					|| golem.level().getBlockState(pos).is(Blocks.SOUL_FIRE)) {
				golem.level().removeBlock(pos, false);
			}
		});

		List<LivingEntity> allies = golem.level().getEntitiesOfClass(LivingEntity.class, area,
				e -> e != golem && e.isAlive() && golem.isAlliedTo(e) && e.isOnFire());
		for (LivingEntity ally : allies) {
			ally.clearFire();
		}
	}
}
