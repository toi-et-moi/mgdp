package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.content.entity.humanoid.HumanoidGolemEntity;
import dev.xkmc.modulargolems.content.entity.humanoid.HumanoidGolemModel;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 主人潜行时人形傀儡跟随蹲伏（客户端渲染）。
 * 在 HumanoidGolemModel.setupAnim 末尾应用蹲伏姿势——setupAnim 在每帧渲染时最先执行，
 * 主模型（皮肤）与所有 layer（护甲/手持物品）都使用 setupAnim 后的姿势，
 * 从而同时覆盖普通渲染、玩家皮肤与怪物皮肤。绕开 MobRenderer 的 model.crouching 字段
 * （golem 渲染路径上不一定设置）。服务端通过 setPose(CROUCHING) 同步 DATA_POSE。
 */
@OnlyIn(Dist.CLIENT)
@Mixin(HumanoidGolemModel.class)
public class GolemSneakRendererMixin {

	@Inject(method = "setupAnim(Ldev/xkmc/modulargolems/content/entity/humanoid/HumanoidGolemEntity;FFFFF)V",
			at = @At("TAIL"), remap = false)
	private void mgdp$sneak(HumanoidGolemEntity entity, float f1, float f2, float f3, float f4, float f5, CallbackInfo ci) {
		if (entity.getPose() != Pose.CROUCHING) return;
		mgdp$applyHumanoidSneak((HumanoidGolemModel) (Object) this);
	}

	/** 人形傀儡蹲伏：躯干/头下沉弯腰，手臂随躯干前倾，腿微沉 2px + 后移不倾斜 */
	@Unique
    private static void mgdp$applyHumanoidSneak(HumanoidGolemModel m) {
		m.body.xRot = 0.4F;        // 身体前倾弯腰
		m.body.y += 5.0F;          // 躯干下沉
		m.head.y += 7.0F;          // 头跟随并再低一点（不分离）
		m.hat.y += 7.0F;
		m.leftArm.xRot += 0.4F;    // 手臂随躯干前倾
		m.rightArm.xRot += 0.4F;
		m.leftArm.y += 6.0F;       // 手臂跟随躯干下沉
		m.rightArm.y += 6.0F;
		m.leftLeg.y += 2.0F;       // 腿微沉 2px
		m.rightLeg.y += 2.0F;
		m.leftLeg.z += 3.0F;       // 腿向后移（z 正方向=后），不倾斜
		m.rightLeg.z += 3.0F;
	}
}
