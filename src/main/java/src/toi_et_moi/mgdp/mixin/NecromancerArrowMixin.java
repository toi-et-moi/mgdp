package src.toi_et_moi.mgdp.mixin;

import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public class NecromancerArrowMixin {

	@Shadow
    protected int inGroundTime;

	@Inject(method = "tick", at = @At("HEAD"))
	private void mgdp$fastDespawn(CallbackInfo ci) {
		if (inGroundTime > 20) {
			AbstractArrow arrow = (AbstractArrow) (Object) this;
			if (arrow.getOwner() != null && arrow.getOwner().getPersistentData().getBoolean("mgdp_necromancer_minion")) {
				arrow.discard();
			}
		}
	}
}
