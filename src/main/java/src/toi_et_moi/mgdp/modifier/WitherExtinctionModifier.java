package src.toi_et_moi.mgdp.modifier;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.entity.targeting.TargetManager;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class WitherExtinctionModifier extends GolemModifier {

    private static final int CHARGE_TIME = 200; // 10 seconds
    private static final double RANGE = 64.0;
    private static final int COOLDOWN = 1200; // 60 seconds
    private static final String TAG_CHARGE = "mgdp_wither_charge";
    private static final String TAG_COOLDOWN = "mgdp_wither_cooldown";

    public WitherExtinctionModifier() {
        super(StatFilterType.HEALTH, 1);
    }

    @Override
    public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
        if (golem.level().isClientSide()) return;
        CompoundTag tag = golem.getPersistentData();
        long now = golem.level().getGameTime();
        ServerLevel sl = (ServerLevel) golem.level();

        int charge = tag.getInt(TAG_CHARGE);
        long cooldown = tag.getLong(TAG_COOLDOWN);

        if (charge > 0) {
            charge--;
            tag.putInt(TAG_CHARGE, charge);

            if (charge % 40 == 0) {
                golem.playSound(SoundEvents.WITHER_AMBIENT, 2.0F, 1.0F);
            }
            sl.sendParticles(ParticleTypes.SMOKE,
                    golem.getX(), golem.getY(), golem.getZ(),
                    128, 0, 0, 0, 0.6);

            if (charge <= 0) {
                tag.putLong(TAG_COOLDOWN, now + COOLDOWN);
                detonate(sl, golem);
            }
            return;
        }

        if (now < cooldown) return;

        AABB area = golem.getBoundingBox().inflate(RANGE);
        List<LivingEntity> targets = sl.getEntitiesOfClass(LivingEntity.class, area,
                e -> e.isAlive() && e != golem
                        && (e == golem.getTarget() || TargetManager.wantsToAttack(golem, e)));

        if (!targets.isEmpty()) {
            tag.putInt(TAG_CHARGE, CHARGE_TIME);
            golem.playSound(SoundEvents.WITHER_AMBIENT, 2.0F, 1.0F);
        }
    }

    private void detonate(ServerLevel sl, AbstractGolemEntity<?, ?> golem) {
        AABB area = golem.getBoundingBox().inflate(RANGE);
        List<LivingEntity> targets = sl.getEntitiesOfClass(LivingEntity.class, area,
                e -> e.isAlive() && e != golem
                        && (e == golem.getTarget() || TargetManager.wantsToAttack(golem, e)));

        DamageSource witherDmg = sl.damageSources().wither();
        DamageSource explosionDmg = sl.damageSources().explosion(null);

        sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                golem.getX(), golem.getY(), golem.getZ(), 1, 0, 0, 0, 0);
        sl.sendParticles(ParticleTypes.EXPLOSION,
                golem.getX(), golem.getY(), golem.getZ(), 16, 4, 2, 4, 0);

        for (LivingEntity target : targets) {
            target.hurt(witherDmg, target.getMaxHealth());
        }

        golem.playSound(SoundEvents.WITHER_SPAWN, 3.0F, 1.0F);

        for (LivingEntity target : targets) {
            target.invulnerableTime = 0;
            target.hurt(explosionDmg, target.getMaxHealth());
        }
    }
}
