package src.toi_et_moi.mgdp.compat;

import dev.xkmc.l2complements.init.registrate.LCEnchantments;
import dev.xkmc.modulargolems.compat.materials.l2complements.FreezingModifier;
import dev.xkmc.modulargolems.compat.materials.l2complements.SoulFlameModifier;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.fml.ModList;
import src.toi_et_moi.mgdp.mixin.AbstractBladeEnchantmentAccessor;

public class L2Compat {

    public static boolean isLoaded() {
        return ModList.get().isLoaded("l2complements");
    }

    public static MobEffectInstance tryGetEffect(GolemModifier mod, int level) {
        if (!isLoaded()) return null;
        try {
            if (mod instanceof SoulFlameModifier) {
                return ((AbstractBladeEnchantmentAccessor) LCEnchantments.FLAME_BLADE.get()).callGetEffect(level);
            }
            if (mod instanceof FreezingModifier) {
                return ((AbstractBladeEnchantmentAccessor) LCEnchantments.ICE_BLADE.get()).callGetEffect(level);
            }
        } catch (Exception ignored) {}
        return null;
    }
}
