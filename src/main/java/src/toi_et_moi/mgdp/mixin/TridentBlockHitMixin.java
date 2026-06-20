package src.toi_et_moi.mgdp.mixin;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Projectile.class)
public class TridentBlockHitMixin {

	@Inject(method = "onHitBlock", at = @At("HEAD"))
	private void mgdp$onHitBlock(BlockHitResult result, CallbackInfo ci) {
		if (!(((Object) this) instanceof ThrownTrident trident)) return;
		if (trident.getTags().contains("mgdp_trident_festival")) {
			src.toi_et_moi.mgdp.modifier.combat.TridentFestivalModifier.onTridentHit(
					trident, trident.level(), result.getBlockPos());
		}
	}
}
