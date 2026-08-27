package src.toi_et_moi.mgdp.modifier.combat;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.init.data.MGDamageTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import src.toi_et_moi.mgdp.Mgdp;

/**
 * 锐不可当箭矢命中处理：取消原版箭伤害，改用 ECHO 伤害类型（无视护甲）。
 * 箭矢自导追踪在 mixin/IndomitableArrowMixin 中实现（Forge 1.20.1 无实体 tick 事件）。
 */
@Mod.EventBusSubscriber(modid = Mgdp.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class IndomitableArrowHitHandler {

	@SubscribeEvent
	public static void onArrowHit(ProjectileImpactEvent event) {
		if (!(event.getProjectile() instanceof AbstractArrow arrow)) return;
		if (!arrow.getPersistentData().getBoolean(IndomitableModifier.TAG_ARROW)) return;
		if (!(event.getRayTraceResult() instanceof EntityHitResult hit)) return;
		if (!(hit.getEntity() instanceof LivingEntity living)) return;
		if (!(arrow.getOwner() instanceof AbstractGolemEntity<?, ?> golem)) return;
		if (living == golem) return;
		// 取消原版箭伤害，改用 ECHO 伤害类型（无视护甲/魔法，无敌帧用 invulnerableTime=0 兜底）
		event.setCanceled(true);
		DamageSource source = new DamageSource(living.level().registryAccess()
				.registryOrThrow(Registries.DAMAGE_TYPE)
				.getHolderOrThrow(MGDamageTypes.ECHO), golem, golem);
		living.hurt(source, (float) arrow.getBaseDamage());
		living.invulnerableTime = 0;
	}
}
