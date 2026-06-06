package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.content.entity.goals.GolemMeleeGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

@Mixin(GolemMeleeGoal.class)
public abstract class MeleeIntervalMixin {

	@Shadow(remap = false)
	protected dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity<?, ?> golem;

	@Inject(method = "getMeleeInterval", at = @At("RETURN"), cancellable = true, remap = false)
	private void mgdp$fastMelee(CallbackInfoReturnable<Integer> cir) {
		if (!golem.getModifiers().containsKey(MGDPModifiers.QUICK_STRIKE.get())) return;
		cir.setReturnValue(Math.max(1, (int) Math.ceil(20 / golem.getAttributeValue(
				net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED))));
	}
}
