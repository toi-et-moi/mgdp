package src.toi_et_moi.mgdp.modifier.hostility;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.entity.targeting.TargetManager;
import dev.xkmc.modulargolems.content.modifier.base.AttributeGolemModifier;
import dev.xkmc.modulargolems.init.registrate.GolemTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;

/**
 * 恶意升级：杀戮光环
 * <p>
 * 最高三级。每级为傀儡 +3 范围攻击属性（实体触及距离）与 +3 横扫属性
 * （golem_sweep）；远程攻击命中时，对命中目标周围 6/9/12 格（按等级）
 * 内的其他敌对目标同时造成与主目标相同的伤害。
 * <p>
 * 挂点说明：modulargolems 的 ModifierEventListeners.onHurtPre 会对所有
 * 间接攻击者为傀儡的伤害事件调用 onHurtTarget（远程弹射物同样适用，
 * 因为 DamageSource.getEntity() 返回的是弹射物持有者）。这里再以
 * getDirectEntity() instanceof Projectile 过滤，确保只有弹射物命中才触发；
 * 溅射伤害的 direct 实体是傀儡自身，天然不会递归。
 */
public class KillerAuraModifier extends AttributeGolemModifier {

	public KillerAuraModifier() {
		super(3,
				new AttrEntry(GolemTypes.STAT_RANGE, () -> 3.0),
				new AttrEntry(GolemTypes.STAT_SWEEP, () -> 3.0));
	}

	@Override
	public void onHurtTarget(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		if (level <= 0) return;
		// 仅远程（弹射物）命中触发
		if (!(event.getSource().getDirectEntity() instanceof Projectile)) return;
		LivingEntity target = event.getEntity();
		if (target == null || target.level().isClientSide()) return;

		// 等级 1/2/3 -> 半径 6/9/12 格
		double radius = 3.0 + 3.0 * level;
		AABB area = target.getBoundingBox().inflate(radius);
		// 目标筛选与 LightningStormModifier.shouldStrike 一致：
		// canAttack 排除主人/领袖/同主人宠物与傀儡/盟友，wantsToAttack 按
		// 默认敌对规则或玩家筛选卡决定是否攻击（默认只打怪物与敌对傀儡）
		List<LivingEntity> others = target.level().getEntitiesOfClass(LivingEntity.class, area,
				e -> e.isAlive()
						&& e != target
						&& e != golem
						&& e != golem.getOwner()
						&& (e == golem.getTarget() || (golem.canAttack(e) && TargetManager.wantsToAttack(golem, e))));

		float splash = event.getAmount();
		for (LivingEntity e : others) {
			e.invulnerableTime = 0;
			e.hurt(golem.damageSources().mobAttack(golem), splash);
		}
	}

	@Override
	public List<MutableComponent> getDetail(int v) {
		List<MutableComponent> ans = super.getDetail(v);
		ans.add(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GREEN));
		return ans;
	}
}
