package src.toi_et_moi.mgdp.modifier.common;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.entity.dog.DogGolemEntity;
import dev.xkmc.modulargolems.content.entity.dog.DogGolemEntity;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import src.toi_et_moi.mgdp.Mgdp;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

@Mod.EventBusSubscriber(modid = Mgdp.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GolemFallHandler {

	private static final double MAX_DIST = 32.0;

	// === Void rescue: teleport player to a flying dog-golem when falling into the void ===
	@SubscribeEvent
	public static void onVoidDamage(LivingAttackEvent evt) {
		if (!(evt.getEntity() instanceof Player player)) return;
		if (!evt.getSource().is(DamageTypes.FELL_OUT_OF_WORLD)) return;
		if (player.isPassenger()) return;

		AABB area = player.getBoundingBox().inflate(64);
		for (var golem : player.level().getEntitiesOfClass(DogGolemEntity.class, area,
				e -> e.isAlive() && (e.getModifiers().containsKey(MGDPModifiers.FLIGHT.get())
						|| e.getModifiers().containsKey(MGDPModifiers.ROCKET_FLIGHT.get())))) {
			if (!golem.hasPassenger(player)) {
				evt.setCanceled(true);
				player.startRiding(golem, true);
				break;
			}
		}
	}

	// === Fall rescue: when a player falls from a height, teleport to the nearest owned dog-golem ===
	@SubscribeEvent
	public static void onLivingTick(LivingEvent.LivingTickEvent event) {
		if (!(event.getEntity() instanceof Player player)) return;
		if (player.isPassenger()) return;
		if (player.onGround()) return;
		if (player.fallDistance <= 3.0 && player.getDeltaMovement().y >= -1.5) return;

		// Skip if player already has fall immunity
		if (player.isCreative() || player.isSpectator()) return;
		if (player.getAbilities().flying) return;
		if (player.hasEffect(net.minecraft.world.effect.MobEffects.SLOW_FALLING)) return;

		DogGolemEntity bestGolem = null;
		double bestDist = Double.MAX_VALUE;

		for (DogGolemEntity golem : player.level().getEntitiesOfClass(
				DogGolemEntity.class,
				player.getBoundingBox().inflate(MAX_DIST),
				g -> !g.isVehicle() && g.isAlive() && player.getUUID().equals(g.getOwnerUUID())
		)) {
			double dist = player.distanceToSqr(golem);
			if (dist < bestDist) {
				bestDist = dist;
				bestGolem = golem;
			}
		}

		if (bestGolem == null) return;

		player.startRiding(bestGolem, true);
		player.fallDistance = 0;
	}
}
