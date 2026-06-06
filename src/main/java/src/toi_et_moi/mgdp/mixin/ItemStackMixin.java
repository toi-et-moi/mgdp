package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

	@Inject(method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
			at = @At("HEAD"), cancellable = true)
	private void mgdp$unbreakable(int amount, LivingEntity entity, Consumer<LivingEntity> onBreak, CallbackInfo ci) {
		if (entity instanceof AbstractGolemEntity<?, ?> golem) {
			if (golem.getModifiers().containsKey(MGDPModifiers.UNBREAKABLE.get())) {
				ci.cancel();
			}
		}
	}
}
