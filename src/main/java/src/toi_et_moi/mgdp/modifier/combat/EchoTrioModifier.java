package src.toi_et_moi.mgdp.modifier.combat;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.entity.targeting.TargetManager;
import dev.xkmc.modulargolems.content.item.ranged.SonicCannonItem;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import dev.xkmc.modulargolems.init.data.MGDamageTypes;
import dev.xkmc.modulargolems.init.registrate.GolemTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

import java.util.List;
import src.toi_et_moi.mgdp.Mgdp;

@Mod.EventBusSubscriber(modid = Mgdp.MODID)
public class EchoTrioModifier extends GolemModifier {

	public EchoTrioModifier() {
		super(StatFilterType.ATTACK, 1);
	}

	private static int synergyCount(AbstractGolemEntity<?, ?> golem) {
		int n = 1;
		if (golem.getModifiers().getOrDefault(MGDPModifiers.SONIC_BOOM.get(), 0) > 0) n++;
		if (golem.getMainHandItem().getItem() instanceof SonicCannonItem
				|| golem.getOffhandItem().getItem() instanceof SonicCannonItem) n++;
		return n;
	}

	private static float synergyMult(int count) {
		return count == 3 ? 24.0F : count == 2 ? 4.0F : 1.0F;
	}

	private static DamageSource echoDamage(LivingEntity attacker) {
		return new DamageSource(
				attacker.level().registryAccess()
						.lookupOrThrow(Registries.DAMAGE_TYPE)
						.getOrThrow(MGDamageTypes.ECHO),
				attacker);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onLivingHurt(LivingHurtEvent event) {
		if (!(event.getSource().getEntity() instanceof AbstractGolemEntity<?, ?> golem)) return;
		if (golem.level().isClientSide()) return;
		if (!golem.getModifiers().containsKey(MGDPModifiers.ECHO_TRIO.get())) return;
		if (golem.getPersistentData().getBoolean("mgdp_echo_self")) return;

		int n = synergyCount(golem);
		float mult = synergyMult(n);
		if (mult <= 1) return;

		if (event.getSource().is(DamageTypes.SONIC_BOOM)) {
			event.setCanceled(true);
			golem.getPersistentData().putBoolean("mgdp_echo_convert", true);
			event.getEntity().hurt(echoDamage(golem), event.getAmount() * mult);
			golem.getPersistentData().putBoolean("mgdp_echo_convert", false);
		} else if (event.getSource().is(MGDamageTypes.ECHO) && !golem.getPersistentData().getBoolean("mgdp_echo_convert")) {
			event.setAmount(event.getAmount() * mult);
		}
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		if (golem.tickCount % 20 != 0) return;

		int n = synergyCount(golem);
		float mult = synergyMult(n);
		boolean boosted = n >= 2;

		if (n >= 3 && golem.tickCount % 40 == 0) {
			golem.level().playSound(null, golem.blockPosition(), SoundEvents.WARDEN_HEARTBEAT,
					SoundSource.NEUTRAL, 5.0F, 1.0F);
		}

		AABB area = golem.getBoundingBox().inflate(35);
		List<LivingEntity> targets = golem.level().getEntitiesOfClass(LivingEntity.class, area,
				e -> e.isAlive() && e != golem && golem.getSensing().hasLineOfSight(e)
						&& (e == golem.getTarget() || TargetManager.wantsToAttack(golem, e)));

		if (targets.isEmpty()) return;

		float atk = (float) golem.getAttributeValue(Attributes.ATTACK_DAMAGE);
		Vec3 origin = golem.position().add(0, 1.6, 0);

		DamageSource dmgSrc = boosted ? echoDamage(golem) : golem.damageSources().sonicBoom(golem);
		float dmg = atk * mult;
		var sweepAttr = golem.getAttribute(GolemTypes.GOLEM_SWEEP.get());
		double sweep = sweepAttr != null ? sweepAttr.getValue() : 0;

		golem.getPersistentData().putBoolean("mgdp_echo_self", true);
		golem.level().playSound(null, golem.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM,
				SoundSource.NEUTRAL, 0.5F, 1.0F);

		int count = 0;
		for (LivingEntity target : targets) {
			if (count >= 3) break;

			Vec3 targetPos = target.getEyePosition();
			Vec3 dir = targetPos.subtract(origin).normalize();

			if (golem.level() instanceof ServerLevel sl) {
				for (int i = 1; i <= 17; i++) {
					Vec3 p = origin.add(dir.scale(i));
					sl.sendParticles(ParticleTypes.SONIC_BOOM, p.x, p.y, p.z, 1, 0, 0, 0, 0);
				}
			}

			if (sweep > 0) {
				double length = sweep * 2;
				Vec3 end = origin.add(dir.scale(length));
				AABB beamAABB = new AABB(origin, end);
				boolean hitPrimary = false;
				for (LivingEntity nearby : golem.level().getEntitiesOfClass(LivingEntity.class, beamAABB,
						e -> e != golem && e.isAlive()
								&& (e == golem.getTarget() || TargetManager.wantsToAttack(golem, e)))) {
					nearby.hurt(dmgSrc, dmg);
					double kbResist = 1 - nearby.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
					nearby.push(dir.x * 2.5 * kbResist, dir.y * 0.5 * kbResist, dir.z * 2.5 * kbResist);
					if (nearby == target) hitPrimary = true;
				}
				if (!hitPrimary) {
					target.hurt(dmgSrc, dmg);
					double kbResist = 1 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
					target.push(dir.x * 2.5 * kbResist, dir.y * 0.5 * kbResist, dir.z * 2.5 * kbResist);
				}
			} else {
				target.hurt(dmgSrc, dmg);
				double kbResist = 1 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
				target.push(dir.x * 2.5 * kbResist, dir.y * 0.5 * kbResist, dir.z * 2.5 * kbResist);
			}

			count++;
		}

		golem.getPersistentData().putBoolean("mgdp_echo_self", false);
	}

	@Override
	public void onHurt(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		if (event.getSource().getEntity() instanceof LivingEntity attacker) {
			float atk = (float) golem.getAttributeValue(Attributes.ATTACK_DAMAGE);
			int n = synergyCount(golem);
			if (n >= 2) {
				attacker.hurt(echoDamage(golem), atk * synergyMult(n));
			} else {
				attacker.hurt(golem.damageSources().sonicBoom(golem), atk);
			}
		}
	}

	@Override
	public List<MutableComponent> getDetail(int v) {
		return List.of(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GREEN));
	}
}
