package src.toi_et_moi.mgdp.modifier.goety_revelation;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.entity.common.GolemFlags;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Consumer;

public class TheCruelModifier extends GolemModifier {

	private static final int RANGE = 35;
	private static final int DURATION = 200;
	private static final int AMP = 4;
	private static final String TAG_BARRAGE = "mgdp_cruel_barrage";

	public TheCruelModifier() {
		super(StatFilterType.ATTACK, 1);
	}

	@Override
	public void onRegisterFlag(Consumer<GolemFlags> addFlag) {
		addFlag.accept(GolemFlags.FREEZE_IMMUNE);
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		if (!net.minecraftforge.fml.ModList.get().isLoaded("goety")) return;
		if (golem.tickCount % 20 != 0) return;

		var freezing = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("goety", "freezing"));
		var chillHide = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("goety", "chill_hide"));
		if (freezing != null) golem.removeEffect(freezing);

		// Aura
		var box = golem.getBoundingBox().inflate(RANGE);
		for (var entity : golem.level().getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive())) {
			if (entity.distanceToSqr(golem) > RANGE * RANGE) continue;
			if (entity == golem || entity.isAlliedTo(golem) || entity == golem.getOwner()) {
				if (chillHide != null) {
					entity.addEffect(new MobEffectInstance(chillHide, DURATION, AMP));
				}
			} else if (golem.canAttack(entity) && dev.xkmc.modulargolems.content.entity.targeting.TargetManager.wantsToAttack(golem, entity)) {
				entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, DURATION, AMP));
				if (freezing != null) {
					entity.addEffect(new MobEffectInstance(freezing, DURATION, AMP));
				}
			}
		}

		// Weather lock: conduit + cold biome = thunderstorm
		if (golem.level() instanceof ServerLevel wl) {
			boolean hasConduit = golem.getModifiers().containsKey(src.toi_et_moi.mgdp.init.MGDPModifiers.CONDUIT.get());
			boolean isCold = golem.level().getBiome(golem.blockPosition()).value().coldEnoughToSnow(golem.blockPosition());
			if (hasConduit && isCold && !wl.isThundering()) {
				wl.setWeatherParameters(0, 4800, true, true);
			}
		}

		// Cold biome: glacial walls (only with target) + healing
		if (golem.level().getBiome(golem.blockPosition()).value().coldEnoughToSnow(golem.blockPosition())) {
			LivingEntity wallTarget = golem.getTarget();
			if (wallTarget != null && wallTarget.isAlive() && golem.tickCount % 60 == 0) {
				int wallMult = golem.level().isRaining() || golem.level().isThundering() ? 3 : 2;
				try {
					var wallType = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation("goety", "glacial_wall"));
					if (wallType != null) {
						var ctor = Class.forName("com.Polarice3.Goety.common.entities.neutral.GlacialWall")
								.getConstructor(net.minecraft.world.entity.EntityType.class, net.minecraft.world.level.Level.class);
						for (int wi = 0; wi < wallMult; wi++) {
							var wall = ctor.newInstance(wallType, golem.level());
							var rng = golem.getRandom();
							var pos = golem.blockPosition().offset(rng.nextInt(5) - 2, 0, rng.nextInt(5) - 2);
							((net.minecraft.world.entity.Entity) wall).setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
							wall.getClass().getMethod("setOwnerId", java.util.UUID.class).invoke(wall, golem.getUUID());
							golem.level().addFreshEntity((net.minecraft.world.entity.Entity) wall);
						}
					}
				} catch (Exception e) {
					src.toi_et_moi.mgdp.Mgdp.LOGGER.warn("TheCruel: glacial_wall failed", e);
				}
			}
			if (golem.tickCount % 10 == 0) {
				try {
					var glacialClass = Class.forName("com.Polarice3.Goety.common.entities.neutral.GlacialWall");
					for (var entity : golem.level().getEntitiesOfClass(LivingEntity.class,
							golem.getBoundingBox().inflate(16), LivingEntity::isAlive)) {
						if (glacialClass.isInstance(entity)) {
							golem.heal(golem.getMaxHealth() * 0.05f);
							break;
						}
					}
				} catch (Exception ignored) {}
			}
		}

		// Handle ongoing spear barrage
		var data = golem.getPersistentData();
		int barrage = data.getInt(TAG_BARRAGE);
		if (barrage > 0) {
			if (golem.tickCount % 4 == 0) {
				try {
					var target = golem.getTarget();
					if (target != null && target.isAlive()) {
						float atk = Math.max(30, (float) golem.getAttributeValue(Attributes.ATTACK_DAMAGE));
						fireBarrageVolley(golem, target, atk);
					}
				} catch (Exception ignored) {}
				data.putInt(TAG_BARRAGE, barrage - 1);
			}
			return;
		}

		// Periodic ranged attacks
		if (golem.tickCount % 40 != 0) return;
		LivingEntity target = golem.getTarget();
		if (target == null || !target.isAlive()) return;
		if (!golem.getSensing().hasLineOfSight(target) && target.distanceToSqr(golem) > 400) return;

		try {
			var rng = golem.getRandom();
			float atk = Math.max(30, (float) golem.getAttributeValue(Attributes.ATTACK_DAMAGE));
			int mult = 1;
			if (golem.level().getBiome(golem.blockPosition()).value().coldEnoughToSnow(golem.blockPosition())) {
				mult = 2;
				if (golem.level().isRaining() || golem.level().isThundering()) mult = 3;
			}
			int choice = rng.nextInt(3);

			if (choice == 0) {
				data.putInt(TAG_BARRAGE, 8 * mult);
			} else if (choice == 1) {
				var chunkType = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation("goety", "ice_chunk"));
				if (chunkType != null) {
					var ctor = Class.forName("com.Polarice3.Goety.common.entities.projectiles.IceChunk")
							.getConstructor(net.minecraft.world.level.Level.class, LivingEntity.class, LivingEntity.class);
					var rng2 = golem.getRandom();
					var box2 = golem.getBoundingBox().inflate(35);
					var allTargets = golem.level().getEntitiesOfClass(LivingEntity.class, box2,
							e -> e.isAlive() && golem.canAttack(e) && dev.xkmc.modulargolems.content.entity.targeting.TargetManager.wantsToAttack(golem, e));
					for (var tgt : allTargets) {
						for (int ci = 0; ci < mult; ci++) {
							var chunk = ctor.newInstance(golem.level(), golem, tgt);
							chunk.getClass().getMethod("setExtraDamage", float.class).invoke(chunk, atk * 0.75F);
							var p = tgt.blockPosition().offset(rng2.nextInt(5) - 2, 0, rng2.nextInt(5) - 2);
							int spawnY = tgt.blockPosition().getY() + 10 + rng2.nextInt(5);
							int maxH = golem.level().getMaxBuildHeight() - 1;
							if (spawnY > maxH) spawnY = maxH;
							boolean clear = true;
							for (int cy = tgt.blockPosition().getY() + 1; cy <= spawnY; cy++) {
								if (!golem.level().isEmptyBlock(new net.minecraft.core.BlockPos(p.getX(), cy, p.getZ()))) {
									clear = false;
									break;
								}
							}
							if (clear) {
								((net.minecraft.world.entity.Entity) chunk).setPos(p.getX() + 0.5, spawnY, p.getZ() + 0.5);
								golem.level().addFreshEntity((net.minecraft.world.entity.Entity) chunk);
							}
						}
					}
				}
			} else if (choice == 2) {
				var cycloneType = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation("goety", "cyclone"));
				if (cycloneType != null) {
					var ctor = Class.forName("com.Polarice3.Goety.common.entities.projectiles.Cyclone")
							.getConstructor(net.minecraft.world.level.Level.class, LivingEntity.class, double.class, double.class, double.class);
					double dx = target.getX() - golem.getX();
					double dy = target.getEyeY() - golem.getEyeY();
					double dz = target.getZ() - golem.getZ();
					double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
					if (d > 0.01) {
						double nx = dx / d, ny = dy / d, nz = dz / d;
						double px = -nz, pz = nx;
						for (int i = -2 * mult; i <= 2 * mult; i++) {
							Object cyclone = ctor.newInstance(golem.level(), golem, nx * 1.5, ny * 1.5, nz * 1.5);
							cyclone.getClass().getMethod("setSize", float.class).invoke(cyclone, 5.0F);
							cyclone.getClass().getMethod("setExtraDamage", float.class).invoke(cyclone, atk);
							double ox = golem.getX() + px * i * 3.0;
							double oz = golem.getZ() + pz * i * 3.0;
							((net.minecraft.world.entity.Entity) cyclone).setPos(ox, golem.getEyeY() - 0.3, oz);
							golem.level().addFreshEntity((net.minecraft.world.entity.Entity) cyclone);
						}
					}
				}
			}
		} catch (Exception e) {
			src.toi_et_moi.mgdp.Mgdp.LOGGER.warn("TheCruel: attack failed", e);
		}
	}

	private void fireBarrageVolley(AbstractGolemEntity<?, ?> golem, LivingEntity target, float atk) throws Exception {
		var rng = golem.getRandom();
		var spearType = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation("goety", "ice_spear"));
		if (spearType == null) return;

		var ctor = Class.forName("com.Polarice3.Goety.common.entities.projectiles.IceSpear")
				.getConstructor(LivingEntity.class, net.minecraft.world.level.Level.class);
		double dx = target.getX() - golem.getX();
		double dz = target.getZ() - golem.getZ();
		double baseYaw = Math.atan2(dz, dx);
		double dist = Math.sqrt(dx * dx + dz * dz);

		int count = 4 + rng.nextInt(2);
		for (int i = 0; i < count; i++) {
			Projectile spear = (Projectile) ctor.newInstance(golem, golem.level());
			spear.getClass().getMethod("setExtraDamage", float.class).invoke(spear, atk * 0.25F);
			double spread = (rng.nextDouble() - 0.5) * 0.8;
			double yaw = baseYaw + spread;
			double pitch = 0.075 + rng.nextDouble() * 0.125;
			spear.setPos(golem.getX(), golem.getEyeY() - 0.3 + (rng.nextDouble() - 0.5) * 0.5, golem.getZ());
			spear.setDeltaMovement(Math.cos(yaw) * 2.0, pitch * 2.0, Math.sin(yaw) * 2.0);
			spear.hasImpulse = true;
			spear.getPersistentData().putBoolean("mgdp_cruel_barrage", true);
			golem.level().addFreshEntity(spear);
		}

		// Mix in one Ice Storm
		int stormCount = 1;
		for (int si = 0; si < stormCount; si++) {
			var stormType = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation("goety", "ice_storm"));
			if (stormType != null) {
				var stormCtor = Class.forName("com.Polarice3.Goety.common.entities.projectiles.IceStorm")
						.getConstructor(LivingEntity.class, double.class, double.class, double.class, net.minecraft.world.level.Level.class);
				double dy = target.getEyeY() - golem.getEyeY();
				double d = Math.sqrt(dist * dist + dy * dy);
				if (d > 0.01) {
					Projectile storm = (Projectile) stormCtor.newInstance(golem, dx / d * 1.5, dy / d * 1.5, dz / d * 1.5, golem.level());
					storm.getClass().getMethod("setExtraDamage", float.class).invoke(storm, atk * 0.5F);
					storm.setPos(golem.getX(), golem.getEyeY() - 0.3, golem.getZ());
					golem.level().addFreshEntity(storm);
				}
			}
		}
	}

	@Override
	public void onHurtTarget(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		if (!net.minecraftforge.fml.ModList.get().isLoaded("goety")) return;
		var source = event.getSource();
		boolean isFrost = false;
		for (var frost : new ResourceLocation[]{
				new ResourceLocation("goety", "direct_freeze"),
				new ResourceLocation("goety", "indirect_freeze"),
				new ResourceLocation("goety", "ice_spike"),
				new ResourceLocation("goety", "ice_bouquet"),
				new ResourceLocation("goety", "frost_breath"),
		}) {
			if (source.is(ResourceKey.create(Registries.DAMAGE_TYPE, frost))) {
				isFrost = true;
				break;
			}
		}
		if (!isFrost) return;

		float multiplier = 4.0F;
		if (golem.level().getBiome(golem.blockPosition()).value().coldEnoughToSnow(golem.blockPosition())) {
			multiplier = 7.0F;
			if (golem.level().isRaining() || golem.level().isThundering()) {
				multiplier = 10.99F;
			}
		}
		event.setAmount(event.getAmount() * multiplier);
	}

	@Override
	public void onAttacked(AbstractGolemEntity<?, ?> golem, LivingAttackEvent event, int level) {
		if (!net.minecraftforge.fml.ModList.get().isLoaded("goety")) return;
		var src = event.getSource();
		for (var frost : new ResourceLocation[]{
				new ResourceLocation("goety", "direct_freeze"),
				new ResourceLocation("goety", "indirect_freeze"),
				new ResourceLocation("goety", "ice_spike"),
				new ResourceLocation("goety", "ice_bouquet"),
				new ResourceLocation("goety", "frost_breath"),
		}) {
			if (src.is(ResourceKey.create(Registries.DAMAGE_TYPE, frost))) {
				event.setCanceled(true);
				return;
			}
		}
		if (src.is(net.minecraft.world.damagesource.DamageTypes.FREEZE)) {
			event.setCanceled(true);
		}
	}
}
