package src.toi_et_moi.mgdp.compat;

import dev.xkmc.l2complements.init.registrate.LCEnchantments;
import dev.xkmc.modulargolems.compat.materials.l2complements.FreezingModifier;
import dev.xkmc.modulargolems.compat.materials.l2complements.SoulFlameModifier;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;

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
}
