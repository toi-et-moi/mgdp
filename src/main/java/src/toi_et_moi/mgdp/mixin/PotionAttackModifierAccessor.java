package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.content.modifier.base.PotionAttackModifier;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Function;

@Mixin(PotionAttackModifier.class)
public interface PotionAttackModifierAccessor {

    @Accessor(remap = false)
    Function<Integer, MobEffectInstance> getFunc();
}
