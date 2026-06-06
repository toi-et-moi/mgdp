package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.mob_weapon_api.api.projectile.ProjectileWeaponUser;
import dev.xkmc.mob_weapon_api.example.behavior.SimpleCrossbowBehavior;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

@Mixin(SimpleCrossbowBehavior.class)
public abstract class CrossbowAmmoMixin {

	@Inject(method = "hasProjectile", at = @At("HEAD"), cancellable = true, remap = false)
	private void mgdp$crossbowAmmo(ProjectileWeaponUser user, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (user.user() instanceof AbstractGolemEntity<?, ?> golem) {
			if (golem.getModifiers().containsKey(MGDPModifiers.INFINITE_AMMO.get())) {
				cir.setReturnValue(true);
			}
		}
	}

	@Inject(method = "chargeTime", at = @At("HEAD"), cancellable = true, remap = false)
	private void mgdp$instantCharge(LivingEntity user, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
		if (user instanceof AbstractGolemEntity<?, ?> golem) {
			if (golem.getModifiers().containsKey(MGDPModifiers.QUICK_STRIKE.get())) {
				cir.setReturnValue(0);
			}
		}
	}
}
