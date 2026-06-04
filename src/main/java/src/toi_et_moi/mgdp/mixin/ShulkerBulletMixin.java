package src.toi_et_moi.mgdp.mixin;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShulkerBullet.class)
public abstract class ShulkerBulletMixin {

	@Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
	private void mgdp$explosionProtection(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		ShulkerBullet self = (ShulkerBullet) (Object) this;
		if (!self.getPersistentData().getBoolean("mgdp_grenade")) return;
		if (source.is(DamageTypeTags.IS_EXPLOSION)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "onHitEntity", at = @At("TAIL"))
	private void mgdp$removeLevitation(EntityHitResult result, CallbackInfo ci) {
		ShulkerBullet self = (ShulkerBullet) (Object) this;
		if (!self.getPersistentData().getBoolean("mgdp_grenade")) return;
		if (result.getEntity() instanceof LivingEntity living) {
			living.removeEffect(MobEffects.LEVITATION);
		}
	}
}
