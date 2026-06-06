package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.item.ranged.GolemBowBehavior;
import dev.xkmc.mob_weapon_api.api.projectile.BowUseContext;
import dev.xkmc.mob_weapon_api.api.projectile.ProjectileWeaponUser;
import dev.xkmc.mob_weapon_api.example.behavior.SimpleBowBehavior;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

@Mixin({GolemBowBehavior.class, SimpleBowBehavior.class})
public abstract class GolemBowMixin {

	@Inject(method = "hasProjectile", at = @At("HEAD"), cancellable = true, remap = false)
	private void mgdp$infiniteProjectile(ProjectileWeaponUser user, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (user.user() instanceof AbstractGolemEntity<?, ?> golem) {
			if (golem.getModifiers().containsKey(MGDPModifiers.INFINITE_AMMO.get())) {
				cir.setReturnValue(true);
			}
		}
	}

	@Inject(method = "getStandardPullTime", at = @At("HEAD"), cancellable = true, remap = false)
	private void mgdp$noDrawTime(BowUseContext ctx, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
		if (ctx.user() instanceof AbstractGolemEntity<?, ?> golem) {
			if (golem.getModifiers().containsKey(MGDPModifiers.QUICK_STRIKE.get())) {
				cir.setReturnValue(1);
			}
		}
	}

	@Inject(method = "getPowerForTime", at = @At("HEAD"), cancellable = true, remap = false)
	private void mgdp$maxPower(BowUseContext ctx, ItemStack stack, int time, CallbackInfoReturnable<Float> cir) {
		if (ctx.user() instanceof AbstractGolemEntity<?, ?> golem) {
			if (golem.getModifiers().containsKey(MGDPModifiers.QUICK_STRIKE.get())) {
				cir.setReturnValue(1.0F);
			}
		}
	}
}
