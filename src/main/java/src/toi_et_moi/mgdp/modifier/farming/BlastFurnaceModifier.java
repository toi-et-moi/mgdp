package src.toi_et_moi.mgdp.modifier.farming;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;

public class BlastFurnaceModifier extends GolemModifier {

	public BlastFurnaceModifier() {
		super(StatFilterType.MASS, 1);
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		if (golem.tickCount % 20 != 0) return;

		processHand(golem, InteractionHand.MAIN_HAND);
		processHand(golem, InteractionHand.OFF_HAND);

		scanContainers(golem);
	}

	private static void processHand(AbstractGolemEntity<?, ?> golem, InteractionHand hand) {
		ItemStack stack = golem.getItemInHand(hand);
		if (stack.isEmpty()) return;
		ItemStack result = getSmeltResult(stack, golem);
		if (result.isEmpty()) return;

		int count = stack.getCount();
		ItemStack output = result.copy();
		output.setCount(count);

		golem.setItemInHand(hand, output);
	}

	private static void scanContainers(AbstractGolemEntity<?, ?> golem) {
		int range = 1;
		BlockPos center = golem.blockPosition();
		Level level = golem.level();

		for (int dx = -range; dx <= range; dx++) {
			for (int dz = -range; dz <= range; dz++) {
				for (int dy = -1; dy <= 1; dy++) {
					BlockPos pos = center.offset(dx, dy, dz);
					if (!level.isLoaded(pos)) continue;
					BlockEntity be = level.getBlockEntity(pos);
					if (be == null) continue;

					var opt = be.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
					if (!opt.isPresent()) continue;

					opt.ifPresent(handler -> {
						for (int i = 0; i < handler.getSlots(); i++) {
							ItemStack slotStack = handler.getStackInSlot(i);
							if (slotStack.isEmpty()) continue;
							processSlot(slotStack, golem, handler, i);
						}
					});
				}
			}
		}
	}

	private static void processSlot(ItemStack stack, AbstractGolemEntity<?, ?> golem,
									IItemHandler handler, int slot) {
		ItemStack template = getSmeltResult(stack, golem);
		if (template.isEmpty()) return;

		int count = stack.getCount();
		int maxStack = template.getMaxStackSize();

		// First clear the slot so there's room for output
		handler.extractItem(slot, count, false);

		// Calculate how many items can go back into the same slot
		int fitted = Math.min(count, maxStack);
		ItemStack output = template.copy();
		output.setCount(fitted);
		ItemStack remainder = handler.insertItem(slot, output, false);
		fitted -= remainder.getCount(); // actual fitted in the original slot

		// Try to fit remaining output in other slots
		if (fitted < count) {
			ItemStack extra = template.copy();
			extra.setCount(count - fitted);
			ItemStack stillLeft = ItemHandlerHelper.insertItemStacked(handler, extra, false);
			if (!stillLeft.isEmpty()) {
				golem.spawnAtLocation(stillLeft);
			}
		}
	}

	private static ItemStack getSmeltResult(ItemStack stack, AbstractGolemEntity<?, ?> golem) {
		SimpleContainer container = new SimpleContainer(stack);
		var recipe = golem.level().getRecipeManager()
				.getRecipeFor(net.minecraft.world.item.crafting.RecipeType.BLASTING, container, golem.level());
		return recipe.map(r -> r.getResultItem(golem.level().registryAccess()).copy()).orElse(ItemStack.EMPTY);
	}

	@Override
	public List<MutableComponent> getDetail(int v) {
		return List.of(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GREEN));
	}
}
