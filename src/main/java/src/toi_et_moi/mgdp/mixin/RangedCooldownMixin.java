package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.mob_weapon_api.example.goal.SmartBowAttackGoal;
import dev.xkmc.mob_weapon_api.example.goal.SmartInstantRangedAttackGoal;
import dev.xkmc.mob_weapon_api.example.goal.SmartHoldRangedAttackGoal;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

@Mixin({SmartBowAttackGoal.class, SmartInstantRangedAttackGoal.class, SmartHoldRangedAttackGoal.class})
public abstract class RangedCooldownMixin {

	@Shadow(remap = false)
	private int attackTime;

	@Inject(method = "tick", at = @At("HEAD"))
	private void mgdp$clearCooldown(CallbackInfo ci) {
		if (attackTime > 100) return;
		Object self = this;
		Mob mob = ((SmartRangedAccessor)self).getMob();
		if (!(mob instanceof AbstractGolemEntity<?, ?> golem)) return;
		if (!golem.getModifiers().containsKey(MGDPModifiers.QUICK_STRIKE.get())) return;
		attackTime = 0;
	}
}
