package src.toi_et_moi.mgdp.modifier;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.List;

public class AnglerModifier extends GolemModifier {

	private static final ResourceLocation FISHING_LOOT = new ResourceLocation("minecraft:gameplay/fishing");

	public AnglerModifier() {
		super(StatFilterType.MASS, 1);
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		if (golem.tickCount % 20 != 0) return;

		if (golem.getTarget() != null) return;

		ItemStack rod = golem.getMainHandItem().is(Items.FISHING_ROD) ? golem.getMainHandItem()
				: golem.getOffhandItem().is(Items.FISHING_ROD) ? golem.getOffhandItem()
				: ItemStack.EMPTY;

		if (!nearWater(golem)) return;

		int lure = rod.isEmpty() ? 0 : EnchantmentHelper.getTagEnchantmentLevel(Enchantments.FISHING_SPEED, rod) + 1;
		int lastCast = golem.getPersistentData().getInt("mgdp_fishing_cast");
		if (golem.tickCount - lastCast < Math.max(100, 400 - lure * 60)) return;

		int luck = rod.isEmpty() ? 0 : EnchantmentHelper.getTagEnchantmentLevel(Enchantments.FISHING_LUCK, rod);

		ServerLevel sl = (ServerLevel) golem.level();

		// Generate fishing loot from vanilla loot table
		try {
			LootDataId<LootTable> id = new LootDataId<>(LootDataType.TABLE, FISHING_LOOT);
			LootTable table = sl.getServer().getLootData().getElement(id);
			if (table != null) {
				LootParams params = new LootParams.Builder(sl)
						.withParameter(LootContextParams.ORIGIN, golem.position())
						.withParameter(LootContextParams.TOOL, rod.isEmpty() ? net.minecraft.world.item.ItemStack.EMPTY : rod)
						.withLuck(luck)
						.create(LootContextParamSets.FISHING);
				java.util.List<net.minecraft.world.item.ItemStack> loot = table.getRandomItems(params);
				for (net.minecraft.world.item.ItemStack stack : loot) {
					sl.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(sl, golem.getX(), golem.getEyeY(), golem.getZ(), stack));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// XP
		int xp = sl.getRandom().nextInt(6) + 1;
		while (xp > 0) {
			int orb = ExperienceOrb.getExperienceValue(xp);
			xp -= orb;
			sl.addFreshEntity(new ExperienceOrb(sl, golem.getX(), golem.getEyeY(), golem.getZ(), orb));
		}

		// Rod damage
		if (!rod.isEmpty()) {
			rod.hurtAndBreak(1, golem, e -> {});
		}

		// Sound
		golem.level().playSound(null, golem.blockPosition(), SoundEvents.FISHING_BOBBER_SPLASH,
				SoundSource.NEUTRAL, 0.5F, 1.0F);

		golem.getPersistentData().putInt("mgdp_fishing_cast", golem.tickCount);
	}

	private static boolean nearWater(AbstractGolemEntity<?, ?> golem) {
		BlockPos center = golem.blockPosition();
		for (int dx = -10; dx <= 10; dx++) {
			for (int dz = -10; dz <= 10; dz++) {
				for (int dy = -3; dy <= 3; dy++) {
					BlockPos pos = center.offset(dx, dy, dz);
					if (!golem.level().isLoaded(pos)) continue;
					if (golem.level().getBlockState(pos).is(Blocks.WATER)) return true;
				}
			}
		}
		return false;
	}

	@Override
	public List<MutableComponent> getDetail(int v) {
		return List.of(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GREEN));
	}
}
