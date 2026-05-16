package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.l2complements.content.enchantment.weapon.AbstractBladeEnchantment;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractBladeEnchantment.class)
public interface AbstractBladeEnchantmentAccessor {

    @Invoker(remap = false)
    MobEffectInstance callGetEffect(int level);
}
