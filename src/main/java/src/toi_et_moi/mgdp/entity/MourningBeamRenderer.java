package src.toi_et_moi.mgdp.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.xkmc.modulargolems.content.entity.metalgolem.BeaconRenderer;
import dev.xkmc.modulargolems.content.entity.metalgolem.MetalGolemEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

public class MourningBeamRenderer extends EntityRenderer<MourningBeamEntity> {

	// Magenta color from DyeColor
	private static final float[] MAGENTA = DyeColor.MAGENTA.getTextureDiffuseColors();

	public MourningBeamRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public boolean shouldRender(MourningBeamEntity e, Frustum frustum, double x, double y, double z) {
		return true;
	}

	@Override
	public void render(MourningBeamEntity e, float yrot, float pTick, PoseStack pose, MultiBufferSource source, int light) {
		if (!(e.getOwner() instanceof MetalGolemEntity golem)) return;
		pose.pushPose();
		pose.mulPose(Axis.YP.rotationDegrees(e.getYRot()));
		pose.mulPose(Axis.XP.rotationDegrees(e.getXRot() + 90));
		var perc = Math.max(0, 1 - 1f * e.tickCount / e.life);
		var r = golem.getScale() * 0.5f * perc * perc;
		pose.scale(r, 1, r);
		BeaconRenderer.renderBeam(pose, source, 0, 1, e.len, MAGENTA);
		pose.popPose();
	}

	@Override
	public ResourceLocation getTextureLocation(MourningBeamEntity e) {
		return BeaconRenderer.BEAM_LOCATION;
	}
}
