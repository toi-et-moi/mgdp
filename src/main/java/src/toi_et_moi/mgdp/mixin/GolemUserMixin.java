package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.entity.humanoid.weapon.GolemUser;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

import java.util.function.Predicate;

@Mixin(GolemUser.class)
public abstract class GolemUserMixin {

	@Inject(method = "bypassAllConsumption", at = @At("RETURN"), cancellable = true, remap = false)
	private void mgdp$bypassConsumption(CallbackInfoReturnable<Boolean> cir) {
		if (((GolemUser)(Object)this).user().getModifiers().containsKey(MGDPModifiers.INFINITE_AMMO.get())) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "getPreferredProjectile", at = @At("HEAD"), cancellable = true, remap = false)
	private void mgdp$infiniteArrow(ItemStack weapon, Predicate<ItemStack> special, Predicate<ItemStack> general,
									CallbackInfoReturnable<ItemStack> cir) {
		if (!((GolemUser)(Object)this).user().getModifiers().containsKey(MGDPModifiers.INFINITE_AMMO.get())) return;
		ItemStack ans = ((GolemUser)(Object)this).user().getProjectile(weapon);
		if (!ans.isEmpty()) return; // has arrows, use normal path
		// No arrows in inventory — provide a default arrow
		ItemStack defaultArrow = new ItemStack(Items.ARROW);
		if (special.test(defaultArrow)) {
			cir.setReturnValue(defaultArrow);
		}
	}
}
