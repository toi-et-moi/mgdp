package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin {

	@Inject(method = "tryLoadProjectiles", at = @At("HEAD"), cancellable = true)
	private static void mgdp$infiniteCrossbowLoad(LivingEntity entity, ItemStack crossbow,
												   CallbackInfoReturnable<Boolean> cir) {
		if (!(entity instanceof AbstractGolemEntity<?, ?> golem)) return;
		if (!golem.getModifiers().containsKey(MGDPModifiers.INFINITE_AMMO.get())) return;
		if (CrossbowItem.isCharged(crossbow)) {
			cir.setReturnValue(true);
			return;
		}
		// Find the actual arrow type in the golem's inventory (no consumption)
		ItemStack arrowToLoad = findArrow(golem, crossbow);
		// Put it as the charged projectile
		var tag = crossbow.getOrCreateTag();
		var list = new ListTag();
		list.add(arrowToLoad.save(new CompoundTag()));
		tag.put("ChargedProjectiles", list);
		tag.putBoolean("Charged", true);
		cir.setReturnValue(true);
	}

	private static ItemStack findArrow(AbstractGolemEntity<?, ?> golem, ItemStack crossbow) {
		// Check hands
		for (var hand : net.minecraft.world.InteractionHand.values()) {
			ItemStack inHand = golem.getItemInHand(hand);
			if (!inHand.isEmpty() && inHand.getItem() instanceof net.minecraft.world.item.ArrowItem) {
				return inHand.copyWithCount(1);
			}
		}
		// Check arrow slot (HumanoidGolemEntity only)
		if (golem instanceof dev.xkmc.modulargolems.content.entity.humanoid.HumanoidGolemEntity humanoid) {
			ItemStack slot = humanoid.getArrowSlot().getItem();
			if (!slot.isEmpty() && slot.getItem() instanceof net.minecraft.world.item.ArrowItem) {
				return slot.copyWithCount(1);
			}
		}
		// Check full inventory
		var opt = golem.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
		if (opt.isPresent()) {
			IItemHandler inv = opt.get();
			for (int i = 0; i < inv.getSlots(); i++) {
				ItemStack stack = inv.getStackInSlot(i);
				if (!stack.isEmpty() && stack.getItem() instanceof net.minecraft.world.item.ArrowItem) {
					return stack.copyWithCount(1);
				}
			}
		}
		// Default to vanilla arrow
		return new ItemStack(Items.ARROW);
	}
}
