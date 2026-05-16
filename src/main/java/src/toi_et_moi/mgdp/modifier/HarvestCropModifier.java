package src.toi_et_moi.mgdp.modifier;

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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class HarvestCropModifier extends GolemModifier {

	private static final int SCAN_INTERVAL = 100; // 5 seconds

	public HarvestCropModifier() {
		super(StatFilterType.MASS, 1);
	}

	@Override
	public List<MutableComponent> getDetail(int v) {
		int range = MGConfig.COMMON.basePickupRange.get();
		return List.of(Component.translatable(getDescriptionId() + ".desc", range).withStyle(ChatFormatting.GREEN));
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int modifierLevel) {
		if (golem.level().isClientSide()) return;
		if (golem.tickCount % SCAN_INTERVAL != 0) return;

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
		Level level = golem.level();
		ServerLevel sl = (ServerLevel) level;

		int yMin = -1;
		int yMax = 25;

		// First pass: harvest dead chorus flowers individually
		List<BlockPos> harvestedFlowers = new ArrayList<>();

		for (int dx = -range; dx <= range; dx++) {
			for (int dz = -range; dz <= range; dz++) {
				for (int dy = yMin; dy <= yMax; dy++) {
					BlockPos pos = center.offset(dx, dy, dz);
					if (!level.isLoaded(pos)) continue;
					BlockState state = level.getBlockState(pos);
					Block block = state.getBlock();

					if (block instanceof ChorusFlowerBlock && state.getValue(ChorusFlowerBlock.AGE) >= 5) {
						List<ItemStack> drops = Block.getDrops(state, sl, pos, level.getBlockEntity(pos));
						drops.add(new ItemStack(Items.CHORUS_FLOWER));
						level.removeBlock(pos, false);
						for (ItemStack drop : drops) {
							Block.popResource(level, pos, drop);
						}
						harvestedFlowers.add(pos);
					} else if (isAgeBasedCrop(block) && isMature(state, block)) {
						Block.dropResources(state, level, pos);
						level.setBlock(pos, getReplantState(state, block), Block.UPDATE_CLIENTS);
					} else if (isTowerCrop(block) && level.getBlockState(pos.below()).getBlock() == block) {
						Block.dropResources(state, level, pos);
						level.removeBlock(pos, false);
					} else if (block instanceof MelonBlock || block instanceof PumpkinBlock) {
						if (isAttachedToStem(level, pos)) {
							level.destroyBlock(pos, true);
						}
					}
				}
			}
		}

		// Second pass: clear and replant flowerless plants, each plant once
		Set<BlockPos> processedStems = new HashSet<>();
		for (BlockPos flowerPos : harvestedFlowers) {
			BlockPos stem = flowerPos.below();
			if (!level.isLoaded(stem)) continue;
			if (!(level.getBlockState(stem).getBlock() instanceof ChorusPlantBlock)) continue;
			if (processedStems.contains(stem)) continue;

			Set<BlockPos> plantBlocks = collectChorusPlant(level, stem);
			processedStems.addAll(plantBlocks);

			boolean hasFlower = false;
			for (BlockPos p : plantBlocks) {
				if (level.getBlockState(p).getBlock() instanceof ChorusFlowerBlock) {
					hasFlower = true;
					break;
				}
			}
			if (!hasFlower) {
				clearAndReplant(level, plantBlocks);
			}
		}
	}

	// --- chorus plant helpers ---

	private Set<BlockPos> collectChorusPlant(Level level, BlockPos start) {
		Set<BlockPos> visited = new HashSet<>();
		Queue<BlockPos> queue = new LinkedList<>();
		queue.add(start);
		visited.add(start);

		while (!queue.isEmpty()) {
			BlockPos pos = queue.poll();
			for (Direction dir : Direction.values()) {
				BlockPos neighbor = pos.relative(dir);
				if (visited.contains(neighbor)) continue;
				if (!level.isLoaded(neighbor)) continue;
				Block nb = level.getBlockState(neighbor).getBlock();
				if (nb instanceof ChorusPlantBlock || nb instanceof ChorusFlowerBlock) {
					visited.add(neighbor);
					queue.add(neighbor);
				}
			}
		}
		return visited;
	}

	private void clearAndReplant(Level level, Set<BlockPos> plantBlocks) {
		BlockPos lowest = null;
		for (BlockPos pos : plantBlocks) {
			level.destroyBlock(pos, true);
			if (lowest == null || pos.getY() < lowest.getY()) lowest = pos;
		}
		if (lowest != null) {
			level.setBlock(lowest, Blocks.CHORUS_FLOWER.defaultBlockState()
					.setValue(ChorusFlowerBlock.AGE, 0), Block.UPDATE_CLIENTS);
		}
	}

	// --- stem-attached detection for melon/pumpkin ---

	private boolean isAttachedToStem(Level level, BlockPos pos) {
		for (Direction dir : Direction.Plane.HORIZONTAL) {
			if (level.getBlockState(pos.relative(dir)).getBlock() instanceof AttachedStemBlock) {
				return true;
			}
		}
		return false;
	}

	// --- crop classification helpers ---

	private boolean isAgeBasedCrop(Block block) {
		return block instanceof CropBlock          // wheat, potato, carrot, beetroot, torchflower, etc.
				|| block instanceof PitcherCropBlock // pitcher plant (not a CropBlock subclass)
				|| block instanceof NetherWartBlock
				|| block instanceof SweetBerryBushBlock
				|| block instanceof CaveVinesBlock
				|| block instanceof CaveVinesPlantBlock
				|| block instanceof CocoaBlock;
	}

	private boolean isTowerCrop(Block block) {
		return block instanceof CactusBlock
				|| block instanceof SugarCaneBlock
				|| block instanceof BambooStalkBlock
				|| block instanceof KelpBlock
				|| block instanceof KelpPlantBlock;
	}

	private boolean isMature(BlockState state, Block block) {
		if (block instanceof CropBlock crop) return crop.isMaxAge(state);
		if (block instanceof PitcherCropBlock) return state.getValue(PitcherCropBlock.AGE) >= PitcherCropBlock.MAX_AGE;
		if (block instanceof NetherWartBlock) return state.getValue(NetherWartBlock.AGE) >= NetherWartBlock.MAX_AGE;
		if (block instanceof SweetBerryBushBlock) return state.getValue(SweetBerryBushBlock.AGE) >= SweetBerryBushBlock.MAX_AGE;
		if (block instanceof CaveVinesBlock) return state.getValue(CaveVinesBlock.BERRIES);
		if (block instanceof CaveVinesPlantBlock) return state.getValue(CaveVinesPlantBlock.BERRIES);
		if (block instanceof CocoaBlock) return state.getValue(CocoaBlock.AGE) >= CocoaBlock.MAX_AGE;
		return false;
	}

	private BlockState getReplantState(BlockState state, Block block) {
		if (block instanceof CropBlock crop) return crop.getStateForAge(0);
		if (block instanceof PitcherCropBlock) return state.setValue(PitcherCropBlock.AGE, 0);
		if (block instanceof NetherWartBlock) return state.setValue(NetherWartBlock.AGE, 0);
		if (block instanceof SweetBerryBushBlock) return state.setValue(SweetBerryBushBlock.AGE, 0);
		if (block instanceof CaveVinesBlock) return state.setValue(CaveVinesBlock.BERRIES, false);
		if (block instanceof CaveVinesPlantBlock) return state.setValue(CaveVinesPlantBlock.BERRIES, false);
		if (block instanceof CocoaBlock) return state.setValue(CocoaBlock.AGE, 0);
		return state;
	}
}
