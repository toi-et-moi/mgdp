package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.special.BaseRangedAttackGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

@Mixin(BaseRangedAttackGoal.class)
public abstract class QuickStrikeMixin {

	@Shadow(remap = false)
	protected AbstractGolemEntity<?, ?> golem;

	@Shadow(remap = false)
	public long attackTime;

	@Inject(method = "tick", at = @At("HEAD"))
	private void mgdp$fastRanged(CallbackInfo ci) {
		if (!golem.getModifiers().containsKey(MGDPModifiers.QUICK_STRIKE.get())) return;
		golem.specialAttackCoolDown = 0;
		attackTime = 0;
	}
}
