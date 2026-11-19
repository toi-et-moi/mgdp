package src.toi_et_moi.mgdp.handler;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import src.toi_et_moi.mgdp.Config;
import src.toi_et_moi.mgdp.Mgdp;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles the "mobs target by golems fight back" feature.
 * <p>
 * Modular Golems 2.7.5 moved {@code mob.setTarget(self)} out of
 * {@code AbstractGolemEntity.setTarget} and into the default method
 * {@code GolemTargetImpl.postSetTarget}, so the old mixin redirect no longer
 * matched. The mixin now wraps the {@code postSetTarget} call and opens a
 * context flag; the Forge event below applies the config toggle and the
 * per-mob cooldown only while that context is active.
 */
@Mod.EventBusSubscriber(modid = Mgdp.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AutoAggroHandler {

	private static final long AGGRO_COOLDOWN = 100;
	private static final long AGGRO_CLEANUP_AGE = 200;

	private static final Map<UUID, Long> LAST_AGGRO = new HashMap<>();
	private static final ThreadLocal<Integer> CONTEXT = ThreadLocal.withInitial(() -> 0);
	private static int cleanTick;

	private AutoAggroHandler() {
	}

	public static void enterPostSetTarget() {
		CONTEXT.set(CONTEXT.get() + 1);
	}

	public static void exitPostSetTarget() {
		int depth = CONTEXT.get() - 1;
		if (depth <= 0) {
			CONTEXT.remove();
		} else {
			CONTEXT.set(depth);
		}
	}

	public static void clear(UUID id) {
		LAST_AGGRO.remove(id);
	}

	@SubscribeEvent
	public static void onChangeTarget(LivingChangeTargetEvent event) {
		if (CONTEXT.get() <= 0) return;
		if (!(event.getEntity() instanceof Mob mob)) return;
		if (!(event.getNewTarget() instanceof AbstractGolemEntity<?, ?>)) return;

		if (!Config.mobAutoAggro) {
			event.setCanceled(true);
			return;
		}

		long now = mob.level().getGameTime();
		Long last = LAST_AGGRO.get(mob.getUUID());
		if (last != null && now - last < AGGRO_COOLDOWN) {
			event.setCanceled(true);
			return;
		}

		LAST_AGGRO.put(mob.getUUID(), now);

		// Periodically purge entries older than 10s (200 ticks)
		if (++cleanTick % 100 == 0) {
			LAST_AGGRO.values().removeIf(v -> now - v > AGGRO_CLEANUP_AGE);
		}
	}
}
