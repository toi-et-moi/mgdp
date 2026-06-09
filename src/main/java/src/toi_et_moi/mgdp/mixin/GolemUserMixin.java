package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.entity.humanoid.weapon.GolemUser;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Predicate;

@Mixin(GolemUser.class)
public abstract class GolemUserMixin {

	@Inject(method = "bypassAllConsumption", at = @At("RETURN"), cancellable = true, remap = false)
	private void mgdp$bypassConsumption(CallbackInfoReturnable<Boolean> cir) {
		if (((GolemUser)(Object)this).user().getModifiers().containsKey(MGDPModifiers.INFINITE_AMMO.get())) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "getPreferredProjectile", at = @At("HEAD"), cancellable = true, remap = false)
	private void mgdp$infiniteArrow(ItemStack weapon, Predicate<ItemStack> special, Predicate<ItemStack> general,
									CallbackInfoReturnable<ItemStack> cir) {
		if (!((GolemUser)(Object)this).user().getModifiers().containsKey(MGDPModifiers.INFINITE_AMMO.get())) return;
		var golem = ((GolemUser)(Object)this).user();
		// DO NOT call getProjectile — it may consume in newer modulargolems versions.
		// Check hands directly (no consumption).
		for (var hand : net.minecraft.world.InteractionHand.values()) {
			ItemStack inHand = golem.getItemInHand(hand);
			if (!inHand.isEmpty() && general.test(inHand)) {
				cir.setReturnValue(inHand.copyWithCount(1));
				return;
			}
		}
		// Check dedicated arrow slot on HumanoidGolemEntity (no consumption)
		if (golem instanceof dev.xkmc.modulargolems.content.entity.humanoid.HumanoidGolemEntity humanoid) {
			ItemStack arrowSlot = humanoid.getArrowSlot().getItem();
			if (!arrowSlot.isEmpty() && general.test(arrowSlot)) {
				cir.setReturnValue(arrowSlot.copyWithCount(1));
				return;
			}
		}
		// Check full inventory via capability (no consumption — getStackInSlot is read-only)
		var opt = golem.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
		if (opt.isPresent()) {
			IItemHandler inv = opt.get();
			for (int i = 0; i < inv.getSlots(); i++) {
				ItemStack stack = inv.getStackInSlot(i);
				if (!stack.isEmpty() && general.test(stack)) {
					cir.setReturnValue(stack.copyWithCount(1));
					return;
				}
			}
		}
		// Check L2Backpack spatial backpack for arrows
		ItemStack fromBackpack = findArrowInBackpack(golem, general);
		if (!fromBackpack.isEmpty()) {
			cir.setReturnValue(fromBackpack);
			return;
		}
		// No arrow found anywhere — provide a default one
		cir.setReturnValue(new ItemStack(Items.ARROW));
	}

	@SuppressWarnings("unchecked")
	private static ItemStack findArrowInBackpack(AbstractGolemEntity<?, ?> golem, Predicate<ItemStack> predicate) {
		if (!ModList.get().isLoaded("l2backpack")) return ItemStack.EMPTY;
		try {
			Class<?> wcClass = Class.forName("dev.xkmc.l2backpack.content.remote.worldchest.WorldChestItem");
			Method getContainer = wcClass.getMethod("getContainer", ItemStack.class, net.minecraft.server.level.ServerLevel.class);

			// Check equipment slots for WorldChestItem
			if (!(golem.level() instanceof net.minecraft.server.level.ServerLevel sl)) return ItemStack.EMPTY;

			for (EquipmentSlot slot : List.of(EquipmentSlot.CHEST, EquipmentSlot.OFFHAND)) {
				ItemStack stack = golem.getItemBySlot(slot);
				if (stack.isEmpty() || !wcClass.isInstance(stack.getItem())) continue;

				Object opt = getContainer.invoke(stack.getItem(), stack, sl);
				Class<?> optClass = opt.getClass();
				if ((boolean) optClass.getMethod("isEmpty").invoke(opt)) continue;

				Object storage = optClass.getMethod("get").invoke(opt);
				Field f = storage.getClass().getField("container");
				var inv = (net.minecraft.world.SimpleContainer) f.get(storage);

				for (int i = 0; i < inv.getContainerSize(); i++) {
					ItemStack invStack = inv.getItem(i);
					if (!invStack.isEmpty() && predicate.test(invStack)) {
						return invStack.copyWithCount(1);
					}
				}
			}

			// Also check Curios slots for WorldChestItem (if Curios is loaded)
			if (ModList.get().isLoaded("curios")) {
				try {
					Class<?> curiosApi = Class.forName("top.theillusivec4.curios.api.CuriosApi");
					Method getCurios = curiosApi.getMethod("getCuriosInventory", LivingEntity.class);
					Object lazyOpt = getCurios.invoke(null, golem);
					Object opt = lazyOpt.getClass().getMethod("resolve").invoke(lazyOpt);
					if (opt == null) return ItemStack.EMPTY;
					if (!(boolean) opt.getClass().getMethod("isPresent").invoke(opt)) return ItemStack.EMPTY;
					Object handler = opt.getClass().getMethod("get").invoke(opt);
					Object equipped = handler.getClass().getMethod("getEquippedCurios").invoke(handler);
					Method getSlots = equipped.getClass().getMethod("getSlots");
					Method getStackInSlot = equipped.getClass().getMethod("getStackInSlot", int.class);
					int slots = (int) getSlots.invoke(equipped);
					for (int i = 0; i < slots; i++) {
						ItemStack curiosStack = (ItemStack) getStackInSlot.invoke(equipped, i);
						if (curiosStack.isEmpty() || !wcClass.isInstance(curiosStack.getItem())) continue;

						Object bpOpt = getContainer.invoke(curiosStack.getItem(), curiosStack, golem.level());
						if ((boolean) bpOpt.getClass().getMethod("isEmpty").invoke(bpOpt)) continue;
						Object bpStorage = bpOpt.getClass().getMethod("get").invoke(bpOpt);
						Field bpF = bpStorage.getClass().getField("container");
						var bpInv = (net.minecraft.world.SimpleContainer) bpF.get(bpStorage);

						for (int j = 0; j < bpInv.getContainerSize(); j++) {
							ItemStack bpStack = bpInv.getItem(j);
							if (!bpStack.isEmpty() && predicate.test(bpStack)) {
								return bpStack.copyWithCount(1);
							}
						}
					}
				} catch (Exception ignored) {
				}
			}
		} catch (Exception ignored) {
		}
		return ItemStack.EMPTY;
	}
}
