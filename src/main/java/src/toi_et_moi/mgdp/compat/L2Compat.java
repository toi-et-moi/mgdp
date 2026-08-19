package src.toi_et_moi.mgdp.compat;

import dev.xkmc.l2complements.init.registrate.LCEnchantments;
import dev.xkmc.modulargolems.compat.materials.l2complements.FreezingModifier;
import dev.xkmc.modulargolems.compat.materials.l2complements.SoulFlameModifier;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public class L2Compat {

    public static boolean isLoaded() {
        return ModList.get().isLoaded("l2complements");
    }

    public static MobEffectInstance tryGetEffect(GolemModifier mod, int level) {
        if (!isLoaded()) return null;
        try {
            Object enchant = null;
            if (mod instanceof SoulFlameModifier) {
                enchant = LCEnchantments.FLAME_BLADE.get();
            } else if (mod instanceof FreezingModifier) {
                enchant = LCEnchantments.ICE_BLADE.get();
            }
            if (enchant != null) {
                Method m = enchant.getClass().getMethod("getEffect", int.class);
                m.setAccessible(true);
                return (MobEffectInstance) m.invoke(enchant, level);
            }
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * 自动冶炼兼容（l2complements:smelt）：
     * 莱特兰扩充的自动冶炼走的是"玩家破块"链路（SpecialEquipmentEvents 用
     * ThreadLocal 记录破块玩家），傀儡不是玩家所以永远不会触发。这里在掉落
     * 计算后自行按熔炉配方转换：铁矿石→铁锭、原木→木炭等。
     * 时运/精准采集已在 getDrops 阶段生效；精准采集出的原矿方块没有熔炉
     * 配方，天然不会被转换。
     */
    public static void tryAutoSmelt(Level level, ItemStack tool, List<ItemStack> drops) {
        if (!isLoaded() || tool.isEmpty() || drops.isEmpty()) return;
        try {
            if (EnchantmentHelper.getItemEnchantmentLevel(LCEnchantments.SMELT.get(), tool) <= 0) return;
            for (int i = 0; i < drops.size(); i++) {
                ItemStack stack = drops.get(i);
                if (stack.isEmpty()) continue;
                Optional<SmeltingRecipe> recipe = level.getRecipeManager()
                        .getRecipeFor(RecipeType.SMELTING, new SimpleContainer(stack), level);
                if (recipe.isPresent()) {
                    ItemStack result = recipe.get().getResultItem(level.registryAccess());
                    if (!result.isEmpty()) {
                        result = result.copy();
                        result.setCount(Math.min(result.getMaxStackSize(), result.getCount() * stack.getCount()));
                        drops.set(i, result);
                    }
                }
            }
        } catch (Exception ignored) {}
    }
}
