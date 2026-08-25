package src.toi_et_moi.mgdp.modifier.farming;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class FurnaceModifier extends AbstractCookingModifier {

	@Override
	protected Optional<AbstractCookingRecipe> findRecipe(ItemStack stack, AbstractGolemEntity<?, ?> golem) {
		SimpleContainer container = new SimpleContainer(stack);
		Level level = golem.level();
		var manager = level.getRecipeManager();

		Optional<AbstractCookingRecipe> ans = manager.getRecipeFor(RecipeType.SMELTING, container, level)
				.map(r -> (AbstractCookingRecipe) r);
		if (ans.isPresent()) return ans;
		ans = manager.getRecipeFor(RecipeType.BLASTING, container, level)
				.map(r -> (AbstractCookingRecipe) r);
		if (ans.isPresent()) return ans;
		return manager.getRecipeFor(RecipeType.SMOKING, container, level)
				.map(r -> (AbstractCookingRecipe) r);
	}
}
