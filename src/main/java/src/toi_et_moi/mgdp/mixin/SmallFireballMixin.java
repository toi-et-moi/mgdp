package src.toi_et_moi.mgdp.mixin;

import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.SmallFireball;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractHurtingProjectile.class)
public abstract class SmallFireballMixin {

	@Unique
	private static final int MGDP_MAX_LIFETIME = 100;

	@Inject(method = "tick", at = @At("HEAD"))
	private void mgdp$despawnAfterTime(CallbackInfo ci) {
		AbstractHurtingProjectile self = (AbstractHurtingProjectile) (Object) this;
		if (!(self instanceof SmallFireball)) return;
		if (self.level().isClientSide()) return;
		if (!self.getPersistentData().getBoolean("mgdp_fireball")) return;
		if (self.tickCount > MGDP_MAX_LIFETIME) {
			self.discard();
		}
	}
}
