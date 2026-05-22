package src.toi_et_moi.mgdp.modifier;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class EnchantedCrimsonAttackModifier extends GolemModifier {

    public EnchantedCrimsonAttackModifier() {
        super(StatFilterType.ATTACK, 1);
    }

    @Override
    public void onHurtTarget(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
        event.getEntity().invulnerableTime = 0;

        CompoundTag tag = golem.getPersistentData();
        float heal = event.getAmount() * 0.5F;
        tag.putFloat("mgdp_crimson_heal", tag.getFloat("mgdp_crimson_heal") + heal);
    }

    @Override
    public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
        CompoundTag tag = golem.getPersistentData();
        float heal = tag.getFloat("mgdp_crimson_heal");
        if (heal > 0) {
            golem.heal(heal);
            tag.remove("mgdp_crimson_heal");
        }
    }
}
