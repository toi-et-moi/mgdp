package src.toi_et_moi.mgdp.modifier.farming;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import src.toi_et_moi.mgdp.Mgdp;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

import java.util.List;

@Mod.EventBusSubscriber(modid = Mgdp.MODID)
public class ProsperityModifier extends GolemModifier {

	public ProsperityModifier() {
		super(StatFilterType.MASS, 5);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onLivingDrops(LivingDropsEvent event) {
		if (event.getEntity().level().isClientSide()) return;
		if (!(event.getSource().getEntity() instanceof AbstractGolemEntity<?, ?> golem)) return;
		int level = golem.getModifiers().getOrDefault(MGDPModifiers.PROSPERITY.get(), 0);
		if (level <= 0) return;
		for (ItemEntity drop : event.getDrops()) {
			if (drop != null && !drop.getItem().isEmpty()) {
				drop.getItem().grow(drop.getItem().getCount() * level);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onExperienceDrop(LivingExperienceDropEvent event) {
		var lastSrc = event.getEntity().getLastDamageSource();
		if (lastSrc == null || !(lastSrc.getEntity() instanceof AbstractGolemEntity<?, ?> golem)) return;
		if (golem.level().isClientSide()) return;
		int level = golem.getModifiers().getOrDefault(MGDPModifiers.PROSPERITY.get(), 0);
		if (level <= 0) return;
		event.setDroppedExperience(event.getOriginalExperience() * (level + 1));
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		if (golem.tickCount % 40 != 0) return;

		double range = level * 4.0;
		if (!(golem.level() instanceof ServerLevel sl)) return;
		AABB area = golem.getBoundingBox().inflate(range);
		BlockPos.betweenClosedStream(area)
				.filter(pos -> {
					BlockState state = sl.getBlockState(pos);
					Block block = state.getBlock();
					if (block instanceof net.minecraft.world.level.block.CropBlock
							|| block instanceof net.minecraft.world.level.block.StemBlock
							|| block instanceof net.minecraft.world.level.block.SweetBerryBushBlock
							|| block instanceof net.minecraft.world.level.block.CocoaBlock) {
						if (block instanceof BonemealableBlock g && g.isValidBonemealTarget(sl, pos, state, false))
							return true;
						return isAgeableCrop(state);
					}
					return isAgeableCrop(state);
				})
				.forEach(pos -> {
					BlockState state = sl.getBlockState(pos);
					Block block = state.getBlock();
					if (block instanceof BonemealableBlock growable
							&& growable.isValidBonemealTarget(sl, pos, state, false)) {
						growable.performBonemeal(sl, sl.random, pos, state);
						sl.levelEvent(1505, pos, 0);
					} else {
						for (int i = 0; i < 3; i++) {
							block.randomTick(state, sl, pos, sl.random);
						}
					}
				});
	}

	private static boolean isAgeableCrop(BlockState state) {
		Block block = state.getBlock();
		if (block instanceof NetherWartBlock) return true;
		for (Property<?> prop : state.getProperties()) {
			if (prop instanceof IntegerProperty ip
					&& (ip.getName().equals("age") || ip.getName().equals("stage"))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<MutableComponent> getDetail(int v) {
		int perLevel = 4;
		int totalRange = v * 4;
		return List.of(
				Component.translatable(getDescriptionId() + ".desc",
						perLevel, totalRange).withStyle(ChatFormatting.GREEN)
		);
	}
}
