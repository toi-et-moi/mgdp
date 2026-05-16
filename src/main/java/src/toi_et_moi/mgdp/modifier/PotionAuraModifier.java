package src.toi_et_moi.mgdp.modifier;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import dev.xkmc.modulargolems.content.modifier.base.PotionAttackModifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;
import src.toi_et_moi.mgdp.compat.L2Compat;
import src.toi_et_moi.mgdp.mixin.PotionAttackModifierAccessor;

import java.util.ArrayList;
import java.util.List;

public class PotionAuraModifier extends GolemModifier {

    private static final int POSITIVE_INTERVAL = 20;
    private static final int NEGATIVE_INTERVAL = 20;
    private static final double RANGE = 48.0;

    public PotionAuraModifier() {
        super(StatFilterType.MOVEMENT, 1);
    }

    @Override
    public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
        if (golem.level().isClientSide()) return;

        List<MobEffectInstance> positive = new ArrayList<>();
        List<MobEffectInstance> negative = new ArrayList<>();

        for (MobEffectInstance effect : golem.getActiveEffects()) {
            if (effect.getEffect().isBeneficial()) {
                positive.add(effect);
            } else {
                negative.add(effect);
            }
        }

        for (var entry : golem.getModifiers().entrySet()) {
            GolemModifier mod = entry.getKey();
            int modLv = entry.getValue();
            if (mod instanceof PotionAttackModifier atk) {
                MobEffectInstance inst = ((PotionAttackModifierAccessor) atk).getFunc().apply(modLv);
                if (inst != null && !inst.getEffect().isBeneficial()) {
                    negative.add(inst);
                }
            }
            MobEffectInstance l2 = L2Compat.tryGetEffect(mod, modLv);
            if (l2 != null && !l2.getEffect().isBeneficial()) {
                negative.add(l2);
            }
        }

        if (positive.isEmpty() && negative.isEmpty()) return;

        int tick = golem.tickCount;

        if (!positive.isEmpty() && tick % POSITIVE_INTERVAL == 0) {
            applyToAllies(golem, positive);
        }

        if (!negative.isEmpty() && tick % NEGATIVE_INTERVAL == 0) {
            applyToEnemies(golem, negative);
        }
    }

    private void applyToAllies(AbstractGolemEntity<?, ?> golem, List<MobEffectInstance> effects) {
        AABB area = golem.getBoundingBox().inflate(RANGE);
        List<LivingEntity> nearby = golem.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != golem && e.isAlive() && golem.isAlliedTo(e));

        for (LivingEntity entity : nearby) {
            for (MobEffectInstance src : effects) {
                MobEffectInstance existing = entity.getEffect(src.getEffect());
                if (existing == null || existing.getDuration() <= 10) {
                    entity.addEffect(new MobEffectInstance(src));
                }
            }
        }
    }

    private void applyToEnemies(AbstractGolemEntity<?, ?> golem, List<MobEffectInstance> effects) {
        LivingEntity golemTarget = golem.getTarget();
        AABB area = golem.getBoundingBox().inflate(RANGE);
        List<LivingEntity> nearby = golem.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != golem && e.isAlive() && !golem.isAlliedTo(e) && (e instanceof Enemy || e == golemTarget));

        for (LivingEntity entity : nearby) {
            for (MobEffectInstance src : effects) {
                MobEffectInstance existing = entity.getEffect(src.getEffect());
                if (existing == null || existing.getDuration() <= 10) {
                    entity.addEffect(new MobEffectInstance(src));
                }
            }
        }
    }
}
