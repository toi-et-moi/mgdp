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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class FlareModifier extends GolemModifier {

	public FlareModifier() {
		super(StatFilterType.MASS, 1);
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;

		if (golem.tickCount % 20 == 0) {
			// Cook food in hands like a campfire
			for (InteractionHand hand : InteractionHand.values()) {
				ItemStack stack = golem.getItemInHand(hand);
				if (stack.isEmpty()) continue;

				SimpleContainer container = new SimpleContainer(stack);
				var recipe = golem.level().getRecipeManager()
						.getRecipeFor(RecipeType.CAMPFIRE_COOKING, container, golem.level());

				if (recipe.isPresent()) {
					ItemStack result = recipe.get().getResultItem(golem.level().registryAccess());
					if (!result.isEmpty()) {
						stack.shrink(1);
						ItemStack cooked = result.copy();
						if (stack.isEmpty()) {
							golem.setItemInHand(hand, cooked);
						} else {
							var item = new net.minecraft.world.entity.item.ItemEntity(golem.level(),
									golem.getX(), golem.getY() + 0.5, golem.getZ(),
									cooked);
							golem.level().addFreshEntity(item);
						}
					}
					break;
				}
			}

			// Regeneration aura for nearby allies
			AABB area = golem.getBoundingBox().inflate(8);
			for (LivingEntity target : golem.level().getEntitiesOfClass(LivingEntity.class, area,
					e -> e.isAlive() && !e.isSpectator() && (
							e == golem
							|| (e instanceof Player player && player == golem.getOwner())
							|| (e instanceof AbstractGolemEntity<?, ?> other
								&& other.getOwnerUUID() != null
								&& other.getOwnerUUID().equals(golem.getOwnerUUID()))))) {
				target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, false, false, true));
			}
		}

		// Place torches in dark areas
		if (golem.tickCount % 20 != 0) return;

		int range = 16;
		BlockPos center = golem.blockPosition();

		for (int dx = -range; dx <= range; dx++) {
			for (int dz = -range; dz <= range; dz++) {
				for (int dy = -4; dy <= 4; dy++) {
					BlockPos pos = center.offset(dx, dy, dz);
					if (!golem.level().isLoaded(pos)) continue;
					if (!golem.level().isEmptyBlock(pos)) continue;
					if (golem.level().getBrightness(LightLayer.BLOCK, pos) > 0) continue;

					BlockPos below = pos.below();
					BlockState belowState = golem.level().getBlockState(below);
					if (!belowState.isSolid()) continue;

					golem.level().setBlock(pos, Blocks.TORCH.defaultBlockState(), 3);
					return;
				}
			}
		}
	}

	@Override
	public List<MutableComponent> getDetail(int v) {
		return List.of(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GREEN));
	}
}
