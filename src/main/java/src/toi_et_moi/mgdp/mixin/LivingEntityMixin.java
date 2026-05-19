package src.toi_et_moi.mgdp.mixin;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import src.toi_et_moi.mgdp.item.IronCurtainItem;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void mgdp$ironCurtainVisual(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        boolean prot = IronCurtainItem.isProtected(self);
        boolean wasGlow = self.getPersistentData().getBoolean("mgdp_iron_glow");
        if (prot && !wasGlow) {
            self.setGlowingTag(true);
            self.getPersistentData().putBoolean("mgdp_iron_glow", true);
        } else if (!prot && wasGlow) {
            self.setGlowingTag(false);
            self.getPersistentData().remove("mgdp_iron_glow");
        }
    }

    @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"), cancellable = true)
    private void mgdp$ironCurtainNoEffect(MobEffectInstance effect, Entity source, CallbackInfoReturnable<Boolean> cir) {
        if (IronCurtainItem.isProtected((LivingEntity) (Object) this)) {
            cir.setReturnValue(false);
        }
    }
}
