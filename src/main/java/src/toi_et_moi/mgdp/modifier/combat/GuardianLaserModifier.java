package src.toi_et_moi.mgdp.modifier.combat;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import dev.xkmc.modulargolems.init.data.MGDamageTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;

public class GuardianLaserModifier extends GolemModifier {

	private static final String TAG_CHARGE = "mgdp_guardian_charge";
	private static final String TAG_CD = "mgdp_guardian_cd";
	private static final int CHARGE_TICKS = 80;
	private static final int COOLDOWN = 20;

	public GuardianLaserModifier() {
		super(StatFilterType.ATTACK, 3);
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;

		LivingEntity target = golem.getTarget();
		if (target == null || !target.isAlive()) {
			golem.getPersistentData().putInt(TAG_CHARGE, 0);
			return;
		}

		int cd = golem.getPersistentData().getInt(TAG_CD);
		if (cd > 0) {
			golem.getPersistentData().putInt(TAG_CD, cd - 1);
			return;
		}

		int charge = golem.getPersistentData().getInt(TAG_CHARGE);
		charge++;

		if (golem.level() instanceof ServerLevel sl && charge > 0) {
			if (charge % 10 == 0)
				drawLaserLine(sl, golem.getEyePosition(), target.getEyePosition(), ParticleTypes.WAX_ON, 0.5);
		}

		if (charge >= CHARGE_TICKS) {
			fire(golem, target, level);
			golem.getPersistentData().putInt(TAG_CHARGE, 0);
			golem.getPersistentData().putInt(TAG_CD, COOLDOWN);
		} else {
			golem.getPersistentData().putInt(TAG_CHARGE, charge);
		}
	}

	private void fire(AbstractGolemEntity<?, ?> golem, LivingEntity target, int level) {
		ServerLevel sl = (ServerLevel) golem.level();
		Vec3 start = golem.getEyePosition();
		Vec3 end = target.getEyePosition();
		Vec3 dir = end.subtract(start).normalize();

		drawLaserLine(sl, start, end, ParticleTypes.GLOW, 0.2);

		float atk = (float) golem.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
		float baseDamage = (float) (10 + level * 10 + atk * 0.5);
		HashSet<LivingEntity> hit = new HashSet<>();

		double range = start.distanceTo(end);
		Vec3 step = dir.scale(0.5);
		Vec3 pos = start;
		for (int i = 0; i < range * 2; i++) {
			pos = pos.add(step);
			double r = 2.0;
			for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class,
					new AABB(pos.x - r, pos.y - r, pos.z - r, pos.x + r, pos.y + r, pos.z + r),
					e -> e.isAlive() && !golem.isAlliedTo(e) && e != golem)) {
				if (hit.add(e)) {
					e.hurt(e.damageSources().explosion(golem, golem), baseDamage);
					e.hurt(echoDamage(golem), 7);
					e.setRemainingFireTicks(60);
				}
			}
		}

		double impactR = 4.0 * level;
		for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class,
				new AABB(end.x - impactR, end.y - impactR, end.z - impactR,
						end.x + impactR, end.y + impactR, end.z + impactR),
				e -> e.isAlive() && !golem.isAlliedTo(e) && e != golem)) {
			if (hit.add(e)) {
				e.hurt(e.damageSources().explosion(golem, golem), baseDamage * 0.5f);
				e.hurt(echoDamage(golem), 7);
				e.setRemainingFireTicks(60);
			}
		}
		sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, end.x, end.y, end.z, 1, 0, 0, 0, 0);
		sl.sendParticles(ParticleTypes.FLASH, end.x, end.y, end.z, 1, 0, 0, 0, 0);
		sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, end.x, end.y, end.z, 20, 0.5, 0.5, 0.5, 0.05);
	}

	private static DamageSource echoDamage(LivingEntity attacker) {
		return new DamageSource(attacker.level().registryAccess()
				.registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(MGDamageTypes.ECHO), attacker, attacker);
	}

	private static void drawLaserLine(ServerLevel level, Vec3 start, Vec3 end, net.minecraft.core.particles.ParticleOptions particle, double spacing) {
		Vec3 dir = end.subtract(start);
		double dist = dir.length();
		dir = dir.normalize();
		for (double d = 0; d < dist; d += spacing) {
			Vec3 p = start.add(dir.scale(d));
			level.sendParticles(particle, p.x, p.y, p.z, 1, 0, 0, 0, 0);
		}
	}
}
