package src.toi_et_moi.mgdp.mixin;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import src.toi_et_moi.mgdp.modifier.combat.IndomitableModifier;

/**
 * 锐不可当箭矢自导追踪：每 tick 平滑转向发射时的目标，逐渐收拢成一串追向目标的箭。
 * 用 mixin 注入 AbstractArrow.tick（Forge 1.20.1 没有针对实体的 EntityTickEvent）。
 * 只对带锐不可当标记的箭生效，开销极小。
 */
@Mixin(AbstractArrow.class)
public class IndomitableArrowMixin {

	@Inject(method = "tick", at = @At("HEAD"))
	private void mgdp$trackTarget(CallbackInfo ci) {
		AbstractArrow arrow = (AbstractArrow) (Object) this;
		if (arrow.level().isClientSide()) return;
		var data = arrow.getPersistentData();
		if (!data.getBoolean(IndomitableModifier.TAG_ARROW)) return;
		// 已落地/静止则不追踪
		if (arrow.getDeltaMovement().lengthSqr() < 0.0001) return;
		int id = data.getInt(IndomitableModifier.TAG_TARGET);
		if (id == -1) return;
		Entity ent = arrow.level().getEntity(id);
		if (!(ent instanceof LivingEntity living) || !living.isAlive()) return;
		Vec3 cur = arrow.getDeltaMovement();
		Vec3 toTarget = living.getEyePosition().subtract(arrow.position()).normalize();
		Vec3 dir = cur.normalize().scale(1.0F - IndomitableModifier.TRACK_RATE)
				.add(toTarget.scale(IndomitableModifier.TRACK_RATE)).normalize();
		Vec3 motion = dir.scale(cur.length());
		arrow.setDeltaMovement(motion);
		arrow.hasImpulse = true;
		// 让箭的朝向跟随新速度方向
		arrow.setYRot((float) (Mth.atan2(motion.z, motion.x) * (180.0 / Math.PI)) - 90.0F);
		arrow.setXRot((float) (Mth.atan2(motion.y, Math.sqrt(motion.x * motion.x + motion.z * motion.z))
				* (180.0 / Math.PI)));
	}
}
