package src.toi_et_moi.mgdp.modifier.combat;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class TridentFestivalModifier extends GolemModifier {

	public TridentFestivalModifier() {
		super(StatFilterType.MASS, 1);
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		LivingEntity target = golem.getTarget();
		if (target == null) return;
		if (golem.tickCount % 100 != 0) return; // every 5 seconds

		Level levelWorld = golem.level();
		int count = 4 + level * 2;

		for (int i = 0; i < count; i++) {
			double angle = 2 * Math.PI * i / count;
			double radius = 2.5;
			Vec3 spawnPos = golem.position().add(
					Math.cos(angle) * radius,
					1.5 + golem.getRandom().nextDouble(),
					Math.sin(angle) * radius
			);

			ThrownTrident trident = new ThrownTrident(levelWorld, golem, new ItemStack(Items.TRIDENT));
			trident.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
			trident.pickup = ThrownTrident.Pickup.CREATIVE_ONLY;

			// Velocity toward target
			Vec3 toTarget = target.position().add(0, target.getBbHeight() / 2, 0)
					.subtract(spawnPos).normalize().scale(2.5);
			trident.setDeltaMovement(toTarget);
			trident.hasImpulse = true;

			// Tag for mixin to identify
			trident.addTag("mgdp_trident_festival");

			levelWorld.addFreshEntity(trident);
		}

		// Explode all existing tagged tridents (including landed ones)
		for (ThrownTrident existing : levelWorld.getEntitiesOfClass(ThrownTrident.class,
				golem.getBoundingBox().inflate(32),
				t -> t.getTags().contains("mgdp_trident_festival"))) {
			onTridentHit(existing, levelWorld, existing.blockPosition());
		}
	}

	/** Called by mixin when a tagged trident hits something */
	public static void onTridentHit(ThrownTrident trident, Level level, BlockPos pos) {
		if (level.isClientSide()) return;
		if (!(level instanceof ServerLevel sl)) return;

		Entity owner = trident.getOwner();
		if (owner == null) owner = trident;

		// Visual explosion + block destruction (if enabled)
		sl.explode(owner, pos.getX(), pos.getY(), pos.getZ(), 6.0f,
				src.toi_et_moi.mgdp.Config.destructionMode
						? Level.ExplosionInteraction.BLOCK
						: Level.ExplosionInteraction.NONE);

		// Manual damage (bypasses invulnerability frames, distance falloff)
		for (net.minecraft.world.entity.LivingEntity target : sl.getEntitiesOfClass(
				net.minecraft.world.entity.LivingEntity.class,
				(new net.minecraft.world.phys.AABB(pos).inflate(6.0)),
				e -> e.isAlive() && !e.isSpectator())) {
			double dist = target.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());
			float factor = (float) Math.max(0.0, 1.0 - dist / 36.0);
			target.invulnerableTime = 0;
			target.hurt(sl.damageSources().explosion(owner, owner), 145.0f * factor);
		}

		// Lightning strikes (visual only)
		for (int i = 0; i < 5; i++) {
			LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(sl);
			if (bolt != null) {
				double ox = (sl.random.nextDouble() - 0.5) * 6;
				double oz = (sl.random.nextDouble() - 0.5) * 6;
				bolt.setPos(pos.getX() + ox, pos.getY(), pos.getZ() + oz);
				bolt.setVisualOnly(true);
				sl.addFreshEntity(bolt);
			}
		}
	}
}
