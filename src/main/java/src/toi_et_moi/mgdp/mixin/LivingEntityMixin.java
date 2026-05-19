package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import src.toi_et_moi.mgdp.init.MGDPModifiers;
import src.toi_et_moi.mgdp.item.IronCurtainItem;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @ModifyVariable(method = "heal", at = @At("HEAD"), argsOnly = true)
    private float mgdp$healingBoost(float amount) {
        if (!((Object) this instanceof AbstractGolemEntity<?, ?> golem)) return amount;
        if (golem.getModifiers().containsKey(MGDPModifiers.ENCHANTED_NETHERITE_GOLD.get())) {
            return amount * 1.1F;
        }
        return amount;
    }

    @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"), cancellable = true)
    private void mgdp$ironCurtainNoEffect(MobEffectInstance effect, Entity source, CallbackInfoReturnable<Boolean> cir) {
        if (IronCurtainItem.isProtected((LivingEntity) (Object) this)) {
            cir.setReturnValue(false);
        }
    }
}
