package src.toi_et_moi.mgdp.modifier.farming;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;
import java.util.Optional;

public abstract class AbstractCookingModifier extends GolemModifier {

	public AbstractCookingModifier() {
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

	/** Finds the matching furnace-type recipe (carrying its result and experience). */
	protected abstract Optional<AbstractCookingRecipe> findRecipe(ItemStack stack, AbstractGolemEntity<?, ?> golem);

	private void processHand(AbstractGolemEntity<?, ?> golem, InteractionHand hand) {
		ItemStack stack = golem.getItemInHand(hand);
		if (stack.isEmpty()) return;
		Optional<AbstractCookingRecipe> opt = findRecipe(stack, golem);
		if (opt.isEmpty()) return;
		AbstractCookingRecipe recipe = opt.get();

		int count = stack.getCount();
		ItemStack output = recipe.getResultItem(golem.level().registryAccess()).copy();
		output.setCount(count);

		golem.setItemInHand(hand, output);
		spawnExperience(golem, recipe.getExperience(), count);
	}

	private void scanContainers(AbstractGolemEntity<?, ?> golem) {
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

	private void processSlot(ItemStack stack, AbstractGolemEntity<?, ?> golem,
							 IItemHandler handler, int slot) {
		Optional<AbstractCookingRecipe> opt = findRecipe(stack, golem);
		if (opt.isEmpty()) return;
		AbstractCookingRecipe recipe = opt.get();

		int count = stack.getCount();
		ItemStack template = recipe.getResultItem(golem.level().registryAccess());
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
		spawnExperience(golem, recipe.getExperience(), count);
	}

	private static void spawnExperience(AbstractGolemEntity<?, ?> golem, float experience, int count) {
		if (experience <= 0 || count <= 0) return;
		if (!(golem.level() instanceof ServerLevel sl)) return;
		float total = experience * count;
		int xp = Mth.floor(total);
		if (sl.random.nextFloat() < Mth.frac(total)) xp++;
		while (xp > 0) {
			int orb = ExperienceOrb.getExperienceValue(xp);
			xp -= orb;
			sl.addFreshEntity(new ExperienceOrb(sl, golem.getX(), golem.getEyeY(), golem.getZ(), orb));
		}
	}

	@Override
	public List<MutableComponent> getDetail(int v) {
		return List.of(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GREEN));
	}
}
