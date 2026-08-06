package src.toi_et_moi.mgdp.modifier.goety_revelation;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.entity.targeting.TargetManager;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class DefilerModifier extends GolemModifier {

	private static final int RANGE = 35;
	private static final int DURATION = 200;
	private static final int AMP = 4;
	private static final String TAG_BURST = "mgdp_defiler_burst";

	public DefilerModifier() {
		super(StatFilterType.ATTACK, 1);
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		if (!net.minecraftforge.fml.ModList.get().isLoaded("goety")) return;

		// Ongoing poison quill barrage: one volley every 4 ticks
		var data = golem.getPersistentData();
		int burst = data.getInt(TAG_BURST);
		if (burst > 0) {
			if (golem.tickCount % 4 == 0) {
				var target = golem.getTarget();
				if (target != null && target.isAlive()) {
					try {
						float atk = (float) golem.getAttributeValue(Attributes.ATTACK_DAMAGE);
						fireQuillVolley(golem, target, atk);
					} catch (Exception e) {
						src.toi_et_moi.mgdp.Mgdp.LOGGER.warn("TheDefiler: quill volley failed", e);
					}
				}
				data.putInt(TAG_BURST, burst - 1);
			}
			return;
		}

		if (golem.tickCount % 20 != 0) return;

		// Venom V aura on enemies
		var venom = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("goety", "acid_venom"));
		var box = golem.getBoundingBox().inflate(RANGE);
		for (var entity : golem.level().getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive())) {
			if (entity.distanceToSqr(golem) > RANGE * RANGE) continue;
			if (entity == golem || entity.isAlliedTo(golem) || entity == golem.getOwner()) continue;
			if (golem.canAttack(entity) && TargetManager.wantsToAttack(golem, entity)) {
				entity.addEffect(new MobEffectInstance(venom, DURATION, AMP));
			}
		}

		// Periodic attacks (only while a target is locked)
		LivingEntity target = golem.getTarget();
		boolean hasTarget = target != null && target.isAlive();
		if (!hasTarget) return;
		float atk = (float) golem.getAttributeValue(Attributes.ATTACK_DAMAGE);

		if (golem.tickCount % 40 == 0) {
			data.putInt(TAG_BURST, 3);
		}
		if (golem.tickCount % 60 == 0) {
			spawnAcidPools(golem, atk);
		}
		if (golem.tickCount % 80 == 0) {
			spawnVinesAndThorns(golem, atk);
		}
	}

	/** Fires a volley of 4-5 piercing poison quills at the target with slight spread. */
	private static void fireQuillVolley(AbstractGolemEntity<?, ?> golem, LivingEntity target, float atk) throws Exception {
		var rng = golem.getRandom();
		var ctor = Class.forName("com.Polarice3.Goety.common.entities.projectiles.PoisonQuill")
				.getConstructor(Level.class, LivingEntity.class);
		int count = 4 + rng.nextInt(2);
		for (int i = 0; i < count; i++) {
			double dx = target.getX() - golem.getX();
			double dy = target.getEyeY() - golem.getEyeY();
			double dz = target.getZ() - golem.getZ();
			double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
			if (d <= 0.01) continue;
			double vx = dx / d + (rng.nextDouble() - 0.5) * 0.25;
			double vy = dy / d + (rng.nextDouble() - 0.5) * 0.25;
			double vz = dz / d + (rng.nextDouble() - 0.5) * 0.25;
			double vlen = Math.sqrt(vx * vx + vy * vy + vz * vz);
			vx /= vlen; vy /= vlen; vz /= vlen;

			AbstractArrow quill = (AbstractArrow) ctor.newInstance(golem.level(), golem);
			quill.setPos(golem.getX(), golem.getEyeY() - 0.3, golem.getZ());
			quill.setDeltaMovement(vx * 2.5, vy * 2.5, vz * 2.5);
			quill.hasImpulse = true;
			boolean all = golem.getModifiers().containsKey(src.toi_et_moi.mgdp.init.MGDPModifiers.THE_GENESIS.get());
			quill.getClass().getMethod("setExtraDamage", float.class).invoke(quill, atk * (all ? 0.45F : 0.35F));
			quill.getClass().getMethod("setDuration", int.class).invoke(quill, all ? 8 : 5);
			quill.getClass().getMethod("setSpear", boolean.class, int.class).invoke(quill, true, 2);
			golem.level().addFreshEntity(quill);
		}
	}

	/** Spawns several acid pools on the ground around the golem. */
	private static void spawnAcidPools(AbstractGolemEntity<?, ?> golem, float atk) {
		try {
			var poolType = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation("goety", "acid_pool"));
			if (poolType == null) return;
			var ctor = Class.forName("com.Polarice3.Goety.common.entities.projectiles.AcidPool")
					.getConstructor(net.minecraft.world.entity.EntityType.class, Level.class);
			var rng = golem.getRandom();
			int count = 6 + rng.nextInt(4);
			for (int i = 0; i < count; i++) {
				var pool = ctor.newInstance(poolType, golem.level());
				var pos = golem.blockPosition().offset(rng.nextInt(13) - 10, 0, rng.nextInt(13) - 10);
				((Entity) pool).setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
				pool.getClass().getMethod("setRadius", float.class).invoke(pool, 3.0F + rng.nextFloat());
				boolean all = golem.getModifiers().containsKey(src.toi_et_moi.mgdp.init.MGDPModifiers.THE_GENESIS.get());
				pool.getClass().getMethod("setDamage", float.class).invoke(pool, atk * (all ? 0.75F : 0.5F));
				pool.getClass().getMethod("setDuration", int.class).invoke(pool, 60);
				pool.getClass().getMethod("setOwner", LivingEntity.class).invoke(pool, golem);
				golem.level().addFreshEntity((Entity) pool);
			}
		} catch (Exception e) {
			src.toi_et_moi.mgdp.Mgdp.LOGGER.warn("TheDefiler: acid pool failed", e);
		}
	}

	/** Summons blossom thorns and entangle vines at the positions of all surrounding targets. */
	private static void spawnVinesAndThorns(AbstractGolemEntity<?, ?> golem, float atk) {
		try {
			var box = golem.getBoundingBox().inflate(RANGE);
			for (var tgt : golem.level().getEntitiesOfClass(LivingEntity.class, box,
					e -> e.isAlive() && golem.canAttack(e) && TargetManager.wantsToAttack(golem, e))) {
				if (tgt.distanceToSqr(golem) > RANGE * RANGE) continue;

				var thornType = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation("goety", "blossom_thorn"));
				if (thornType != null) {
					var thornCtor = Class.forName("com.Polarice3.Goety.common.entities.projectiles.BlossomThorn")
							.getConstructor(Level.class, double.class, double.class, double.class, int.class, LivingEntity.class);
					var thorn = thornCtor.newInstance(golem.level(), tgt.getX(), tgt.getY(), tgt.getZ(), 20, golem);
					boolean all = golem.getModifiers().containsKey(src.toi_et_moi.mgdp.init.MGDPModifiers.THE_GENESIS.get());
					thorn.getClass().getMethod("setExtraDamage", float.class).invoke(thorn, atk * (all ? 0.9F : 0.6F));
					golem.level().addFreshEntity((Entity) thorn);
				}

				var vineType = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation("goety", "entangle_vines"));
				if (vineType != null) {
					var vineCtor = Class.forName("com.Polarice3.Goety.common.entities.projectiles.EntangleVines")
							.getConstructor(Level.class, LivingEntity.class, Entity.class);
					var vine = vineCtor.newInstance(golem.level(), golem, tgt);
					golem.level().addFreshEntity((Entity) vine);
				}
			}
		} catch (Exception e) {
			src.toi_et_moi.mgdp.Mgdp.LOGGER.warn("TheDefiler: vines/thorns failed", e);
		}
	}

	@Override
	public void onHurtTarget(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		if (golem.level().isClientSide()) return;
		var target = event.getEntity();
		if (!golem.canAttack(target)) return;
		if (target == golem || target == golem.getOwner()) return;

		// +300% damage: arthropods, or any target currently under a debuff
		boolean isArthropod = target.getType().is(TagKey.create(
				net.minecraft.core.registries.Registries.ENTITY_TYPE, new ResourceLocation("minecraft", "arthropod")));
		boolean hasDebuff = false;
		for (var effect : target.getActiveEffects()) {
			if (!effect.getEffect().isBeneficial()) {
				hasDebuff = true;
				break;
			}
		}
		if (isArthropod || hasDebuff) {
			boolean all = golem.getModifiers().containsKey(src.toi_et_moi.mgdp.init.MGDPModifiers.THE_GENESIS.get());
			event.setAmount(event.getAmount() * (all ? 5.5F : 4.0F));
		}
	}
}
