package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.mob_weapon_api.api.projectile.BowUseContext;
import dev.xkmc.mob_weapon_api.integration.l2archery.L2BowBehavior;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

/**
 * L2BowBehavior override 了 getStandardPullTime（返回弓的 config.pull_time），
 * 导致 QuickStrike 对莱特兰弓艺的弓不生效。L2BowBehavior 已在 BOW 注册表先注册（抢不到），
 * 因此用这个最小 mixin 让莱特兰弓也支持 QuickStrike 秒拉弓；其余逻辑（增伤/二段蓄力等）完全不动。
 */
@Mixin(L2BowBehavior.class)
public abstract class L2BowQuickStrikeMixin {

	@Inject(method = "getStandardPullTime", at = @At("HEAD"), cancellable = true, remap = false)
	private void mgdp$fastL2Pull(BowUseContext user, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
		if (user.user() instanceof AbstractGolemEntity<?, ?> golem
				&& golem.getModifiers().containsKey(MGDPModifiers.QUICK_STRIKE.get())) {
			cir.setReturnValue(1);
		}
	}
}
