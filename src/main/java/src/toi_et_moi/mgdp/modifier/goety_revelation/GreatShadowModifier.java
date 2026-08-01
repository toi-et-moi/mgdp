package src.toi_et_moi.mgdp.modifier.goety_revelation;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.entity.targeting.TargetManager;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class GreatShadowModifier extends GolemModifier {

	private static final int RANGE = 35;
	private static final int DURATION = 200;
	private static final int AMP = 4;
	private static final String TAG_BURST = "mgdp_shadow_burst";
	private static final String TAG_BURST_TYPE = "mgdp_shadow_burst_type";

	public GreatShadowModifier() {
		super(StatFilterType.ATTACK, 1);
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		if (!net.minecraftforge.fml.ModList.get().isLoaded("goety")) return;

		// Ongoing bolt barrage: one volley every 4 ticks
		var data = golem.getPersistentData();
		int burst = data.getInt(TAG_BURST);
		if (burst > 0) {
			if (golem.tickCount % 4 == 0) {
				var target = golem.getTarget();
				if (target != null && target.isAlive()) {
					try {
						float atk = (float) golem.getAttributeValue(Attributes.ATTACK_DAMAGE);
						fireVolley(golem, target, atk, data.getInt(TAG_BURST_TYPE));
					} catch (Exception e) {
						src.toi_et_moi.mgdp.Mgdp.LOGGER.warn("TheGreatShadow: volley failed", e);
					}
				}
				data.putInt(TAG_BURST, burst - 1);
			}
			return;
		}

		if (golem.tickCount % 20 != 0) return;

		// Darkness V + Blindness V + Wild Rage aura on enemies
		var wildRage = net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS
				.getValue(new ResourceLocation("goety", "wild_rage"));
		var box = golem.getBoundingBox().inflate(RANGE);
		for (var entity : golem.level().getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive())) {
			if (entity.distanceToSqr(golem) > RANGE * RANGE) continue;
			if (entity == golem || entity.isAlliedTo(golem) || entity == golem.getOwner()) continue;
			if (golem.canAttack(entity) && TargetManager.wantsToAttack(golem, entity)) {
				entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, DURATION, AMP));
				entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, DURATION, AMP));
				if (wildRage != null) {
					entity.addEffect(new MobEffectInstance(wildRage, DURATION, 0));
				}
			}
		}

		// Periodic ranged attack
		if (golem.tickCount % 40 != 0) return;
		LivingEntity target = golem.getTarget();
		if (target == null || !target.isAlive()) return;
		if (!golem.getSensing().hasLineOfSight(target) && target.distanceToSqr(golem) > 400) return;

		var rng = golem.getRandom();
		int choice = rng.nextInt(3);
		data.putInt(TAG_BURST_TYPE, choice);
		data.putInt(TAG_BURST, 4);
	}

	/** Fires a volley of 4-5 projectiles at the target with slight spread. */
	private static void fireVolley(AbstractGolemEntity<?, ?> golem, LivingEntity target, float atk, int type) throws Exception {
		var rng = golem.getRandom();
		int count = 4 + rng.nextInt(2);
		for (int i = 0; i < count; i++) {
			double sx = 0, sy = 0, sz = 0;
			if (type == 2) {
				// Skulls explode on contact: spawn at a random spot around the golem's hitbox, then aim from there
				double offset = golem.getBbWidth() * 0.5 + 1.5;
				var spawnPos = new net.minecraft.core.BlockPos.MutableBlockPos();
				boolean placed = false;
				for (int attempt = 0; attempt < 8 && !placed; attempt++) {
					double angle = rng.nextDouble() * Math.PI * 2;
					sx = golem.getX() + Math.cos(angle) * offset;
					sy = golem.getEyeY() - 0.3 + (rng.nextDouble() - 0.5) * golem.getBbHeight() * 0.75;
					sz = golem.getZ() + Math.sin(angle) * offset;
					spawnPos.set(sx, sy, sz);
					if (golem.level().getBlockState(spawnPos).getCollisionShape(golem.level(), spawnPos).isEmpty()) {
						placed = true;
					}
				}
				if (!placed) continue; // all attempts blocked: skip this skull instead of exploding in a wall
			} else {
				sx = golem.getX();
				sy = golem.getEyeY() - 0.3;
				sz = golem.getZ();
			}

			double vx = target.getX() - sx;
			double vy = target.getEyeY() - sy;
			double vz = target.getZ() - sz;
			double vlen = Math.sqrt(vx * vx + vy * vy + vz * vz);
			if (vlen <= 0.01) continue;
			vx = vx / vlen + (rng.nextDouble() - 0.5) * 0.3;
			vy = vy / vlen + (rng.nextDouble() - 0.5) * 0.3;
			vz = vz / vlen + (rng.nextDouble() - 0.5) * 0.3;
			vlen = Math.sqrt(vx * vx + vy * vy + vz * vz);
			vx /= vlen; vy /= vlen; vz /= vlen;

			Projectile bolt = createProjectile(golem, vx, vy, vz, type);
			if (bolt == null) continue;
			bolt.getClass().getMethod("setExtraDamage", float.class).invoke(bolt, atk * 0.15F);
			bolt.setPos(sx, sy, sz);
			bolt.setDeltaMovement(vx * 1.1, vy * 1.1, vz * 1.1);
			bolt.hasImpulse = true;
			golem.level().addFreshEntity(bolt);
		}
	}

	private static Projectile createProjectile(AbstractGolemEntity<?, ?> golem, double vx, double vy, double vz, int type) {
		try {
			if (type == 0) {
				var ctor = Class.forName("com.Polarice3.Goety.common.entities.projectiles.MagicBolt")
						.getConstructor(net.minecraft.world.level.Level.class, LivingEntity.class,
								double.class, double.class, double.class);
				return (Projectile) ctor.newInstance(golem.level(), golem, vx * 0.25, vy * 0.25, vz * 0.25);
			} else if (type == 1) {
				var ctor = Class.forName("com.Polarice3.Goety.common.entities.projectiles.NecroBolt")
						.getConstructor(LivingEntity.class, double.class, double.class, double.class,
								net.minecraft.world.level.Level.class);
				return (Projectile) ctor.newInstance(golem, vx * 0.125, vy * 0.125, vz * 0.125, golem.level());
			} else {
				var ctor = Class.forName("com.Polarice3.Goety.common.entities.projectiles.HauntedSkullProjectile")
						.getConstructor(LivingEntity.class, double.class, double.class, double.class,
								net.minecraft.world.level.Level.class);
				return (Projectile) ctor.newInstance(golem, vx * 0.05, vy * 0.05, vz * 0.05, golem.level());
			}
		} catch (Exception e) {
			src.toi_et_moi.mgdp.Mgdp.LOGGER.warn("TheGreatShadow: bolt creation failed", e);
			return null;
		}
	}

	@Override
	public void onHurtTarget(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		if (golem.level().isClientSide()) return;
		var target = event.getEntity();
		if (!golem.canAttack(target)) return;
		if (target == golem || target == golem.getOwner()) return;

		// +300% damage: magic damage and undead/debuffed target bonuses stack multiplicatively
		var source = event.getSource();
		boolean isMagic = source.is(ResourceKey.create(Registries.DAMAGE_TYPE,
				new ResourceLocation("goety", "magic_bolt")))
				|| source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC);
		boolean isUndead = target.getMobType() == MobType.UNDEAD;
		boolean isDebuffed = target.hasEffect(MobEffects.DARKNESS) || target.hasEffect(MobEffects.BLINDNESS);

		float mult = 1.0F;
		if (isMagic) mult += 3.0F;
		if (isUndead || isDebuffed) mult += 3.0F;
		event.setAmount(event.getAmount() * mult);
	}

	@Override
	public void onHurt(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		if (golem.level().isClientSide()) return;
		var source = event.getSource();

		// -80% damage from undead attackers
		var attacker = source.getEntity();
		if (attacker == null) attacker = source.getDirectEntity();
		boolean fromUndead = attacker instanceof LivingEntity le && le.getMobType() == MobType.UNDEAD;

		// -80% magic damage (vanilla magic + goety magic types)
		boolean isMagic = source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC)
				|| source.is(DamageTypes.WITHER)
				|| source.is(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("goety", "magic_bolt")))
				|| source.is(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("goety", "magic_fire")))
				|| source.is(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("goety", "magic_fireball")))
				|| source.is(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("goety", "no_owner_magic_fireball")));

		if (fromUndead || isMagic) {
			event.setAmount(event.getAmount() * 0.2F);
		}
	}
}
