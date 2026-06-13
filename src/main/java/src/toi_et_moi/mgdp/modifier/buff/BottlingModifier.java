package src.toi_et_moi.mgdp.modifier.buff;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class BottlingModifier extends GolemModifier {

    private static final int INTERVAL = 20;

    private static MobEffect getBottling() {
        var effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("goety", "bottling"));
        return effect != null ? effect : MobEffects.GLOWING;
    }

    public BottlingModifier() {
        super(StatFilterType.MASS, 5);
    }

    @Override
    public List<net.minecraft.network.chat.MutableComponent> getDetail(int v) {
        return List.of(net.minecraft.network.chat.Component.translatable(getDescriptionId() + ".desc", v)
                .withStyle(net.minecraft.ChatFormatting.GREEN));
    }

    @Override
    public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
        if (golem.level().isClientSide) return;
        if (golem.tickCount % INTERVAL != 0) return;
        var owner = golem.getOwner();
        if (owner != null && owner.isAlive() && golem.distanceToSqr(owner) < 400) {
            var effect = getBottling();
            if (effect != MobEffects.GLOWING) {
                owner.addEffect(new MobEffectInstance(effect, INTERVAL + 20, level - 1));
            }
        }
    }
}
