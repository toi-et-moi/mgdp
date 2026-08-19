package src.toi_et_moi.mgdp.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

/**
 * 拾荒箱彩蛋：叼烟哥没有影子——影子被吓跑了。
 * 阴影是 EntityRenderDispatcher#render 里 renderer.render() 之后，
 * 由私有静态方法 EntityRenderDispatcher#renderShadow(...) 画的。
 * 在这里包住它：带拾荒箱升级的傀儡跳过画影子，其它实体照常画。
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRendererShadowMixin {

    @WrapOperation(method = "render",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;renderShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/Entity;FFLnet/minecraft/world/level/LevelReader;F)V"))
    private void mgdp$skipScavShadow(PoseStack poseStack, MultiBufferSource buffer, Entity entity,
                                     float size, float partialTick, LevelReader level, float shadowRadius,
                                     Operation<Void> original) {
        if (entity instanceof AbstractGolemEntity<?, ?> golem
                && golem.getModifiers().containsKey(MGDPModifiers.SCAV_BOX.get())) {
            return;
        }
        original.call(poseStack, buffer, entity, size, partialTick, level, shadowRadius);
    }
}
