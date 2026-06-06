package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.entity.dog.DogGolemEntity;
import dev.xkmc.modulargolems.content.item.wand.RiderWandItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RiderWandItem.class)
public abstract class RiderWandItemMixin {

	@Inject(method = "interactLivingEntity", at = @At("TAIL"))
	private void mgdp$rideAnyGolem(ItemStack stack, Player user, LivingEntity target, InteractionHand hand,
								   CallbackInfoReturnable<InteractionResult> cir) {
		if (cir.getReturnValue() != InteractionResult.SUCCESS) return;
		if (!(target instanceof AbstractGolemEntity<?, ?> golem)) return;
		if (golem instanceof DogGolemEntity) return;
		if (target.level().isClientSide()) return;
		user.startRiding(golem, false);
	}
}
