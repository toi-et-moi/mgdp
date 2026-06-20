package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

@Mixin(ThrownTrident.class)
public class ThrownTridentTagMixin {

	@Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V", at = @At("TAIL"))
	private void mgdp$tagTrident(Level level, LivingEntity shooter, ItemStack stack, CallbackInfo ci) {
		if (shooter instanceof AbstractGolemEntity golem
				&& golem.getModifiers().containsKey(MGDPModifiers.TRIDENT_FESTIVAL.get())) {
			((ThrownTrident) (Object) this).addTag("mgdp_trident_festival");
		}
	}

	@Inject(method = "onHitEntity", at = @At("HEAD"))
	private void mgdp$onHitEntity(EntityHitResult result, CallbackInfo ci) {
		ThrownTrident self = (ThrownTrident) (Object) this;
		if (self.getTags().contains("mgdp_trident_festival")) {
			src.toi_et_moi.mgdp.modifier.combat.TridentFestivalModifier.onTridentHit(
					self, self.level(), self.blockPosition());
		}
	}
}
