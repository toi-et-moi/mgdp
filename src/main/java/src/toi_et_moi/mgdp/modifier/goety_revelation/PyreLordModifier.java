package src.toi_et_moi.mgdp.modifier.goety_revelation;
import net.minecraft.core.registries.BuiltInRegistries;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import dev.xkmc.modulargolems.content.entity.common.GolemFlags;
import java.util.function.Consumer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class PyreLordModifier extends GolemModifier {

    private static final int DEFAULT_HEIGHT = 32;
    private static final int FALLBACK_MIN = 8;

    public PyreLordModifier() {
        super(StatFilterType.HEALTH, 1);
    }

    /** Spawn Y: default 32 blocks above golem; if blocked, find highest open space >=8 */
    private static int findSpawnY(AbstractGolemEntity<?, ?> golem) {
        var level = golem.level();
        var pos = golem.blockPosition();
        int golemY = pos.getY();
        int targetY = golemY + DEFAULT_HEIGHT;
        int maxY = level.getMaxBuildHeight() - 1;
        if (targetY > maxY) targetY = maxY;

        // Check if space up to targetY is clear
        for (int y = golemY + 1; y <= targetY; y++) {
            if (!level.isEmptyBlock(new net.minecraft.core.BlockPos(pos.getX(), y, pos.getZ()))) {
                int openAbove = y - golemY - 1;
                if (openAbove >= FALLBACK_MIN) return y - 1;
                return -1;
            }
        }
        return targetY;
    }

    @Override
    public void onRegisterFlag(Consumer<GolemFlags> addFlag) {
        addFlag.accept(GolemFlags.FIRE_IMMUNE);
    }

    @Override
    public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
        if (golem.level().isClientSide()) return;
        if (golem.tickCount % 20 != 0) return; // every second
        if (!net.minecraftforge.fml.ModList.get().isLoaded("goety")) return;

        // Remove burn_hex from self (immunity)
        var hex = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("goety", "burn_hex"));
        if (hex != null) {
            golem.removeEffect(hex);
            // Burn hex aura: apply to valid hostile targets within 35 blocks
            var box = golem.getBoundingBox().inflate(35);
            for (var target : golem.level().getEntitiesOfClass(LivingEntity.class, box,
                    e -> e.isAlive() && !e.equals(golem)
                            && dev.xkmc.modulargolems.content.entity.targeting.TargetManager.wantsToAttack(golem, e))) {
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(hex, 1200, 4));
            }
        }

        int spawnY = findSpawnY(golem);
        src.toi_et_moi.mgdp.Mgdp.LOGGER.info("PyreLord: golem={} tick={} spawnY={}", golem, golem.tickCount, spawnY);
        if (spawnY < 0) return;

        try {
            var type = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation("goety", "nether_meteor"));
            if (type == null) {
                src.toi_et_moi.mgdp.Mgdp.LOGGER.warn("PyreLord: nether_meteor entity type not found");
                return;
            }

            var ctor = Class.forName("com.Polarice3.Goety.common.entities.projectiles.NetherMeteor")
                    .getConstructor(net.minecraft.world.level.Level.class, LivingEntity.class,
                            double.class, double.class, double.class);

            var rng = golem.getRandom();
            int spread = rng.nextInt(600) - 300;
            double accelX = spread;
            double accelY = -900.0D;
            double accelZ = rng.nextInt(600) - 300;

            Object meteor = ctor.newInstance(golem.level(), golem, accelX, accelY, accelZ);

            var pos = golem.blockPosition();
            ((net.minecraft.world.entity.Entity) meteor).setPos(pos.getX() + 0.5, spawnY, pos.getZ() + 0.5);
            // Store golem UUID for persistent owner reference after golem is recycled
            ((net.minecraft.world.entity.Entity) meteor).getPersistentData()
                    .putUUID("mgdp_meteor_owner", golem.getUUID());
            golem.level().addFreshEntity((net.minecraft.world.entity.Entity) meteor);

            // Additional aimed meteor: fire directly at current target
            LivingEntity target = golem.getTarget();
            if (target != null && target.isAlive()) {
                double dx = target.getX() - golem.getX();
                double dy = target.getEyeY() - golem.getEyeY();
                double dz = target.getZ() - golem.getZ();
                double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (d > 0.01) {
                    float speed = 3.0F;
                    Object aimed = ctor.newInstance(golem.level(), golem,
                            dx / d * speed, dy / d * speed, dz / d * speed);
                    ((net.minecraft.world.entity.Entity) aimed).setPos(
                            golem.getX(), golem.getEyeY() - 0.3, golem.getZ());
                    ((net.minecraft.world.entity.Entity) aimed).getPersistentData()
                            .putUUID("mgdp_meteor_owner", golem.getUUID());
                    try {
                        aimed.getClass().getMethod("setDamage", float.class).invoke(aimed, 24.0F);
                        aimed.getClass().getMethod("setExplosionPower", float.class).invoke(aimed, 12.0F);
                    } catch (Exception ignored) {}
                    golem.level().addFreshEntity((net.minecraft.world.entity.Entity) aimed);
                }
            }

            try {
                meteor.getClass().getMethod("setDamage", float.class).invoke(meteor, 24.0F);
                meteor.getClass().getMethod("setExplosionPower", float.class).invoke(meteor, 12.0F);
            } catch (Exception ignored) {}

            golem.level().addFreshEntity((net.minecraft.world.entity.Entity) meteor);
            src.toi_et_moi.mgdp.Mgdp.LOGGER.info("PyreLord: spawned meteor at y={}", spawnY);
        } catch (Exception e) {
            src.toi_et_moi.mgdp.Mgdp.LOGGER.warn("PyreLord: meteor failed", e);
        }
    }

    @Override
    public void onAttacked(AbstractGolemEntity<?, ?> golem, LivingAttackEvent event, int level) {
        // Explosion immunity (fire is handled by GolemFlags.FIRE_IMMUNE)
        var src = event.getSource();
        if (src.is(DamageTypes.EXPLOSION) || src.is(DamageTypes.PLAYER_EXPLOSION)
                || src.is(DamageTypes.FIREBALL) || src.is(DamageTypes.UNATTRIBUTED_FIREBALL)) {
            event.setCanceled(true);
        }
    }

    @Override
    public void onHurt(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
        // Hellfire damage -75%
        var hellfireKey = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("goety", "hellfire"));
        if (event.getSource().is(hellfireKey)) {
            event.setAmount(event.getAmount() * 0.25F);
        }
    }

    @Override
    public void onHurtTarget(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
        var source = event.getSource();
        // 4x fire/explosion/hellfire damage
        if (source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.IN_FIRE)
                || source.is(DamageTypes.LAVA) || source.is(DamageTypes.EXPLOSION)
                || source.is(DamageTypes.PLAYER_EXPLOSION) || source.is(DamageTypes.FIREBALL)
                || source.is(DamageTypes.UNATTRIBUTED_FIREBALL)
                || source.is(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("goety", "hellfire")))) {
            event.setAmount(event.getAmount() * 4.0F);
        }
    }
}
