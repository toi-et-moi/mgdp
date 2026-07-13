package src.toi_et_moi.mgdp.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.entity.dog.DogGolemRenderer;
import dev.xkmc.modulargolems.content.entity.humanoid.HumanoidGolemRenderer;
import dev.xkmc.modulargolems.content.entity.metalgolem.MetalGolemRenderer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

@Mixin({DogGolemRenderer.class, HumanoidGolemRenderer.class, MetalGolemRenderer.class})
public abstract class AbstractGolemRendererMixin<T extends AbstractGolemEntity<?, ?>, M extends EntityModel<T>>
        extends LivingEntityRenderer<T, M> {

    protected AbstractGolemRendererMixin() { super(null, null, 0); }

    private static final ResourceLocation ENERGY_SWIRL = new ResourceLocation("textures/entity/creeper/creeper_armor.png");


    @Inject(method = "<init>", at = @At("RETURN"))
    private void mgdp$addEnergyLayer(CallbackInfo ci) {
        this.addLayer(new RenderLayer<T, M>(this) {
            @Override
            public void render(PoseStack pose, MultiBufferSource buf, int light,
                               T entity, float limbSwing, float limbSwingAmount,
                               float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
                if (!entity.getModifiers().containsKey(MGDPModifiers.CHARGED_SHIELD.get())) return;
                float u = ((float) entity.tickCount + partialTick) * 0.03F;
                float v = u * 0.5F;
                var vc = buf.getBuffer(RenderType.energySwirl(ENERGY_SWIRL, u, v));
               this.getParentModel().renderToBuffer(pose, vc, light, 0, 1.0F, 1.0F, 1.0F, 1.0F);
            }
        });
        this.addLayer(new RenderLayer<T, M>(this) {
            @Override
            public void render(PoseStack pose, MultiBufferSource buf, int light,
                               T entity, float limbSwing, float limbSwingAmount,
                               float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
                var flags = ((EntityAccessor) entity).getDataSharedFlagsId();
                if ((entity.getEntityData().get(flags) & 64) == 0) return;
                float u = ((float) entity.tickCount + partialTick) * 0.03F;
                float v = u * 0.5F;
                var vc = buf.getBuffer(RenderType.energySwirl(ENERGY_SWIRL, u, v));
                this.getParentModel().renderToBuffer(pose, vc, light, 0, 1.0F, 0.0F, 0.0F, 1.0F);
            }
        });
    }
}
