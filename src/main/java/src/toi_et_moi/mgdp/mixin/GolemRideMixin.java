package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.entity.humanoid.HumanoidGolemEntity;
import dev.xkmc.modulargolems.content.entity.metalgolem.MetalGolemEntity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({MetalGolemEntity.class, HumanoidGolemEntity.class})
public abstract class GolemRideMixin {

	@Inject(method = "checkRide", at = @At("HEAD"), cancellable = true, remap = false)
	private void mgdp$rideAnyTarget(LivingEntity target, CallbackInfo ci) {
		if (target != null) {
			AbstractGolemEntity<?, ?> self = (AbstractGolemEntity<?, ?>) (Object) this;
			self.startRiding(target);
		}
		ci.cancel();
	}
}
