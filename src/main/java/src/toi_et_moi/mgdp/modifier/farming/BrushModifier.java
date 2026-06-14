package src.toi_et_moi.mgdp.modifier.farming;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import dev.xkmc.modulargolems.content.modifier.special.PickupModifier;
import dev.xkmc.modulargolems.init.data.MGConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.List;

public class BrushModifier extends GolemModifier {

	public BrushModifier() {
		super(StatFilterType.MASS, 1);
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		if (golem.tickCount % 10 != 0) return;

		// Apply suspicious_smell if FruitsDelight is loaded
		if (net.minecraftforge.fml.ModList.get().isLoaded("fruitsdelight")) {
			var smell = net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getValue(
					new net.minecraft.resources.ResourceLocation("fruitsdelight", "suspicious_smell"));
			if (smell != null) {
				var owner = golem.getOwner();
				if (owner != null && owner.isAlive() && golem.distanceToSqr(owner) < 400
						&& golem.tickCount % 20 == 0) {
					owner.addEffect(new net.minecraft.world.effect.MobEffectInstance(smell, 40, 0));
				}
			}
		}

		int pickupLevel = 0;
		for (var entry : golem.getModifiers().entrySet()) {
			if (entry.getKey() instanceof PickupModifier) {
				pickupLevel = entry.getValue();
				break;
			}
		}
		if (pickupLevel <= 0) return;

		int range = pickupLevel * MGConfig.COMMON.basePickupRange.get();
		BlockPos center = golem.blockPosition();

		for (int dx = -range; dx <= range; dx++) {
			for (int dz = -range; dz <= range; dz++) {
				for (int dy = -1; dy <= 1; dy++) {
					BlockPos pos = center.offset(dx, dy, dz);
					if (!golem.level().isLoaded(pos)) continue;
					BlockState state = golem.level().getBlockState(pos);
					if (!(state.getBlock() instanceof BrushableBlock)) continue;

					BlockEntity be = golem.level().getBlockEntity(pos);
					if (!(be instanceof BrushableBlockEntity brushable)) continue;

					var fakePlayer = FakePlayerFactory.getMinecraft((ServerLevel) golem.level());
					fakePlayer.setPos(golem.getX(), golem.getY(), golem.getZ());

					brushable.brush(golem.level().getGameTime(), fakePlayer, Direction.UP);

					ItemStack buried = brushable.getItem();
					if (!buried.isEmpty()) {
						Block.popResource(golem.level(), pos, buried);
						BlockState newState = ((BrushableBlock) state.getBlock()).getTurnsInto().defaultBlockState();
						golem.level().setBlock(pos, newState, 3);
					}
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
