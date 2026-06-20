package src.toi_et_moi.mgdp.modifier.special;

import dev.xkmc.l2damagetracker.contents.attack.CreateSourceEvent;
import dev.xkmc.l2damagetracker.contents.damage.DefaultDamageState;
import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class EndOfBeginningModifier extends GolemModifier {

	public EndOfBeginningModifier() {
		super(StatFilterType.HEALTH, 1);
	}

	// === 始源大地: 治疗翻倍 ===
	@Override
	public double onHealTick(double heal, AbstractGolemEntity<?, ?> golem, int level) {
		return heal * 2.0;
	}

	// === 始源大地: 免疫击退 ===
	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;

		// Knockback immunity
		var attr = golem.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
		if (attr != null && attr.getBaseValue() < 1.0) {
			golem.getPersistentData().putDouble("mgdp_eob_kb", attr.getBaseValue());
			attr.setBaseValue(1.0);
		}

		// === 炽热炼狱: 着火/熔岩/Y≥128 每秒修复 ===
		if (golem.tickCount % 20 == 0 &&
			(golem.isOnFire() || golem.isInLava() || golem.getY() >= 128) &&
			(golem.getHealth() < golem.getMaxHealth() || golem.getReforgeCount() > 0)) {
			golem.repairWithItem();
		}

		// === 终末虚空: 末影水晶附近每秒修复 ===
		if (golem.tickCount % 20 == 0) {
			if (golem.getHealth() >= golem.getMaxHealth() && golem.getReforgeCount() == 0) return;
			AABB area = golem.getBoundingBox().inflate(32);
			for (EndCrystal crystal : golem.level().getEntitiesOfClass(EndCrystal.class, area, EndCrystal::isAlive)) {
				if (golem.distanceToSqr(crystal) < 32 * 32) {
					golem.repairWithItem();
					break;
				}
			}
		}
	}

	// === 始狱终: 末影水晶附近无敌 ===
	@Override
	public void onAttacked(AbstractGolemEntity<?, ?> golem, LivingAttackEvent event, int level) {
		if (golem.level().isClientSide()) return;

		AABB area = golem.getBoundingBox().inflate(16);
		if (!golem.level().getEntitiesOfClass(EndCrystal.class, area, EndCrystal::isAlive).isEmpty()) {
			event.setCanceled(true);
		}
	}

	// === 始源大地: 减伤30% ===
	@Override
	public void onHurt(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		if (golem.level().isClientSide()) return;
		event.setAmount(event.getAmount() * 0.7f);
	}

	// === 始源大地: 伤害×2 ===
	@Override
	public void onHurtTarget(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		if (golem.level().isClientSide()) return;
		event.setAmount(event.getAmount() * 2.0f);
	}

	// === 终末虚空: 虚空伤害（穿甲穿魔） ===
	@Override
	public void modifySource(AbstractGolemEntity<?, ?> golem, CreateSourceEvent event, int level) {
		if (golem.level().isClientSide()) return;

		var result = event.getResult();
		if (result == null) return;
		if (result.validState(DefaultDamageState.BYPASS_ARMOR))
			event.enable(DefaultDamageState.BYPASS_ARMOR);
		if (result.validState(DefaultDamageState.BYPASS_MAGIC))
			event.enable(DefaultDamageState.BYPASS_MAGIC);
	}
}
