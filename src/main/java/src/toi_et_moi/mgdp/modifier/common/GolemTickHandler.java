package src.toi_et_moi.mgdp.modifier.common;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.entity.mode.GolemModes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import src.toi_et_moi.mgdp.Mgdp;
import src.toi_et_moi.mgdp.init.MGDPModifiers;
import src.toi_et_moi.mgdp.jukebox.JukeboxGolem;

@Mod.EventBusSubscriber(modid = Mgdp.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GolemTickHandler {

	@SubscribeEvent
	public static void onGolemTick(LivingEvent.LivingTickEvent event) {
		if (!(event.getEntity() instanceof AbstractGolemEntity<?, ?> golem)) return;

		tickCreateCompat(golem);
		tickUnstoppable(golem);
		tickProjectileDodge(golem);
		tickJukebox(golem);
	}

	private static void tickCreateCompat(AbstractGolemEntity<?, ?> golem) {
		if (golem.level().isClientSide) return;
		if (!ModList.get().isLoaded("create")) return;
		if (golem.getMode() != GolemModes.STAND) return;

		try {
			Class<?> cc = Class.forName("src.toi_et_moi.mgdp.compat.CreateCompat");
			cc.getMethod("tryDriveHandCrank", AbstractGolemEntity.class).invoke(null, golem);
		} catch (Exception ignored) {}
	}

	private static void tickUnstoppable(AbstractGolemEntity<?, ?> golem) {
		if (golem.level().isClientSide) return;
		if (!golem.getModifiers().containsKey(MGDPModifiers.UNSTOPPABLE.get())) return;

		var attr = golem.getAttribute(Attributes.MOVEMENT_SPEED);
		if (attr == null || attr.getValue() >= attr.getBaseValue()) return;
		var toRemove = attr.getModifiers().stream()
				.filter(m -> m.getAmount() < 0)
				.map(AttributeModifier::getId)
				.toList();
		toRemove.forEach(attr::removeModifier);
	}

	private static void tickProjectileDodge(AbstractGolemEntity<?, ?> golem) {
		if (golem.level().isClientSide()) return;
		if (!golem.getModifiers().containsKey(MGDPModifiers.PROJECTILE_DODGE.get())) return;

		try {
			Vec3 golemPos = golem.position();
			for (Entity e : golem.level().getEntitiesOfClass(Entity.class,
					golem.getBoundingBox().inflate(10))) {
				if (e == golem) continue;
				if (e instanceof AbstractArrow && e.tickCount > 100) continue;
				Vec3 vel = e.getDeltaMovement();
				if (vel.lengthSqr() < (e instanceof Projectile ? 0.01 : 0.5)) continue;
				Entity owner = e instanceof Projectile ? ((Projectile) e).getOwner() : null;
				if (owner == golem || owner == golem.getOwner()) continue;
				Vec3 rel = golemPos.subtract(e.position());
				double t = rel.dot(vel) / vel.lengthSqr();
				if (t < 0 || t > 15) continue;
				Vec3 closest = e.position().add(vel.scale(t));
				if (closest.distanceToSqr(golemPos) > 9) continue;
				Vec3 dodge = vel.cross(new Vec3(0, 1, 0)).normalize();
				if (dodge.lengthSqr() < 0.5) dodge = new Vec3(1, 0, 0);
				float strength = t < 5 ? 1.0f : 0.5f;
				Vec3 newVel = golem.getDeltaMovement().add(dodge.scale(strength));
				if (Math.abs(newVel.x) < 100 && Math.abs(newVel.z) < 100) {
					golem.setDeltaMovement(newVel);
				}
				break;
			}
		} catch (Exception ignored) {}
	}

	private static void tickJukebox(AbstractGolemEntity<?, ?> golem) {
		if (golem.level().isClientSide) return;
		if (!(golem instanceof JukeboxGolem jb)) return;

		// Auto-stop if disc removed while playing
		if (jb.mgdp$isPlaying() && jb.mgdp$getDisc().isEmpty()) {
			jb.mgdp$setPlaying(false);
			jb.mgdp$setTick(0);
			golem.level().levelEvent(null, 1011, golem.blockPosition(), 0);
			return;
		}

		if (!jb.mgdp$isPlaying()) return;
		jb.mgdp$setTick(jb.mgdp$getTick() + 1);

		// Safety auto-stop after ~5 minutes
		if (jb.mgdp$getTick() > 20 * 60 * 5) {
			jb.mgdp$setPlaying(false);
			jb.mgdp$setTick(0);
			golem.level().levelEvent(null, 1011, golem.blockPosition(), 0);
		}
	}
}
