package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.l2archery.content.item.GenericBowItem;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

/**
 * Intercepts the user.getProjectile(bow) call inside GenericBowItem.releaseUsingAndShootArrow.
 * This prevents the L2BowBehavior.shootArrow from consuming arrows a second time.
 */
@Mixin(GenericBowItem.class)
public abstract class GenericBowItemMixin {

	@Redirect(
			method = "releaseUsingAndShootArrow(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)Ljava/util/Optional;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getProjectile(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;",
					remap = true),
			remap = false
	)
	private ItemStack mgdp$redirectGetProjectile(LivingEntity user, ItemStack bow) {
		if (user instanceof AbstractGolemEntity<?, ?> golem
				&& golem.getModifiers().containsKey(MGDPModifiers.INFINITE_AMMO.get())) {
			return ItemStack.EMPTY;
		}
		return user.getProjectile(bow);
	}
}
