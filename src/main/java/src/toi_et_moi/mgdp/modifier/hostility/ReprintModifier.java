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
import net.minecraftforge.items.IItemHandlerModifiable;

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

							// 关键优化：tryReprint 内部已做完"是否真的会变"的判断，
							// 早返回 null 意味着复印后无变化，外层直接跳过整段 extract/set。
							ItemStack target = tryReprint(stack, srcEnch);
							if (target == null) continue;

							int count = stack.getCount();

							// 复印 BOOK 时：64 本书 → 1 本 ENCHANTED_BOOK（带 mainHand 附魔），
							// 然后强制 setStackInSlot 把 count 本附魔书塞回原 slot——
							// 绕过 vanilla maxStackSize=1 限制，避免 64 本分散在 64 slot 占满容器。
							// 对非 BOOK 物品（剑/工具/附魔书）count 通常是 1，行为退化为普通覆盖。
							//
							// setStackInSlot 在 IItemHandlerModifiable 子接口上（不是所有 IItemHandler
							// 都实现 modifiable），用 instanceof 检测后调用；否则 fallback 到 insertItem
							// 循环（旧行为，附魔书会占 maxStackSize=1 的多个 slot）。
							handler.extractItem(i, count, false);
							target.setCount(count);
							if (handler instanceof IItemHandlerModifiable mod) {
								mod.setStackInSlot(i, target);
							} else {
								ItemStack left = handler.insertItem(i, target, false);
								if (!left.isEmpty()) golem.spawnAtLocation(left);
							}
						}
					});
				}
			}
		}
	}

	/**
	 * 合并 srcEnch 附魔到 dstEnch：跳过不兼容 + 不超 cur 的（已 max）。
	 * 返回 dstEnch 是否有实际变化。isBook=true 时跳过 canEnchant 检查
	 * （canEnchant 对 BOOK 永远 false，但 BOOK 应该接所有 srcEnch 附魔作为 ENCHANTED_BOOK 存储）。
	 */
	private static boolean mergeEnchantments(Map<Enchantment, Integer> dstEnch,
	                                         Map<Enchantment, Integer> srcEnch,
	                                         boolean isBook, ItemStack contextStack) {
		boolean changed = false;
		for (var entry : srcEnch.entrySet()) {
			Enchantment ench = entry.getKey();
			if (ench == null) continue;
			if (!isBook && !ench.canEnchant(contextStack)) continue;

			int cur = dstEnch.getOrDefault(ench, 0);
			int want = entry.getValue();
			if (cur >= want) continue;

			boolean compatible = true;
			for (var existing : dstEnch.entrySet()) {
				if (existing.getKey() != ench && !existing.getKey().isCompatibleWith(ench)) {
					compatible = false;
					break;
				}
			}
			if (compatible) {
				dstEnch.put(ench, want);
				changed = true;
			}
		}
		return changed;
	}

	/**
	 * 尝试复印 srcEnch 到 stack。BOOK 整组转换为 ENCHANTED_BOOK；其他物品 copy 后 merge。
	 * 返回 null 表示复印后无变化（外层可直接跳过 extract/insert）。
	 * 返回非 null 时 target.count 总是 1（BOOK 分支）或 stack.count（其他），外层按原 stack.count 复制。
	 *
	 * <p>支持"未附魔装备被首次复印附魔"：当 stack 是未附魔的剑/工具但 canEnchant(srcEnch)
	 * 通过时，dstEnch 从空开始，mergeEnchantments 会把 srcEnch 全量加进去。</p>
	 */
	private static ItemStack tryReprint(ItemStack stack, Map<Enchantment, Integer> srcEnch) {
		boolean isBook = stack.is(Items.BOOK) || stack.is(Items.ENCHANTED_BOOK);

		ItemStack target = isBook ? new ItemStack(Items.ENCHANTED_BOOK) : stack.copy();
		Map<Enchantment, Integer> dstEnch = EnchantmentHelper.getEnchantments(target);
		boolean changed = mergeEnchantments(dstEnch, srcEnch, isBook, stack);
		if (!changed) return null;

		EnchantmentHelper.setEnchantments(dstEnch, target);
		return target;
	}

	private static void applyReprint(AbstractGolemEntity<?, ?> golem, ItemStack targetStack) {
		ItemStack mainHand = golem.getMainHandItem();
		if (mainHand.isEmpty() || targetStack.isEmpty()) return;

		Map<Enchantment, Integer> srcEnch = EnchantmentHelper.getEnchantments(mainHand);
		if (srcEnch.isEmpty()) return;

		// 不做 isEnchanted 早返回——支持"未附魔 offhand 被首次复印附魔"：
		// mergeEnchantments 内部对不兼容附魔会跳过，没有可合并附魔时返回 false，自然不写入。
		boolean isBook = targetStack.is(Items.BOOK) || targetStack.is(Items.ENCHANTED_BOOK);
		Map<Enchantment, Integer> dstEnch = EnchantmentHelper.getEnchantments(targetStack);
		boolean changed = mergeEnchantments(dstEnch, srcEnch, isBook, targetStack);
		if (changed) {
			EnchantmentHelper.setEnchantments(dstEnch, targetStack);
		}
	}

	@Override
	public List<MutableComponent> getDetail(int v) {
		return List.of(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GREEN));
	}
}
