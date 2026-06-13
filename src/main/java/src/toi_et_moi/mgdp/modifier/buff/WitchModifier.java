package src.toi_et_moi.mgdp.modifier.buff;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class WitchModifier extends GolemModifier {

    private static final int BUFF_INTERVAL = 120;
    private static final int ATTACK_INTERVAL = 60;
    private static final int SUPPORT_INTERVAL = 100;
    private static final double POTION_SPEED = 1.5;
    private static final double SUPPORT_RANGE = 16.0;


    private static final List<MobEffectInstance> ATTACK_POTIONS = List.of(
            new MobEffectInstance(MobEffects.POISON, 200, 1),
            new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1),
            new MobEffectInstance(MobEffects.WEAKNESS, 200, 1),
            new MobEffectInstance(MobEffects.WITHER, 140, 0)
    );

    // Shared buff pool: self-buff and support potions both use this
    private static final List<MobEffectInstance> BUFF_POOL = List.of(
            new MobEffectInstance(MobEffects.REGENERATION, 1000, 1),
            new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1000, 0),
            new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1000, 0),
            new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1000, 0),
            new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1000, 0),
            new MobEffectInstance(MobEffects.ABSORPTION, 600, 1),
            new MobEffectInstance(MobEffects.DIG_SPEED, 1000, 2)
    );

    // Support potions including heal (for injured allies)
    private static final List<MobEffectInstance> SUPPORT_POTIONS = List.of(
            new MobEffectInstance(MobEffects.HEAL, 1, 2),
            new MobEffectInstance(MobEffects.REGENERATION, 1000, 1),
            new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1000, 0),
            new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1000, 0),
            new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1000, 0),
            new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1000, 0),
            new MobEffectInstance(MobEffects.ABSORPTION, 600, 1)
    );

    public WitchModifier() {
        super(StatFilterType.MASS, 2);
    }

    @Override
    public List<net.minecraft.network.chat.MutableComponent> getDetail(int v) {
        return List.of(net.minecraft.network.chat.Component.translatable(getDescriptionId() + ".desc")
                .withStyle(net.minecraft.ChatFormatting.GREEN));
    }

    @Override
    public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
        if (golem.level().isClientSide) return;
        int tick = golem.tickCount;

        // Self-buff (uses shared buff pool, 30s duration)
        if (tick % BUFF_INTERVAL == 0) {
            var buff = BUFF_POOL.get(golem.level().random.nextInt(BUFF_POOL.size()));
            golem.addEffect(new MobEffectInstance(buff));
        }

        // Attack potions — up to 3 targets
        if (tick % ATTACK_INTERVAL == 0) {
            var target = golem.getTarget();
            int targets = 0;
            if (target != null && target.isAlive() && golem.distanceToSqr(target) < 900) {
                throwPotion(golem, target.position().add(0, target.getBbHeight() * 0.5, 0),
                        pickAttackPotion(golem, target), level);
                targets++;
            }
            if (targets > 0) {
                for (var other : golem.level().getEntitiesOfClass(LivingEntity.class,
                        golem.getBoundingBox().inflate(30),
                        e -> e != target && e != golem && e.isAlive() && !e.isAlliedTo(golem)
                                && e.distanceToSqr(golem) < 900)) {
                    if (targets >= 3) break;
                    throwPotion(golem, other.position().add(0, other.getBbHeight() * 0.5, 0),
                            pickAttackPotion(golem, other), level);
                    targets++;
                }
            }
        }

        // Support potions — up to 3 allies
        if (tick % SUPPORT_INTERVAL == 0) {
            var allies = findAllies(golem);
            int targets = 0;
            for (var ally : allies) {
                if (targets >= 3) break;
                var effect = pickSupportPotion(golem, ally);
                throwPotion(golem, ally.position().add(0, ally.getBbHeight() * 0.5, 0), effect, level);
                targets++;
            }
        }
    }

    private static MobEffectInstance pickAttackPotion(AbstractGolemEntity<?, ?> golem, LivingEntity target) {
        return ATTACK_POTIONS.get(golem.level().random.nextInt(ATTACK_POTIONS.size()));
    }

    private static MobEffectInstance pickSupportPotion(AbstractGolemEntity<?, ?> golem, LivingEntity ally) {
        var rand = golem.level().random;
        boolean undead = ally.isInvertedHealAndHarm();
        float missing = 1.0f - ally.getHealth() / ally.getMaxHealth();
        // Undead ally: never use instant health (it damages them)
        if (undead) {
            return BUFF_POOL.get(rand.nextInt(BUFF_POOL.size()));
        }
        // Severely injured: 50% chance for instant heal
        if (missing > 0.5f && rand.nextFloat() < 0.5f) {
            return new MobEffectInstance(MobEffects.HEAL, 1, 2);
        }
        // Slightly injured: random from full pool (includes heal)
        if (ally.getHealth() < ally.getMaxHealth()) {
            return SUPPORT_POTIONS.get(rand.nextInt(SUPPORT_POTIONS.size()));
        }
        return BUFF_POOL.get(rand.nextInt(BUFF_POOL.size()));
    }

    private void throwPotion(AbstractGolemEntity<?, ?> golem, Vec3 target, MobEffectInstance effect, int level) {
        ItemStack potionStack = new ItemStack(level >= 2 ? Items.LINGERING_POTION : Items.SPLASH_POTION);
        PotionUtils.setCustomEffects(potionStack, List.of(effect));

        ThrownPotion potion = new ThrownPotion(golem.level(), golem);
        potion.setItem(potionStack);
        potion.setPos(golem.getX(), golem.getEyeY() - 0.3, golem.getZ());

        Vec3 dir = target.subtract(potion.position()).normalize();
        potion.setDeltaMovement(dir.scale(POTION_SPEED));

        golem.level().addFreshEntity(potion);
    }

    private List<LivingEntity> findAllies(AbstractGolemEntity<?, ?> golem) {
        List<LivingEntity> list = new java.util.ArrayList<>();
        var owner = golem.getOwner();
        var ownerUUID = golem.getOwnerUUID();
        double rangeSq = SUPPORT_RANGE * SUPPORT_RANGE;
        if (owner != null && owner.isAlive() && golem.distanceToSqr(owner) < rangeSq) {
            list.add(owner);
        }
        for (var other : golem.level().getEntitiesOfClass(LivingEntity.class,
                golem.getBoundingBox().inflate(SUPPORT_RANGE),
                e -> e != golem && e != owner && e.isAlive()
                        && golem.distanceToSqr(e) < rangeSq
                        && (e.isAlliedTo(golem) || isOwnedBy(e, ownerUUID)))) {
            list.add(other);
        }
        return list;
    }

    private static boolean isOwnedBy(LivingEntity entity, java.util.UUID ownerUUID) {
        if (ownerUUID == null) return false;
        if (entity instanceof net.minecraft.world.entity.TamableAnimal ta
                && ownerUUID.equals(ta.getOwnerUUID())) return true;
        if (entity instanceof dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity ge
                && ownerUUID.equals(ge.getOwnerUUID())) return true;
        // Check capabilities or other owner patterns via Entity.getOwnerUUID() if it exists
        try {
            var m = entity.getClass().getMethod("getOwnerUUID");
            if (m.getReturnType() == java.util.UUID.class) {
                return ownerUUID.equals(m.invoke(entity));
            }
        } catch (Exception ignored) {}
        return false;
    }
}
