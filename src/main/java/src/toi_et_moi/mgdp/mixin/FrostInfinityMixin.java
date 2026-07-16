package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.mob_weapon_api.api.projectile.BowUseContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(dev.xkmc.mob_weapon_api.example.behavior.SimpleBowBehavior.class)
public class FrostInfinityMixin {

    @Redirect(method = "shootArrow", at = @At(value = "INVOKE", target = "Ldev/xkmc/mob_weapon_api/api/projectile/BowUseContext;hasInfiniteArrow(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"), remap = false)
    private boolean mgdp$frostInfinity(BowUseContext ctx, ItemStack bowStack, ItemStack arrowStack) {
        if (ctx.hasInfiniteArrow(bowStack, arrowStack)) return true;

        if (!net.minecraftforge.fml.ModList.get().isLoaded("smc")) return false;
        ResourceLocation bowId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(bowStack.getItem());
        if (bowId == null || !bowId.getNamespace().equals("smc")) return false;
        if (!bowId.getPath().equals("frostium_bow") && !bowId.getPath().equals("perfrostite_bow")) return false;
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, bowStack) <= 0) return false;

        ResourceLocation frostArrowId = new ResourceLocation("smc", "frost_arrow");
        return arrowStack.is(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(frostArrowId));
    }
}
