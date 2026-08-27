package src.toi_et_moi.mgdp.compat;

import dev.xkmc.mob_weapon_api.api.projectile.BowUseContext;
import dev.xkmc.mob_weapon_api.api.projectile.ProjectileWeaponUser;
import dev.xkmc.mob_weapon_api.example.behavior.SimpleBowBehavior;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

/**
 * 通用弓行为（替换 SimpleBowBehavior fallback）：带 QuickStrike（秒拉弓+满力）与无限弹药支持。
 * 仅作用于普通 BowItem（MetalGolemBowItem、L2Archery 弓、SMC/Revelation 专属弓由各自行为处理）。
 */
public class QuickStrikeBowBehavior extends SimpleBowBehavior {

	private static boolean hasQuickStrike(LivingEntity user) {
		return user instanceof AbstractGolemEntity<?, ?> golem
				&& golem.getModifiers().containsKey(MGDPModifiers.QUICK_STRIKE.get());
	}

	private static boolean hasInfiniteAmmo(LivingEntity user) {
		return user instanceof AbstractGolemEntity<?, ?> golem
				&& golem.getModifiers().containsKey(MGDPModifiers.INFINITE_AMMO.get());
	}

	@Override
	public int getStandardPullTime(BowUseContext ctx, ItemStack stack) {
		return hasQuickStrike(ctx.user()) ? 1 : super.getStandardPullTime(ctx, stack);
	}

	@Override
	public float getPowerForTime(BowUseContext ctx, ItemStack stack, int time) {
		return hasQuickStrike(ctx.user()) ? 1.0F : super.getPowerForTime(ctx, stack, time);
	}

	@Override
	public boolean hasProjectile(ProjectileWeaponUser user, ItemStack stack) {
		return hasInfiniteAmmo(user.user()) || super.hasProjectile(user, stack);
	}
}
