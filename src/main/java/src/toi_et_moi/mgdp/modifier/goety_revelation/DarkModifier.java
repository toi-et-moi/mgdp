package src.toi_et_moi.mgdp.modifier.goety_revelation;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.entity.targeting.TargetManager;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class DarkModifier extends GolemModifier {

	private static final int RANGE = 35;
	private static final int DURATION = 200;
	private static final int AMP = 4;
	private static final int DARK_LIGHT = 7;

	public DarkModifier() {
		super(StatFilterType.ATTACK, 1);
	}

	private static boolean isDark(Level level, BlockPos pos) {
		return level.getMaxLocalRawBrightness(pos) < DARK_LIGHT;
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		if (!net.minecraftforge.fml.ModList.get().isLoaded("goety")) return;
		if (golem.tickCount % 20 != 0) return;

		// Wane V aura on enemies
		var wane = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("goety", "wane"));
		var box = golem.getBoundingBox().inflate(RANGE);
		for (var entity : golem.level().getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive())) {
			if (entity.distanceToSqr(golem) > RANGE * RANGE) continue;
			if (entity == golem || entity.isAlliedTo(golem) || entity == golem.getOwner()) continue;
			if (golem.canAttack(entity) && TargetManager.wantsToAttack(golem, entity)) {
				entity.addEffect(new MobEffectInstance(wane, DURATION, AMP));
			}
		}

		// Attacks only while a target is locked
		LivingEntity target = golem.getTarget();
		if (target == null || !target.isAlive()) return;
		float atk = (float) golem.getAttributeValue(Attributes.ATTACK_DAMAGE);

		// Giant scythe blade barrage every second: 3 parallel slashes, 1 block apart, 3x attack damage each
		try {
			var ctor = Class.forName("com.Polarice3.Goety.common.entities.projectiles.ScytheSlash")
					.getConstructor(Level.class, double.class, double.class, double.class,
							double.class, double.class, double.class);
			double dx = target.getX() - golem.getX();
			double dy = target.getEyeY() - golem.getEyeY();
			double dz = target.getZ() - golem.getZ();
			double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
			if (d > 0.01) {
				double nx = dx / d, ny = dy / d, nz = dz / d;
				double px = -nz, pz = nx;
				for (int i = -1; i <= 1; i++) {
					Entity slash = (Entity) ctor.newInstance(golem.level(),
							golem.getX() + px * i * 1.5, golem.getEyeY() - 0.3, golem.getZ() + pz * i * 1.5,
							nx * 0.35, ny * 0.35, nz * 0.35);
					// The 7-arg constructor leaves totalLife at 0 (discards on first tick) and no owner
					slash.getClass().getMethod("setTotalLife", int.class).invoke(slash, 60);
					((AbstractHurtingProjectile) slash).setOwner(golem);
					slash.getClass().getMethod("setDamage", float.class).invoke(slash, atk * 3.0F);
					slash.setDeltaMovement(nx * 2.0, ny * 2.0, nz * 2.0);
					slash.hasImpulse = true;
					golem.level().addFreshEntity(slash);
				}
			}
		} catch (Exception e) {
			src.toi_et_moi.mgdp.Mgdp.LOGGER.warn("TheDark: scythe failed", e);
		}

		// Shadow walk V for 3s every 5s while the golem itself stands in darkness
		if (isDark(golem.level(), golem.blockPosition()) && golem.tickCount % 100 == 0) {
			var shadowWalk = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("goety", "shadow_walk"));
			if (shadowWalk != null) {
				golem.addEffect(new MobEffectInstance(shadowWalk, 60, AMP));
			}
		}
	}

	@Override
	public void onHurtTarget(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		if (golem.level().isClientSide()) return;
		var target = event.getEntity();
		if (!golem.canAttack(target)) return;
		if (target == golem || target == golem.getOwner()) return;

		// +300% damage vs wane debuffed targets or targets in a dark environment
		var wane = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("goety", "wane"));
		boolean waned = wane != null && target.hasEffect(wane);
		boolean inDark = isDark(target.level(), target.blockPosition());
		if (waned || inDark) {
			event.setAmount(event.getAmount() * 4.0F);
		}
	}

	@Override
	public void onAttacked(AbstractGolemEntity<?, ?> golem, LivingAttackEvent event, int level) {
		// 50% chance to ignore incoming damage while standing in darkness
		if (golem.level().isClientSide()) return;
		if (!isDark(golem.level(), golem.blockPosition())) return;
		if (golem.getRandom().nextFloat() < 0.5F) {
			event.setCanceled(true);
		}
	}
}
