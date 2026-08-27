package src.toi_et_moi.mgdp.compat;

import dev.xkmc.mob_weapon_api.api.projectile.CrossbowUseContext;
import dev.xkmc.mob_weapon_api.api.projectile.ProjectileWeaponUser;
import dev.xkmc.mob_weapon_api.api.projectile.ProjectileWeaponUseContext;
import dev.xkmc.mob_weapon_api.example.behavior.GeneralCrossbowBehavior;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

import java.util.List;

/**
 * 通用弩行为（替换 GeneralCrossbowBehavior fallback）。
 * 原版弩 AI 有硬编码 10 刻发射延迟 + 单发流程，无法像弓一样高速连发，
 * 因此 QuickStrike 时重写 performRangedAttack：一次发射一批箭形成连发弹幕。
 */
public class QuickStrikeCrossbowBehavior extends GeneralCrossbowBehavior {

	/** 高速连发时一次发射的箭数 */
	public static final int ARROWS_PER_VOLLEY = 6;

	private static boolean hasQuickStrike(LivingEntity user) {
		return user instanceof AbstractGolemEntity<?, ?> golem
				&& golem.getModifiers().containsKey(MGDPModifiers.QUICK_STRIKE.get());
	}

	private static boolean hasInfiniteAmmo(LivingEntity user) {
		return user instanceof AbstractGolemEntity<?, ?> golem
				&& golem.getModifiers().containsKey(MGDPModifiers.INFINITE_AMMO.get());
	}

	@Override
	public int chargeTime(LivingEntity user, ItemStack stack) {
		return hasQuickStrike(user) ? 0 : super.chargeTime(user, stack);
	}

	@Override
	public boolean hasProjectile(ProjectileWeaponUser user, ItemStack stack) {
		return hasInfiniteAmmo(user.user()) || super.hasProjectile(user, stack);
	}

	@Override
	public int performRangedAttack(CrossbowUseContext user, ItemStack stack, InteractionHand hand) {
		if (!hasQuickStrike(user.user()))
			return super.performRangedAttack(user, stack, hand);
		// 高速连发：不装载单发弹药，直接用当前弹药一次发射一批箭
		ItemStack ammo = user.getPreferredProjectile(stack);
		if (ammo.isEmpty()) return 0;
		float velocity = user.getCrossbowVelocity(List.of(ammo));
		ProjectileWeaponUseContext.AimResult aim = null;
		for (int i = 0; i < ARROWS_PER_VOLLEY; i++) {
			AbstractArrow arrow = user.createArrow(ammo.copy(), 1);
			arrow.setCritArrow(true);
			arrow.setShotFromCrossbow(true);
			arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
			// 微小递增散布，形成一束密集连发（不重叠成一点）
			float angle = (i - (ARROWS_PER_VOLLEY - 1) / 2F) * 0.6F;
			if (aim == null)
				aim = user.aim(arrow.position(), velocity, 0.05F, user.getInitialInaccuracy());
			aim.shoot(arrow, angle);
			user.user().level().addFreshEntity(arrow);
		}
		user.user().level().playSound(null, user.user().getX(), user.user().getY(), user.user().getZ(),
				SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
		if (!user.bypassAllConsumption())
			stack.hurtAndBreak(1, user.user(), e -> e.broadcastBreakEvent(hand));
		return 0;
	}
}
