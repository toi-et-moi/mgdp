package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.item.ranged.FlameThrowerItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

@Mixin(FlameThrowerItem.class)
public class FlameThrowerMixin {

    @Redirect(method = "onTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private void mgdp$redirectShrink(ItemStack stack, int amount, AbstractGolemEntity<?, ?> e, ItemStack weapon, int slot) {
        if (!e.getModifiers().containsKey(MGDPModifiers.INFINITE_AMMO.get())) {
            stack.shrink(amount);
        }
    }
}
