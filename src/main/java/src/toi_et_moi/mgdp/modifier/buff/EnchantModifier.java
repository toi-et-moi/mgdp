package src.toi_et_moi.mgdp.modifier.buff;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Auto-enchant upgrade. Max level 3. Each level adds 10 levels of
 * enchantment-table power (per MGDP convention), so level 1 = 10
 * levels, level 2 = 20, level 3 = 30.
 *
 * <h2>Behavior</h2>
 * <ul>
 *   <li>Every {@value #COOLDOWN_TICKS} ticks (1 s), scan:
 *     <ol>
 *       <li>The golem's own armor + hands.</li>
 *       <li>Allied living entities' (owner / same team) armor +
 *           hands within {@value #ALLY_RADIUS} blocks.</li>
 *       <li>Every container within {@value #SCAN_RADIUS} blocks.</li>
 *     </ol>
 *   </li>
 *   <li>Unenchanted item: roll an enchantment using the
 *       level-equivalent power (vanilla
 *       {@link EnchantmentHelper#enchantItem}).</li>
 *   <li>Already enchanted item: skipped at levels 1/2, upgraded
 *       one level at a time toward max at level 3.</li>
 *   <li>No "treasure" / "curse" enchantments are ever applied
 *       (we pass {@code allowTreasure=false} to vanilla
 *       {@code enchantItem}).</li>
 * </ul>
 */
public class EnchantModifier extends GolemModifier {

    private static final int COOLDOWN_TICKS = 20;
    private static final double SCAN_RADIUS = 1.0;
    private static final double ALLY_RADIUS = 1.0;

    public EnchantModifier() {
        super(StatFilterType.MASS, 3);
    }

    @Override
    public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
        if (golem.level().isClientSide()) return;
        if (level < 1) return;
        if (golem.tickCount % COOLDOWN_TICKS != 0) return;

        int power = level * 10;
        RandomSource random = golem.getRandom();

        for (SlotRef ref : collectTargets(golem)) {
            ItemStack stack = ref.stack();
            if (stack.isEmpty()) continue;

            // NOTE: 1.20.1 ItemStack.isEnchantable() 内部会检查 !isEnchanted()，
            // 所以已附魔物品 isEnchantable()==false——必须先把 L3 max 化分支提到前面，
            // 否则 upgradeToMax() 永远到不了。
            if (stack.isEnchanted()) {
                if (level >= 3) upgradeToMax(stack);
                continue;
            }

            // BOOK 整组模式：vanilla enchantItem 对 BOOK 总是返回单件 ENCHANTED_BOOK，
            // 整组 stack 完全不动——必须自己按 count 拆成单件、逐本附魔、再 insertItemStacked
            // 塞回容器；这样能保证"成组的书直接变为附魔书且保持堆叠"，避免丢 count。
            // 这种"重新插回"操作只对容器里的 slot 有意义（GOLEM/友方身上不可能放成组的书）。
            if (ref.handler() != null && stack.is(Items.BOOK)) {
                enchantBookStack(ref, random, power, golem);
                continue;
            }

            if (!stack.isEnchantable()) continue;
            EnchantmentHelper.enchantItem(random, stack, power, false);
        }
    }

    /**
     * 把容器里整组 BOOK (count>=1) 全部变为对应数量的 ENCHANTED_BOOK 单件，
     * 紧邻 slot 就近插入（通过 insertItemStacked 优先合并到已有同附魔书堆叠），
     * 抽走原 slot 的所有 BOOK。插不下的作为掉落物 spawn 出来。
     */
    private static void enchantBookStack(SlotRef ref, RandomSource random, int power,
                                         AbstractGolemEntity<?, ?> golem) {
        IItemHandler handler = ref.handler();
        ItemStack books = ref.stack();
        int count = books.getCount();

        List<ItemStack> enchantedBooks = new ArrayList<>(count);
        for (int j = 0; j < count; j++) {
            ItemStack singleBook = books.copy();
            singleBook.setCount(1);
            ItemStack enchanted = EnchantmentHelper.enchantItem(random, singleBook, power, false);
            enchantedBooks.add(enchanted);
        }

        // 抽空原 slot（确认 count 没变，因为 onAiStep 持有 stack 引用期间 handler 不会被改）
        handler.extractItem(ref.index(), count, false);

        // 整组 insertItemStacked——优先合并到已有同附魔书堆叠，再填紧邻空 slot
        for (ItemStack book : enchantedBooks) {
            ItemStack left = ItemHandlerHelper.insertItemStacked(handler, book, false);
            if (!left.isEmpty()) golem.spawnAtLocation(left);
        }
    }

    /** Slot 引用：携带 stack 本身、来源 handler、slot index。 */
    private record SlotRef(ItemStack stack, IItemHandler handler, int index) {
        /** GOLEM/友方装备没有 handler，直接修改原 stack 即可。 */
        static SlotRef direct(ItemStack stack) {
            return new SlotRef(stack, null, -1);
        }
    }

    private static List<SlotRef> collectTargets(AbstractGolemEntity<?, ?> golem) {
        List<SlotRef> out = new ArrayList<>();
        // 1. Golem 自身
        addEntityEquipment(golem, out);

        if (!golem.level().isClientSide()) {
            // 2. 友方实体 (owner / 同队) 的装备
            AABB allyBox = golem.getBoundingBox().inflate(ALLY_RADIUS);
            for (LivingEntity ally : golem.level().getEntitiesOfClass(
                    LivingEntity.class, allyBox, e -> e != golem && isAlly(golem, e))) {
                addEntityEquipment(ally, out);
            }

            // 3. 周围 4 格内的 IItemHandler 容器（Forge capability 模式，覆盖原版箱子 +
            //    漏斗 + Storage Drawers + AE2 + Quark 等所有实现 ITEM_HANDLER 的方块实体）
            AABB box = golem.getBoundingBox().inflate(SCAN_RADIUS);
            int r = (int) Math.ceil(SCAN_RADIUS);
            BlockPos center = golem.blockPosition();
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    for (int dz = -r; dz <= r; dz++) {
                        BlockPos pos = center.offset(dx, dy, dz);
                        if (!box.contains(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) continue;
                        if (!golem.level().isLoaded(pos)) continue;
                        BlockEntity be = golem.level().getBlockEntity(pos);
                        if (be == null) continue;

                        var cap = be.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
                        if (cap.isPresent()) {
                            IItemHandler handler = cap.orElseThrow(IllegalStateException::new);
                            for (int i = 0; i < handler.getSlots(); i++) {
                                out.add(new SlotRef(handler.getStackInSlot(i), handler, i));
                            }
                        }
                    }
                }
            }
        }
        return out;
    }

    private static void addEntityEquipment(LivingEntity e, List<SlotRef> out) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                out.add(SlotRef.direct(e.getItemBySlot(slot)));
            }
        }
        out.add(SlotRef.direct(e.getMainHandItem()));
        out.add(SlotRef.direct(e.getOffhandItem()));
    }

    /** 友方判定：同队 / 主人 / 互相 alliedTo 都算 */
    private static boolean isAlly(AbstractGolemEntity<?, ?> golem, Entity other) {
        if (other.is(golem)) return false;
        if (golem.isAlliedTo(other)) return true;
        if (other.isAlliedTo(golem)) return true;
        var owner = golem.getOwner();
        return owner != null && owner.is(other);
    }

    private static void upgradeToMax(ItemStack stack) {
        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
        for (Map.Entry<Enchantment, Integer> e : map.entrySet()) {
            int cur = e.getValue();
            int max = e.getKey().getMaxLevel();
            if (cur < max) {
                map.put(e.getKey(), max);
            }
        }
        EnchantmentHelper.setEnchantments(map, stack);
    }
}
