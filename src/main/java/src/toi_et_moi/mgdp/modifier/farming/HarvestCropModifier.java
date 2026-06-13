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
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.lang.reflect.Method;
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
    public void onAttacked(AbstractGolemEntity<?, ?> entity, LivingAttackEvent event, int level) {
		if (event.getSource().is(DamageTypes.SWEET_BERRY_BUSH)
				|| event.getSource().is(DamageTypes.CACTUS)) {
            event.setCanceled(true);
		}
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
					} else if (tryHarvestPineapple(level, pos, state, block)) {
						// handled: FruitsDelight pineapple
					} else if (tryHarvestMushroomColony(level, pos, state, block)) {
						// handled: FarmersDelight mushroom colony
					} else if (tryHarvestModCrop(level, pos, state)) {
						// handled by L2Harvester HarvestableBlock API
					} else if (isAgeBasedCrop(block) && isMature(state, block)) {
						Block.dropResources(state, level, pos);
						level.setBlock(pos, getReplantState(state, block), Block.UPDATE_CLIENTS);
					} else if (isTowerCrop(block) && level.getBlockState(pos.below()).getBlock() == block) {
						Block.dropResources(state, level, pos);
						level.removeBlock(pos, false);
					} else if (golem.getMainHandItem().getItem() instanceof ShearsItem
							&& (block instanceof GrowingPlantHeadBlock
								|| block instanceof VineBlock
								|| block instanceof GrowingPlantBodyBlock)) {
						Block.dropResources(state, level, pos, null, golem, golem.getMainHandItem());
						level.levelEvent(2001, pos, Block.getId(state));
						level.removeBlock(pos, false);
					} else if (block instanceof AmethystClusterBlock && block == Blocks.AMETHYST_CLUSTER) {
						Direction facing = state.getValue(AmethystClusterBlock.FACING);
						if (level.getBlockState(pos.relative(facing.getOpposite())).is(Blocks.BUDDING_AMETHYST)) {
							level.destroyBlock(pos, true);
						}
					} else if (block instanceof StemGrownBlock) {
						if (isAttachedToStem(level, pos)) {
							level.destroyBlock(pos, true);
						}
					} else if ((block instanceof SculkSensorBlock || block instanceof SculkShriekerBlock)
							&& golem.getMainHandItem().getItem() instanceof HoeItem) {
						level.levelEvent(2001, pos, Block.getId(state));
						Block.dropResources(state, level, pos, null, golem, golem.getMainHandItem());
						level.removeBlock(pos, false);
					} else if (golem.getMainHandItem().getItem() instanceof AxeItem
							&& (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES))) {
						ItemStack tool = golem.getMainHandItem();
						boolean dualWield = golem.getOffhandItem().getItem() instanceof AxeItem;
						if (state.is(BlockTags.LOGS) && !isTreeLog(level, pos, dualWield)) {
							continue;
						}
						if (state.is(BlockTags.LEAVES) && !dualWield) {
							boolean adjProtected = false;
							for (Direction dir : Direction.values()) {
								BlockPos n = pos.relative(dir);
								if (level.getBlockState(n).is(BlockTags.LOGS) && !isTreeLog(level, n, false)) {
									adjProtected = true;
									break;
								}
							}
							if (adjProtected) continue;
						}
						Block.dropResources(state, level, pos, null, golem, tool);
						level.levelEvent(2001, pos, Block.getId(state));
						level.removeBlock(pos, false);
					} else if (block instanceof BushBlock && tryRightClickHarvest(level, pos, state, golem)) {
						// handled by FakePlayer right-click simulation
					} else if (golem.getMainHandItem().getItem() instanceof SwordItem
							&& block == Blocks.COBWEB) {
						ItemStack tool = golem.getMainHandItem();
						Block.dropResources(state, level, pos, null, golem, tool);
						level.levelEvent(2001, pos, Block.getId(state));
						level.removeBlock(pos, false);
					} else if (golem.getMainHandItem().getItem() instanceof ShearsItem
							&& state.is(BlockTags.FLOWERS)) {
						ItemStack tool = golem.getMainHandItem();
						Block.dropResources(state, level, pos, null, golem, tool);
						level.levelEvent(2001, pos, Block.getId(state));
						level.removeBlock(pos, false);
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

	// --- stem-attached detection for melon/pumpkin/honeydew ---

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

	// --- FruitsDelight pineapple special case (soft dependency, reflection) ---

	private static boolean pineappleChecked = false;
	private static Class<?> pineappleBlockClass;

	private boolean tryHarvestPineapple(Level level, BlockPos pos, BlockState state, Block block) {
		if (!initPineapple()) return false;
		if (!pineappleBlockClass.isInstance(block)) return false;
		try {
			int age = state.getValue(BlockStateProperties.AGE_4);
			if (age < 4) return false;
			Block.dropResources(state, level, pos);
			level.setBlock(pos, state.setValue(BlockStateProperties.AGE_4, 0), Block.UPDATE_CLIENTS);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private static boolean initPineapple() {
		if (pineappleChecked) return pineappleBlockClass != null;
		pineappleChecked = true;
		try {
			pineappleBlockClass = Class.forName("dev.xkmc.fruitsdelight.content.block.PineappleBlock");
			return true;
		} catch (Exception e) {
			pineappleBlockClass = null;
			return false;
		}
	}

	// --- FarmersDelight mushroom colony special case (soft dependency, reflection) ---

	private static boolean fdMushroomChecked = false;
	private static Class<?> mushroomColonyClass;

	private boolean tryHarvestMushroomColony(Level level, BlockPos pos, BlockState state, Block block) {
		if (!initMushroomColony()) return false;
		if (!mushroomColonyClass.isInstance(block)) return false;
		try {
			int age = state.getValue(BlockStateProperties.AGE_3);
			if (age < 3) return false;
			Block.dropResources(state, level, pos);
			level.setBlock(pos, state.setValue(BlockStateProperties.AGE_3, 0), Block.UPDATE_CLIENTS);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private static boolean initMushroomColony() {
		if (fdMushroomChecked) return mushroomColonyClass != null;
		fdMushroomChecked = true;
		try {
			mushroomColonyClass = Class.forName("vectorwing.farmersdelight.common.block.MushroomColonyBlock");
			return true;
		} catch (Exception e) {
			mushroomColonyClass = null;
			return false;
		}
	}


	// --- Tree detection: BFS over connected logs, checks horizontal count and nearby leaves ---

	private static boolean isTreeLog(Level level, BlockPos pos, boolean dualWield) {
		if (dualWield) return true; // dual-wielding axes bypasses all safety checks
		Set<BlockPos> visited = new HashSet<>();
		Queue<BlockPos> queue = new LinkedList<>();
		queue.add(pos);
		visited.add(pos);
		int horizontalCount = 0;
		boolean hasLeaves = false;
		while (!queue.isEmpty()) {
			BlockPos current = queue.poll();
			for (Direction dir : Direction.values()) {
				BlockPos neighbor = current.relative(dir);
				if (visited.contains(neighbor)) continue;
				if (!level.isLoaded(neighbor)) continue;
				BlockState state = level.getBlockState(neighbor);
				if (state.is(BlockTags.LEAVES)) hasLeaves = true;
				if (state.is(BlockTags.LOGS) && visited.size() < 64) {
					visited.add(neighbor);
					queue.add(neighbor);
					if (dir.getAxis() != Direction.Axis.Y) horizontalCount++;
				}
			}
		}
		return hasLeaves && horizontalCount <= 10;
	}

	// --- FakePlayer right-click harvest fallback (兼容支持右键收获的模组作物) ---

	private static FakePlayer fakePlayer;

	private boolean tryRightClickHarvest(Level level, BlockPos pos, BlockState state, AbstractGolemEntity<?, ?> golem) {
		if (!(level instanceof ServerLevel sl)) return false;
		if (fakePlayer == null || fakePlayer.level() != level) {
			fakePlayer = FakePlayerFactory.getMinecraft(sl);
		}
		fakePlayer.moveTo(golem.getX(), golem.getY(), golem.getZ(), 0, 0);
		BlockHitResult hitResult = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
		InteractionResult result = state.use(level, fakePlayer, InteractionHand.MAIN_HAND, hitResult);
		return result.consumesAction();
	}

	// --- L2Harvester HarvestableBlock compat (reflection, soft dependency) ---

	private static boolean l2HarvesterChecked = false;
	private static Class<?> harvestableBlockClass;
	private static Method harvestableGetResult;
	private static Method harvestResultDrops;
	private static Method harvestResultUpdateState;

	private boolean tryHarvestModCrop(Level level, BlockPos pos, BlockState state) {
		if (!initL2Harvester()) return false;
		Block block = state.getBlock();
		if (!harvestableBlockClass.isInstance(block)) return false;
		try {
			Object result = harvestableGetResult.invoke(block, level, state, pos);
			if (result == null) return true; // block is present but not mature
			@SuppressWarnings("unchecked")
			List<ItemStack> drops = (List<ItemStack>) harvestResultDrops.invoke(result);
			for (ItemStack stack : drops) {
				Block.popResource(level, pos, stack);
			}
			harvestResultUpdateState.invoke(result, level, pos);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private static boolean initL2Harvester() {
		if (l2HarvesterChecked) return harvestableBlockClass != null;
		l2HarvesterChecked = true;
		try {
			harvestableBlockClass = Class.forName("dev.xkmc.l2harvester.api.HarvestableBlock");
			harvestableGetResult = harvestableBlockClass.getMethod("getHarvestResult", Level.class, BlockState.class, BlockPos.class);
			Class<?> resultClass = Class.forName("dev.xkmc.l2harvester.api.HarvestResult");
			harvestResultDrops = resultClass.getMethod("drops");
			harvestResultUpdateState = resultClass.getMethod("updateState", Level.class, BlockPos.class);
			return true;
		} catch (Exception e) {
			harvestableBlockClass = null;
			return false;
		}
	}
}
