package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.mob_weapon_api.util.ShootUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ShootUtils.class)
public class ShootUtilsMixin {

	private static final double PROJECTILE_SPEED = 2.5;

	@ModifyVariable(method = "getShootVector", at = @At("HEAD"), remap = false, argsOnly = true)
	private static Vec3 mgdp$predictAim(Vec3 targetPos, LivingEntity shooter) {
		if (!(shooter instanceof AbstractGolemEntity<?, ?> golem)) return targetPos;
		LivingEntity target = golem.getTarget();
		if (target == null) return targetPos;

		Vec3 targetVel = target.getDeltaMovement();
		if (targetVel.lengthSqr() < 0.01) return targetPos;

		double dist = shooter.distanceTo(target);
		if (dist < 0.1) return targetPos;
		double flightTime = dist / PROJECTILE_SPEED;

		return target.position()
				.add(targetVel.scale(flightTime))
				.add(0, 0.025 * flightTime * flightTime, 0);
	}
}
