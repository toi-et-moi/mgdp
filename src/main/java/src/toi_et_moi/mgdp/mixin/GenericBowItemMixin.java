package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.l2archery.content.item.GenericBowItem;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.entity.common.SweepGolemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

/**
 * 修复莱特兰弓艺（L2Archery）的箭矢增伤：
 * 1. QuickStrike 秒拉弓（1 tick）会让 releaseUsingAndShootArrow 内部 getPowerForTime(1)
 *    算出 power≈0.03 < 0.1 直接取消返回空 → L2BowBehavior 退回普通箭头吃不到 config.damage() 增伤。
 *    → 注入 getPowerForTime：QuickStrike 时强制返回满力 1。
 * 2. 无限弹药时 getProjectile 被重定向成 EMPTY 同样会提前返回空。
 *    → 改为返回真实箭（copy），并拦截 arrow.shrink(1) 使无限弹药时不消耗。
 * 这样增伤流程完整走通，且不消耗弹药。
 */
@Mixin(GenericBowItem.class)
public abstract class GenericBowItemMixin {

	@Inject(method = "getPowerForTime(Lnet/minecraft/world/entity/LivingEntity;F)F",
			at = @At("HEAD"), cancellable = true, remap = false)
	private void mgdp$fullPower(LivingEntity entity, float time, CallbackInfoReturnable<Float> cir) {
		if (hasQuickStrike(entity)) {
			cir.setReturnValue(1.0F);
		}
	}

	@Redirect(
			method = "releaseUsingAndShootArrow(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)Ljava/util/Optional;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getProjectile(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;",
					remap = true),
			remap = false
	)
	private ItemStack mgdp$redirectGetProjectile(LivingEntity user, ItemStack bow) {
		if (hasInfiniteAmmo(user)) {
			return findRealArrow(user, bow);
		}
		return user.getProjectile(bow);
	}

	@Redirect(
			method = "releaseUsingAndShootArrow(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)Ljava/util/Optional;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V",
					remap = true),
			remap = false
	)
	private void mgdp$noConsumeArrow(ItemStack stack, int count, ItemStack bow, Level level, LivingEntity user, int remaining) {
		if (!hasInfiniteAmmo(user)) {
			stack.shrink(count);
		}
	}

	private static boolean hasInfiniteAmmo(LivingEntity user) {
		return user instanceof AbstractGolemEntity<?, ?> golem
				&& golem.getModifiers().containsKey(MGDPModifiers.INFINITE_AMMO.get());
	}

	private static boolean hasQuickStrike(LivingEntity user) {
		return user instanceof AbstractGolemEntity<?, ?> golem
				&& golem.getModifiers().containsKey(MGDPModifiers.QUICK_STRIKE.get());
	}

	/** 无限弹药时找一个真实的当前弹药（copy），让莱特兰增伤流程走通；不调用 getProjectile（可能消耗） */
	private static ItemStack findRealArrow(LivingEntity user, ItemStack bow) {
		if (user instanceof AbstractGolemEntity<?, ?> golem) {
			// 1. 手持/副手箭
			if (bow.getItem() instanceof ProjectileWeaponItem pwi) {
				ItemStack held = ProjectileWeaponItem.getHeldProjectile(golem, pwi.getSupportedHeldProjectiles());
				if (!held.isEmpty()) return held.copyWithCount(1);
			}
			// 2. 专属箭槽（SweepGolemEntity）
			if (golem instanceof SweepGolemEntity<?, ?> sweep) {
				ItemStack slot = sweep.getArrowSlot().getItem();
				if (!slot.isEmpty()) return slot.copyWithCount(1);
			}
			// 3. 背包搜索（只读，不消耗）
			var opt = golem.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
			if (opt.isPresent()) {
				IItemHandler inv = opt.get();
				for (int i = 0; i < inv.getSlots(); i++) {
					ItemStack s = inv.getStackInSlot(i);
					if (!s.isEmpty() && s.getItem() instanceof ArrowItem) {
						return s.copyWithCount(1);
					}
				}
			}
			// 4. 兜底原版箭
			return new ItemStack(Items.ARROW);
		}
		ItemStack real = user.getProjectile(bow);
		return real.isEmpty() ? new ItemStack(Items.ARROW) : real.copyWithCount(1);
	}
}
