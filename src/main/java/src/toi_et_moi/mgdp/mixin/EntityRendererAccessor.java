package src.toi_et_moi.mgdp.mixin;

import net.minecraft.client.renderer.entity.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityRenderer.class)
public interface EntityRendererAccessor {

    @Accessor("shadowRadius")
    void setShadowRadius(float radius);

    @Accessor("shadowRadius")
    float getShadowRadius();
}
