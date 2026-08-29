package src.toi_et_moi.mgdp.modifier.combat;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import dev.xkmc.modulargolems.init.data.MGDamageTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import src.toi_et_moi.mgdp.Mgdp;
import src.toi_et_moi.mgdp.entity.GuardianLaserTargetEntity;

import java.util.HashSet;

public class GuardianLaserModifier extends GolemModifier {

	private static final String TAG_CHARGE = "mgdp_guardian_charge";
	private static final String TAG_CD = "mgdp_guardian_cd";
	private static final String TAG_BEAT = "mgdp_guardian_beat";
	private static final String TAG_SHOTS = "mgdp_guardian_shots";
	private static final int CHARGE_TICKS = 80;
	private static final int COOLDOWN = 20;
	private static final int VOLLEY_INTERVAL = 20; // 连发间隔：1 秒

	public GuardianLaserModifier() {
		super(StatFilterType.ATTACK, 4);
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;

		var data = golem.getPersistentData();
		LivingEntity target = golem.getTarget();
		if (target == null || !target.isAlive()) {
			// 无目标：蓄力/连发重置，但冷却照常递减（对齐原模组 specialAttackCoolDown 无条件递减）
			int curCd = data.getInt(TAG_CD);
			if (curCd > 0) data.putInt(TAG_CD, curCd - 1);
			data.putInt(TAG_CHARGE, 0);
			data.putInt(TAG_SHOTS, 0);
			return;
		}

		int cd = data.getInt(TAG_CD);
		if (cd > 0) {
			cd--;
			data.putInt(TAG_CD, cd);
			// 连发恢复：隔一秒后回到攻击前5刻，停止 bit，播放一次 pling，再攻击
			if (cd == 0 && data.getInt(TAG_CHARGE) == CHARGE_TICKS - 5) {
				if (golem.level() instanceof ServerLevel sl) {
					sl.playSound(null, golem.getX(), golem.getEyeY(), golem.getZ(),
							SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.NEUTRAL, 2.0F, 1.0F);
				}
				// 重新生成瞄准实体（与本体同步走最后5刻，白光与激光同时出现）
				var laser = new GuardianLaserTargetEntity(Mgdp.GUARDIAN_LASER_TARGET.get(), golem.level(), golem);
				laser.setCharge(CHARGE_TICKS - 5);
				golem.level().addFreshEntity(laser);
			}
			return;
		}

		int charge = data.getInt(TAG_CHARGE);
		charge++;

		// 蓄力开始：生成瞄准实体（渲染红线/红圈，发射阶段白光），重置音效节拍
		if (charge == 1) {
			var laser = new GuardianLaserTargetEntity(Mgdp.GUARDIAN_LASER_TARGET.get(), golem.level(), golem);
			golem.level().addFreshEntity(laser);
			data.putInt(TAG_BEAT, 1);
		}

		if (golem.level() instanceof ServerLevel sl && charge > 0) {
			// 蓄力音效：节拍器式（nextBeat 精确控制间隔），随蓄力程度缩短（BotW 守护者准备音效）
			int remaining = CHARGE_TICKS - charge;
			if (remaining > 5) {
				int interval = Math.max(1, 12 - charge * 11 / CHARGE_TICKS);
				int beat = data.getInt(TAG_BEAT);
				if (charge >= beat) {
					sl.playSound(null, golem.getX(), golem.getEyeY(), golem.getZ(),
							SoundEvents.NOTE_BLOCK_BIT.value(), SoundSource.NEUTRAL, 1.0F, 1.0F);
					data.putInt(TAG_BEAT, charge + interval);
				}
			} else if (remaining == 5) {
				// 攻击前5刻：停止 bit，播放一次 pling
				sl.playSound(null, golem.getX(), golem.getEyeY(), golem.getZ(),
						SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.NEUTRAL, 2.0F, 1.0F);
			}
		}

		if (charge >= CHARGE_TICKS) {
			fire(golem, target, level);
			int shots = data.getInt(TAG_SHOTS) + 1;
			if (level > 1 && shots < level) {
				// 二级起连发：回到攻击前5刻，隔一秒（期间停止 bit）再走 pling→攻击；
				// 瞄准实体在冷却结束（cd==0）时重新生成，与本体同步出白光
				data.putInt(TAG_CHARGE, CHARGE_TICKS - 5);
				data.putInt(TAG_CD, VOLLEY_INTERVAL);
				data.putInt(TAG_SHOTS, shots);
			} else {
				// 打满次数（或一级）：重置蓄力，重新蓄能
				data.putInt(TAG_CHARGE, 0);
				data.putInt(TAG_SHOTS, 0);
				data.putInt(TAG_CD, COOLDOWN);
			}
		} else {
			data.putInt(TAG_CHARGE, charge);
		}
	}

	private void fire(AbstractGolemEntity<?, ?> golem, LivingEntity target, int level) {
		ServerLevel sl = (ServerLevel) golem.level();
		Vec3 start = golem.getEyePosition();
		Vec3 end = target.getEyePosition();
		Vec3 dir = end.subtract(start).normalize();

		// 发射音效：音调高的信标失效
		sl.playSound(null, start.x, start.y, start.z, SoundEvents.BEACON_DEACTIVATE, SoundSource.NEUTRAL, 2.0F, 1.5F);

		float atk = (float) golem.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
		// 等级加成 = 傀儡攻击力 25%/级，每级保底 10 点；基础 10 点不变
		float baseDamage = 10.0F + Math.max(10.0F, atk * 0.25F) * level + atk * 0.5F;
		// 额外回声伤害：3 级及以上 7 → 10.5
		float extra = level >= 3 ? 10.5F : 7.0F;
		HashSet<LivingEntity> hit = new HashSet<>();

		double range = start.distanceTo(end);
		Vec3 step = dir.scale(0.5);
		Vec3 pos = start;
		for (int i = 0; i < range * 2; i++) {
			pos = pos.add(step);
			double r = 2.0;
			for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class,
					new AABB(pos.x - r, pos.y - r, pos.z - r, pos.x + r, pos.y + r, pos.z + r),
					e -> e.isAlive() && golem.canAttack(e) && !golem.isAlliedTo(e) && e != golem)) {
				if (hit.add(e)) {
					e.hurt(e.damageSources().explosion(golem, golem), baseDamage);
					e.hurt(echoDamage(golem), extra);
					e.setRemainingFireTicks(60);
				}
			}
		}

		double impactR = 4.0 * level;
		for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class,
				new AABB(end.x - impactR, end.y - impactR, end.z - impactR,
						end.x + impactR, end.y + impactR, end.z + impactR),
				e -> e.isAlive() && golem.canAttack(e) && !golem.isAlliedTo(e) && e != golem)) {
			if (hit.add(e)) {
				e.hurt(e.damageSources().explosion(golem, golem), baseDamage * 0.5f);
				e.hurt(echoDamage(golem), extra);
				e.setRemainingFireTicks(60);
			}
		}
		sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, end.x, end.y, end.z, 1, 0, 0, 0, 0);
		sl.sendParticles(ParticleTypes.FLASH, end.x, end.y, end.z, 1, 0, 0, 0, 0);
		sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, end.x, end.y, end.z, 20, 0.5, 0.5, 0.5, 0.05);
		// 命中音效：爆炸
		sl.playSound(null, end.x, end.y, end.z, SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 2.0F, 1.0F);
	}

	private static DamageSource echoDamage(LivingEntity attacker) {
		return new DamageSource(attacker.level().registryAccess()
				.registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(MGDamageTypes.ECHO), attacker, attacker);
	}
}
