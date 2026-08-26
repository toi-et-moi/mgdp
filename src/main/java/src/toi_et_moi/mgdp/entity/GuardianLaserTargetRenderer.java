package src.toi_et_moi.mgdp.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.entity.metalgolem.BeaconRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * 守卫者激光视觉渲染：
 * 蓄力期间在傀儡与目标之间画随音效闪烁的红瞄准线，并在目标处画随蓄力收缩的红圈；
 * PLING 后（蓄力 75 刻起）隐藏；发射阶段渲染发白光的粗直线。
 */
public class GuardianLaserTargetRenderer extends EntityRenderer<GuardianLaserTargetEntity> {

	private static final float[] RED = {1.0F, 0.55F, 0.35F};   // 闪烁：亮红
	private static final float[] RED_DIM = {0.85F, 0.22F, 0.13F}; // 平时：红
	private static final float[] WHITE = {1.0F, 1.0F, 1.0F};

	public GuardianLaserTargetRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public boolean shouldRender(GuardianLaserTargetEntity e, Frustum frustum, double x, double y, double z) {
		return true;
	}

	@Override
	public void render(GuardianLaserTargetEntity e, float yrot, float pTick, PoseStack pose, MultiBufferSource source, int light) {
		LivingEntity owner = e.getOwner();
		if (!(owner instanceof AbstractGolemEntity<?, ?> golem)) return;
		Vec3 start = golem.getEyePosition();
		Vec3 end = e.position();

		if (e.getBeam() == 1) {
			// 发射阶段：发白光的直线
			renderBeam(e, pose, source, start, end, WHITE, 0.75F);
			return;
		}

		int charge = e.getCharge();
		if (charge >= 75) return; // PLING 播放后隐藏瞄准线与圈
		boolean flash = e.getFlash() == 1;

		float[] lineColor = flash ? RED : RED_DIM;
		renderBeam(e, pose, source, start, end, lineColor, flash ? 0.25F : 0.15F);

		// 目标处红圈：随蓄力逐渐收缩
		float radius = Math.max(0.3F, 3.0F - 3.0F * charge / 75.0F);
		renderCircle(pose, source, radius, lineColor);
	}

	private static void renderBeam(GuardianLaserTargetEntity e, PoseStack pose, MultiBufferSource source, Vec3 start, Vec3 end, float[] color, float thickness) {
		Vec3 dir = end.subtract(start);
		float len = (float) dir.length();
		if (len <= 0.01F) return;
		pose.pushPose();
		pose.translate((float) (start.x - e.getX()), (float) (start.y - e.getY()), (float) (start.z - e.getZ()));
		pose.mulPose(new Quaternionf().rotationTo(new Vector3f(0, 1, 0),
				new Vector3f((float) dir.x / len, (float) dir.y / len, (float) dir.z / len)));
		BeaconRenderer.renderBeam(pose, source, 0, thickness, len, color);
		pose.popPose();
	}

	private static void renderCircle(PoseStack pose, MultiBufferSource source, float radius, float[] color) {
		VertexConsumer buffer = source.getBuffer(RenderType.lines());
		Matrix4f mat = pose.last().pose();
		int segments = 40;
		for (int i = 0; i < segments; i++) {
			double a1 = i * Math.PI * 2 / segments;
			double a2 = (i + 1) * Math.PI * 2 / segments;
			float x1 = (float) (Math.cos(a1) * radius);
			float z1 = (float) (Math.sin(a1) * radius);
			float x2 = (float) (Math.cos(a2) * radius);
			float z2 = (float) (Math.sin(a2) * radius);
			buffer.vertex(mat, x1, 0, z1).color(color[0], color[1], color[2], 1.0F)
					.normal(pose.last().normal(), 0, 1, 0).endVertex();
			buffer.vertex(mat, x2, 0, z2).color(color[0], color[1], color[2], 1.0F)
					.normal(pose.last().normal(), 0, 1, 0).endVertex();
		}
	}

	@Override
	public ResourceLocation getTextureLocation(GuardianLaserTargetEntity e) {
		return BeaconRenderer.BEAM_LOCATION;
	}
}
