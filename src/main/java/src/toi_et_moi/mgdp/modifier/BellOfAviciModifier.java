package src.toi_et_moi.mgdp.modifier;

import dev.xkmc.l2library.base.effects.EffectUtil;
import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class BellOfAviciModifier extends GolemModifier {

    private static final double RANGE = 64.0;
    private static final int INTERVAL = 40; // 2 seconds

    public BellOfAviciModifier() {
        super(StatFilterType.HEALTH, 1);
    }

    @Override
    public void onSetTarget(AbstractGolemEntity<?, ?> golem, Mob target, int level) {
        applyGlow(golem);
    }

    @Override
    public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
        if (golem.level().isClientSide()) return;
        if (golem.tickCount % INTERVAL != 0) return;
        teleportTargeters(golem);
    }

    private void applyGlow(AbstractGolemEntity<?, ?> golem) {
        AABB area = golem.getBoundingBox().inflate(RANGE);
        List<Mob> nearby = golem.level().getEntitiesOfClass(Mob.class, area,
                e -> e instanceof Enemy && !(e instanceof Creeper) && e.canAttack(golem));

        boolean played = false;
        for (Mob mob : nearby) {
            if (!mob.hasEffect(MobEffects.GLOWING)) {
                played = true;
            }
            EffectUtil.addEffect(mob, new MobEffectInstance(MobEffects.GLOWING, 200),
                    EffectUtil.AddReason.NONE, golem);
            if (!(mob.getTarget() instanceof AbstractGolemEntity)) {
                mob.setTarget(golem);
            }
        }
        if (played) {
            golem.playSound(SoundEvents.BELL_BLOCK, 1.0F, 1.0F);
        }
    }

    private void teleportTargeters(AbstractGolemEntity<?, ?> golem) {
        AABB area = golem.getBoundingBox().inflate(RANGE);
        List<Mob> nearby = golem.level().getEntitiesOfClass(Mob.class, area,
                e -> e.isAlive() && e.getTarget() == golem);

        for (Mob mob : nearby) {
            double angle = golem.getRandom().nextDouble() * Math.PI * 2;
            double dist = 2.0 + golem.getRandom().nextDouble() * 3.0;
            double tx = golem.getX() + Math.cos(angle) * dist;
            double ty = golem.getY();
            double tz = golem.getZ() + Math.sin(angle) * dist;
            mob.teleportTo(tx, ty, tz);
        }
    }
}
