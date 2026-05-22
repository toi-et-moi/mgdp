package src.toi_et_moi.mgdp.modifier;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.entity.common.GolemFlags;
import dev.xkmc.modulargolems.content.entity.targeting.TargetManager;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class LightningStormModifier extends GolemModifier {

    private static final int INTERVAL = 60;
    private static final double RANGE = 48.0;

    public LightningStormModifier() {
        super(StatFilterType.ATTACK, 1);
    }

    @Override
    public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
        if (golem.level().isClientSide()) return;
        if (golem.tickCount % INTERVAL != 0) return;

        AABB area = golem.getBoundingBox().inflate(RANGE);
        List<LivingEntity> targets = golem.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e.isAlive() && e != golem && shouldStrike(golem, e));

        ServerLevel sl = (ServerLevel) golem.level();
        if (sl.isThundering()) {
            area = golem.getBoundingBox().inflate(RANGE * 2);
            targets = golem.level().getEntitiesOfClass(LivingEntity.class, area,
                    e -> e.isAlive() && e != golem && shouldStrike(golem, e));
        }
        if (targets.isEmpty()) return;

        float dmg = (float) golem.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (sl.isThundering()) dmg *= 3.0F;

        for (LivingEntity target : targets) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(sl);
            bolt.setPos(target.getX(), target.getY(), target.getZ());
            bolt.setVisualOnly(true);
            sl.addFreshEntity(bolt);

            target.invulnerableTime = 0;
            target.hurt(sl.damageSources().lightningBolt(), dmg);
        }
    }

    private static boolean shouldStrike(AbstractGolemEntity<?, ?> golem, LivingEntity e) {
        if (e == golem.getTarget()) return true;
        if (e instanceof AbstractGolemEntity<?, ?> eg && eg.hasFlag(GolemFlags.THUNDER_IMMUNE))
            return golem.isAlliedTo(eg);
        return TargetManager.wantsToAttack(golem, e);
    }
}
