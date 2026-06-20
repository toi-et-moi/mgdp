package src.toi_et_moi.mgdp.modifier.hostility;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.items.IItemHandler;

import java.util.List;
import java.util.Map;

public class ReprintModifier extends GolemModifier {

	public ReprintModifier() {
		super(StatFilterType.ATTACK, 1);
	}

	@Override
	public void onHurtTarget(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		applyReprint(golem, golem.getOffhandItem());

		if (!(event.getEntity() instanceof LivingEntity)) return;
		LivingEntity target = (LivingEntity) event.getEntity();
		float bonus = calcReprintDamage(target);
		if (bonus > 0) {
			event.setAmount(event.getAmount() * (1 + bonus));
		}
	}

	private static float calcReprintDamage(LivingEntity target) {
		long total = 0;
		for (var slot : EquipmentSlot.values()) {
			ItemStack src = target.getItemBySlot(slot);
			var ench = src.getAllEnchantments();
			for (var e : ench.entrySet()) {
				int lv = e.getValue();
				if (lv >= 30) {
					total = -1;
					break;
				} else if (total >= 0) {
					total += 1L << lv;
				}
			}
			if (total < 0) break;
		}
		long pts = total >= 0 ? Math.min(total, 1000) : (1L << 30);
		return 0.02f * pts;
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		if (golem.tickCount % 20 != 0) return;

		ItemStack mainHand = golem.getMainHandItem();
		if (mainHand.isEmpty()) return;
		Map<Enchantment, Integer> srcEnch = EnchantmentHelper.getEnchantments(mainHand);
		if (srcEnch.isEmpty()) return;

		int range = 1;
		BlockPos center = golem.blockPosition();
		Level levelWorld = golem.level();

		for (int dx = -range; dx <= range; dx++) {
			for (int dz = -range; dz <= range; dz++) {
				for (int dy = -1; dy <= 1; dy++) {
					BlockPos pos = center.offset(dx, dy, dz);
					if (!levelWorld.isLoaded(pos)) continue;
					BlockEntity be = levelWorld.getBlockEntity(pos);
					if (be == null) continue;

					var opt = be.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
					if (!opt.isPresent()) continue;

					opt.ifPresent(handler -> {
						for (int i = 0; i < handler.getSlots(); i++) {
							ItemStack stack = handler.getStackInSlot(i);
							if (stack.isEmpty()) continue;
							ItemStack modified = tryEnchantItem(stack.copy(), srcEnch);
							if (!modified.equals(stack, false)) {
								int count = stack.getCount();
								int fitted = 0;
								for (int j = 0; j < count; j++) {
									ItemStack single = modified.copy();
									single.setCount(1);
									ItemStack left = net.minecraftforge.items.ItemHandlerHelper.insertItemStacked(handler, single, false);
									if (left.isEmpty()) fitted++;
								}
								if (fitted > 0) {
									handler.extractItem(i, count, false);
									if (fitted < count) {
										ItemStack drop = modified.copy();
										drop.setCount(count - fitted);
										golem.spawnAtLocation(drop);
									}
								}
							}
						}
					});
				}
			}
		}
	}

	private static ItemStack tryEnchantItem(ItemStack stack, Map<Enchantment, Integer> srcEnch) {
		boolean isBook = stack.is(Items.BOOK) || stack.is(Items.ENCHANTED_BOOK);
		ItemStack target = isBook ? new ItemStack(Items.ENCHANTED_BOOK) : stack.copy();

		Map<Enchantment, Integer> dstEnch = EnchantmentHelper.getEnchantments(target);
		boolean changed = false;

		for (var entry : srcEnch.entrySet()) {
			Enchantment ench = entry.getKey();
			if (ench == null) continue;
			if (!isBook && !ench.canEnchant(stack)) continue;
			boolean compatible = true;
			for (var existing : dstEnch.entrySet()) {
				if (existing.getKey() != ench && !existing.getKey().isCompatibleWith(ench)) {
					compatible = false;
					break;
				}
			}
			if (compatible) {
				dstEnch.merge(ench, entry.getValue(), Math::max);
				changed = true;
			}
		}

		if (changed) {
			EnchantmentHelper.setEnchantments(dstEnch, target);
			return target;
		}
		return stack;
	}

	private static void applyReprint(AbstractGolemEntity<?, ?> golem, ItemStack targetStack) {
		var mainHand = golem.getMainHandItem();
		if (mainHand.isEmpty() || targetStack.isEmpty()) return;

		Map<Enchantment, Integer> srcEnch = EnchantmentHelper.getEnchantments(mainHand);
		if (srcEnch.isEmpty()) return;

		Map<Enchantment, Integer> dstEnch = EnchantmentHelper.getEnchantments(targetStack);
		boolean changed = false;

		for (var entry : srcEnch.entrySet()) {
			Enchantment ench = entry.getKey();
			if (ench == null) continue;
			if (!ench.canEnchant(targetStack)) continue;

			boolean compatible = true;
			for (var existing : dstEnch.entrySet()) {
				if (existing.getKey() != ench && !existing.getKey().isCompatibleWith(ench)) {
					compatible = false;
					break;
				}
			}
			if (compatible) {
				dstEnch.merge(ench, entry.getValue(), Math::max);
				changed = true;
			}
		}

		if (changed) {
			EnchantmentHelper.setEnchantments(dstEnch, targetStack);
		}
	}

	@Override
	public List<MutableComponent> getDetail(int v) {
		return List.of(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GREEN));
	}
}
