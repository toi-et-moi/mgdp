package src.toi_et_moi.mgdp.modifier.farming;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.stream.StreamSupport;

public class LiquidClearModifier extends GolemModifier {

	private static final int INTERVAL = 20;

	public LiquidClearModifier() {
		super(StatFilterType.MASS, 4);
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		if (golem.tickCount % INTERVAL != 0) return;

		int range = level * src.toi_et_moi.mgdp.Config.liquidClearRangePerLevel;
		BlockPos center = golem.blockPosition();
		StreamSupport.stream(
				BlockPos.withinManhattan(center, range, range, range).spliterator(), false
		)
				.forEach(pos -> {
					if (!golem.level().isLoaded(pos)) return;
					BlockState state = golem.level().getBlockState(pos);
					Block block = state.getBlock();
					// Remove liquid blocks entirely
					if (block instanceof LiquidBlock || block == Blocks.BUBBLE_COLUMN) {
						golem.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
						return;
					}
					// Remove underwater plants + their water by replacing with air
					if (!state.getFluidState().isEmpty() && !(block instanceof LiquidBlock)) {
						golem.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
						return;
					}
					// Remove waterlogging from waterlogged blocks
					if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED)
							&& state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED)) {
						golem.level().setBlock(pos, state.setValue(
								net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED, false), 3);
					}
				});
	}

	@Override
	public List<MutableComponent> getDetail(int v) {
		int perLevel = src.toi_et_moi.mgdp.Config.liquidClearRangePerLevel;
		int totalRange = v * 8;
		return List.of(
				Component.translatable(getDescriptionId() + ".desc",
						perLevel, totalRange).withStyle(ChatFormatting.GREEN)
		);
	}
}
