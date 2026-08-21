package src.toi_et_moi.mgdp.handler;

import dev.xkmc.l2damagetracker.init.L2DamageTracker;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import src.toi_et_moi.mgdp.Mgdp;

/**
 * 弓强度（l2damagetracker:bow_strength，Projectile Strength）原公式在 L2Weaponry 里
 * 只对玩家生效，其它实体（包括模块化傀儡）即使注册了该属性也不参与乘算。
 * 这里按原公式重写一份，仅对模块化傀儡的弹射物伤害生效：
 *   hurt 阶段伤害 = 基础弹射伤害 × 傀儡弓强度属性值（默认 1 = 无加成，护甲减免前乘，与原公式一致）。
 * 使用 Forge 事件实现，不用 mixin。
 */
@Mod.EventBusSubscriber(modid = Mgdp.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GolemBowStrengthHandler {

	@SubscribeEvent
	public static void onGolemProjectileHurt(LivingHurtEvent event) {
		DamageSource source = event.getSource();
		if (!source.is(DamageTypeTags.IS_PROJECTILE)) return;

		AbstractGolemEntity<?, ?> golem = getShootingGolem(source);
		if (golem == null) return;

		double strength = L2DamageTracker.BOW_STRENGTH.get().getWrappedValue(golem);
		if (Math.abs(strength - 1.0) < 1e-6) return;
		event.setAmount(event.getAmount() * (float) strength);
	}

	private static AbstractGolemEntity<?, ?> getShootingGolem(DamageSource source) {
		Entity direct = source.getDirectEntity();
		if (direct instanceof AbstractGolemEntity<?, ?> g) return g;
		if (direct instanceof Projectile p && p.getOwner() instanceof AbstractGolemEntity<?, ?> g) return g;
		return null;
	}
}
