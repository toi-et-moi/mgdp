package src.toi_et_moi.mgdp.modifier;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.entity.targeting.TargetManager;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public class DragonBreathModifier extends GolemModifier {

    private static final int[] INTERVALS = {120, 100, 80};
    private static final int[] BALLS = {1, 2, 3};
    private static final int[] MAX_TARGETS = {2, 4, 6};
    private static final int[] EXPLOSION_POWER = {2, 4, 6};

    public DragonBreathModifier() {
        super(StatFilterType.ATTACK, 3);
    }

    @Override
    public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
        if (golem.level().isClientSide()) return;
        int idx = level - 1;
        if (golem.tickCount % INTERVALS[idx] != 0) return;

        ServerLevel sl = (ServerLevel) golem.level();
        AABB area = golem.getBoundingBox().inflate(40.0);
        List<LivingEntity> allTargets = sl.getEntitiesOfClass(LivingEntity.class, area,
                e -> e.isAlive() && e != golem && (e == golem.getTarget() || TargetManager.wantsToAttack(golem, e)));

        List<LivingEntity> selected = allTargets.stream()
                .sorted((a, b) -> {
                    if (a == golem.getTarget()) return -1;
                    if (b == golem.getTarget()) return 1;
                    return 0;
                })
                .limit(MAX_TARGETS[idx]).toList();

        float atk = (float) golem.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float baseExplosionDmg = EXPLOSION_POWER[idx];

        for (LivingEntity target : selected) {
            boolean canSee = golem.hasLineOfSight(target);
            int count = BALLS[idx];
            for (int i = 0; i < count; i++) {
                double dx, dy, dz;
                double px, py, pz;
                if (canSee) {
                    dx = golem.getRandom().nextGaussian() * 0.5;
                    dy = 0.5 + golem.getRandom().nextDouble() * 0.5;
                    dz = golem.getRandom().nextGaussian() * 0.5;
                    px = golem.getX();
                    py = golem.getY() + 1.5;
                    pz = golem.getZ();
                } else {
                    dx = 0;
                    dy = -1 - golem.getRandom().nextDouble() * 0.5;
                    dz = 0;
                    px = target.getX() + golem.getRandom().nextGaussian() * 2;
                    py = target.getY() + 5 + golem.getRandom().nextDouble() * 3;
                    pz = target.getZ() + golem.getRandom().nextGaussian() * 2;
                }

                DragonFireball ball = new DragonFireball(
                        sl, golem,
                        target.getX() - px + dx,
                        target.getY() - py + dy,
                        target.getZ() - pz + dz
                );
                ball.setPos(px, py, pz);
                sl.addFreshEntity(ball);

                var tag = ball.getPersistentData();
                tag.putFloat("mgdp_atk", atk);
                tag.putFloat("mgdp_explosion", baseExplosionDmg);
                tag.putUUID("mgdp_target", target.getUUID());
            }
        }
    }

}
