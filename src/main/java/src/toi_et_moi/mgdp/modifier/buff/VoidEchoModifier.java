package src.toi_et_moi.mgdp.modifier.buff;

import dev.xkmc.l2damagetracker.contents.attack.CreateSourceEvent;
import dev.xkmc.l2damagetracker.contents.damage.DefaultDamageState;
import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.entity.humanoid.HumanoidGolemEntity;
import dev.xkmc.modulargolems.content.entity.humanoid.weapon.GolemWeaponRegistry;
import dev.xkmc.modulargolems.content.entity.metalgolem.MetalGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.registries.ForgeRegistries;
import src.toi_et_moi.mgdp.Mgdp;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class VoidEchoModifier extends GolemModifier {

	private static final String TAG_BURST = "mgdp_void_burst";
	private static final String TAG_TYPE = "mgdp_void_type";

	private static final Method SET_POS;

	static {
		try {
			SET_POS = findMethod(Entity.class, "setPos", "m_20343_", double.class, double.class, double.class);
		} catch (Exception e) {
			throw new RuntimeException("Failed to init VoidEchoModifier", e);
		}

	}

	private static Method findMethod(Class<?> cls, String deobf, String srg, Class<?>... params) throws Exception {
		try {
			return cls.getMethod(deobf, params);
		} catch (NoSuchMethodException e) {
			return cls.getMethod(srg, params);
		}
	}

	public VoidEchoModifier() {
		super(StatFilterType.HEALTH, 1);
	}

	@Override
	public void onHurtTarget(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		if (golem.level().isClientSide()) return;
		LivingEntity target = event.getEntity();

		int lastSlash = golem.getPersistentData().getInt("mgdp_void_slash");
		if (golem.tickCount - lastSlash >= 10) {
			golem.getPersistentData().putInt("mgdp_void_slash", golem.tickCount);
			fireVoidSlash(golem, target);
		}

		float heal = event.getAmount() * 0.25f;
		if (heal > 0) golem.heal(heal);

		var voidEffect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("goety", "void_touched"));
		if (voidEffect != null) {
			target.addEffect(new MobEffectInstance(voidEffect, 100, 1));
		}
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		LivingEntity target = golem.getTarget();
		if (target == null) return;

		CompoundTag data = golem.getPersistentData();

		// Teleport behind target every 3s when in melee range
		if (golem.tickCount % 60 == 0) {
			// Only teleport if wielding a melee weapon (not bow/crossbow)
			var weapon = golem.getMainHandItem();
			boolean isRanged = weapon.getItem() instanceof net.minecraft.world.item.BowItem
					|| weapon.getItem() instanceof net.minecraft.world.item.CrossbowItem;
			if (!isRanged) {
				int last = data.getInt("mgdp_void_tp");
				if (golem.tickCount - last >= 60) {
					data.putInt("mgdp_void_tp", golem.tickCount);
					Vec3 dir = target.getLookAngle().normalize();
					Vec3 pos = target.position().add(dir.scale(-3));
					golem.teleportTo(pos.x, pos.y, pos.z);

					if (target instanceof Mob mob) {
						mob.setTarget(null);
						mob.setLastHurtByMob(null);
						mob.setLastHurtByPlayer(null);
						try {
							for (var entry : mob.targetSelector.getAvailableGoals()) {
								if (entry.getGoal() instanceof TargetGoal tg) {
									var f = TargetGoal.class.getDeclaredField("targetMob");
									f.setAccessible(true);
									f.set(tg, null);
								}
							}
						} catch (Exception ignored) {}
					}
				}
			}
		}

		// Burst logic
		int burst = data.getInt(TAG_BURST);
		if (burst > 0) {
			if (data.getBoolean(TAG_TYPE)) {
				fireVoidShockBomb(golem, target);
			} else {
				fireVoidShock(golem, target);
			}
			data.putInt(TAG_BURST, burst - 1);
		} else if (golem.tickCount % 60 == 0) {
			boolean halfHp = golem.getHealth() <= golem.getMaxHealth() * 0.5f;
			int mult = halfHp ? 3 : 1;
			boolean isBomb = golem.getRandom().nextBoolean();
			data.putBoolean(TAG_TYPE, isBomb);
			data.putInt(TAG_BURST, (isBomb ? 1 : 6) * mult);
		}
	}



	@Override
	public void onHurt(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		if (golem.level().isClientSide()) return;
		float cap = Math.max(20, golem.getMaxHealth() * 0.2f);
		if (event.getAmount() > cap) event.setAmount(cap);
	}

	private void fireVoidSlash(AbstractGolemEntity<?, ?> golem, LivingEntity target) {
		try {
			Class<?> cls = Class.forName("com.Polarice3.Goety.common.entities.projectiles.VoidSlash");
			Constructor<?> ctor = cls.getConstructor(net.minecraft.world.level.Level.class, LivingEntity.class);
			Object slash = ctor.newInstance(golem.level(), golem);

			SET_POS.invoke(slash, golem.getX(), golem.getEyeY(), golem.getZ());
			cls.getMethod("setVoidLevel", int.class).invoke(slash, 3);
			cls.getMethod("slash", Vec3.class, double.class)
					.invoke(slash, target.position().subtract(golem.position()).normalize(), 1.0d);
			cls.getMethod("setDamage", float.class)
					.invoke(slash, (float) golem.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE));

			golem.level().addFreshEntity((net.minecraft.world.entity.Entity) slash);
		} catch (Exception e) {
			Mgdp.LOGGER.error("VoidEcho fireVoidSlash failed", e);
		}
	}

	private void fireVoidShock(AbstractGolemEntity<?, ?> golem, LivingEntity target) {
		try {
			Class<?> cls = Class.forName("com.Polarice3.Goety.common.entities.projectiles.VoidShock");
			Constructor<?> ctor = cls.getConstructor(LivingEntity.class, LivingEntity.class, net.minecraft.world.level.Level.class);
			Object shock = ctor.newInstance(golem, target, golem.level());

			SET_POS.invoke(shock, golem.getX() + (golem.getRandom().nextDouble() - 0.5) * 2,
					golem.getY() + 4.0, golem.getZ() + (golem.getRandom().nextDouble() - 0.5) * 2);
			cls.getMethod("setPower", Vec3.class, int.class).invoke(shock, Vec3.ZERO, 10);
			cls.getMethod("setBaseDamage", float.class)
					.invoke(shock, (float) golem.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE));

			golem.level().addFreshEntity((net.minecraft.world.entity.Entity) shock);
		} catch (Exception e) {
			Mgdp.LOGGER.error("VoidEcho fireVoidShock failed", e);
		}
	}

	private void fireVoidShockBomb(AbstractGolemEntity<?, ?> golem, LivingEntity target) {
		try {
			Class<?> cls = Class.forName("com.Polarice3.Goety.common.entities.projectiles.VoidShockBomb");
			Constructor<?> ctor = cls.getConstructor(LivingEntity.class, net.minecraft.world.level.Level.class);
			Object bomb = ctor.newInstance(golem, golem.level());

			SET_POS.invoke(bomb, target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ());
			cls.getMethod("setBaseDamage", float.class)
					.invoke(bomb, (float) golem.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE));
			try {
					cls.getMethod("shootFromRotation", Entity.class, float.class, float.class, float.class, float.class, float.class)
							.invoke(bomb, golem, golem.getXRot(), golem.getYRot(), 0.0f, 0.8f, 1.0f);
				} catch (NoSuchMethodException sfrEx) {
					try {
						// Might need different name in production
					} catch (Exception ignored) {}
				}

			golem.level().addFreshEntity((net.minecraft.world.entity.Entity) bomb);
		} catch (Exception e) {
			Mgdp.LOGGER.error("VoidEcho fireVoidShockBomb failed", e);
		}
	}
}
