package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderDragon.class)
public abstract class EnderDragonMixin {

    @Invoker("reallyHurt")
    abstract boolean invokeReallyHurt(DamageSource source, float amount);

    @Inject(method = "hurt(Lnet/minecraft/world/entity/boss/EnderDragonPart;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At("HEAD"), cancellable = true)
    private void mgdp$allowGolemAttack(EnderDragonPart part, DamageSource source, float amount,
                                        CallbackInfoReturnable<Boolean> cir) {
        if (source.getEntity() instanceof AbstractGolemEntity) {
            cir.setReturnValue(invokeReallyHurt(source, amount));
        }
    }
}
