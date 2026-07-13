package src.toi_et_moi.mgdp.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

    @Inject(method = "isEntityUpsideDown", at = @At("HEAD"), cancellable = true)
    private static void mgdp$upsideDown(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof AbstractGolemEntity<?, ?> golem
                && golem.getModifiers().containsKey(MGDPModifiers.UPSIDE_DOWN.get())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "setupRotations", at = @At("TAIL"))
    private void mgdp$reverseFrontBack(LivingEntity entity, PoseStack poseStack,
                                       float bob, float yRot, float partialTick,
                                       CallbackInfo ci) {
        if (entity instanceof AbstractGolemEntity<?, ?> golem
                && golem.getModifiers().containsKey(MGDPModifiers.REVERSE.get())) {
            poseStack.scale(1.0F, 1.0F, -1.0F);
        }
    }
}
