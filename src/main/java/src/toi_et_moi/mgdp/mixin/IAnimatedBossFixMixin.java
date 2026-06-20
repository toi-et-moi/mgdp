package src.toi_et_moi.mgdp.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fix entities that call getMaxHealth() during construction (before attributes are initialized).
 * <p>
 * Some mods override isAlive() to call getHealth() -> getMaxHealth() -> getAttribute(MAX_HEALTH).
 * During Entity.<init> -> gatherCapabilities(), the LivingEntity attribute map hasn't been
 * initialized yet, so getAttribute returns null -> NPE.
 * <p>
 * This mixin adds a universal null guard at the source: getMaxHealth().
 */
@Mixin(LivingEntity.class)
public class IAnimatedBossFixMixin {

    @Inject(method = "getMaxHealth", at = @At("HEAD"), cancellable = true)
    private void mgdp$fixGetMaxHealth(CallbackInfoReturnable<Float> cir) {
        if (((LivingEntity) (Object) this).getAttributes() == null) {
            cir.setReturnValue(1.0F);
        }
    }
}
