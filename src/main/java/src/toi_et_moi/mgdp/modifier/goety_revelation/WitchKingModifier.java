package src.toi_et_moi.mgdp.modifier.goety_revelation;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class WitchKingModifier extends GolemModifier {

    private static final int RANGE = 35;
    private static final int DURATION = 1200; // 60s
    private static final int AMPLIFIER = 3; // level 4
    private static List<MobEffect> POSITIVE_EFFECTS = null;
    private static List<MobEffect> NEGATIVE_EFFECTS = null;

    private static MobEffect goety(String name) {
        var effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("goety", name));
        return effect;
    }

    public WitchKingModifier() {
        super(StatFilterType.HEALTH, 1);
    }

    private static void ensureEffects() {
        if (POSITIVE_EFFECTS != null) return;
        POSITIVE_EFFECTS = new ArrayList<>();
        NEGATIVE_EFFECTS = new ArrayList<>();

        // Positive effects: Witch BUFF_POOL + Crone BUFF_POOL (excluding speed)
        for (var e : List.of(
                MobEffects.REGENERATION, MobEffects.DAMAGE_BOOST,
                MobEffects.FIRE_RESISTANCE, MobEffects.DAMAGE_RESISTANCE,
                MobEffects.ABSORPTION, MobEffects.DIG_SPEED,
                goety("repulsive"), goety("photosynthesis"), goety("iron_hide"),
                goety("rallying"), goety("shielding"), goety("deflective"),
                goety("leeching"), goety("climbing"), goety("radiance"),
                goety("corpse_eater"), goety("swirling")
        )) {
            if (e != null) POSITIVE_EFFECTS.add(e);
        }

        // Negative effects: Witch ATTACK + Crone ATTACK
        for (var e : List.of(
                MobEffects.POISON, MobEffects.WEAKNESS, MobEffects.WITHER,
                MobEffects.MOVEMENT_SLOWDOWN,
                goety("sapped"), goety("cursed"), goety("freezing"),
                goety("flammable"), goety("sun_allergy"), goety("arrowmantic"),
                goety("flimsy"), goety("gold_touched"), goety("acid_venom"),
                goety("ender_ground"), goety("nyctophobia")
        )) {
            if (e != null) NEGATIVE_EFFECTS.add(e);
        }
    }
@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		if (golem.tickCount % 20 != 0) return;

		ensureEffects();

		// Self and ally buffing (every 3 seconds)
		if (golem.tickCount % 60 == 0) {
			var box = golem.getBoundingBox().inflate(RANGE);
			for (var entity : golem.level().getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive())) {
				if (entity.distanceToSqr(golem) > RANGE * RANGE) continue;
				if (entity != golem && !entity.isAlliedTo(golem) && entity != golem.getOwner() && !isOwnedBy(golem, entity))
					continue;
				// Remove negative effects from allies
				for (var effect : new java.util.ArrayList<>(entity.getActiveEffects())) {
					if (!effect.getEffect().isBeneficial()) {
						entity.removeEffect(effect.getEffect());
					}
				}
				// Apply all positive effects
				for (var eff : POSITIVE_EFFECTS) {
					entity.addEffect(new MobEffectInstance(eff, DURATION, AMPLIFIER));
				}
			}
		}
	}

	@Override
	public void onHurtTarget(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		if (golem.level().isClientSide()) return;
		var target = event.getEntity();
	if (!golem.canAttack(target)) return;
		if (target == golem || target == golem.getOwner()) return;

		ensureEffects();

		// Apply all negative effects to the target
		for (var eff : NEGATIVE_EFFECTS) {
			target.addEffect(new MobEffectInstance(eff, DURATION, AMPLIFIER));
		}

		// Steal all positive effects from target
		for (var effect : new java.util.ArrayList<>(target.getActiveEffects())) {
			if (effect.getEffect().isBeneficial()) {
				target.removeEffect(effect.getEffect());
				golem.addEffect(new MobEffectInstance(effect.getEffect(), effect.getDuration(), effect.getAmplifier()));
			}
		}
	}

    private static boolean isOwnedBy(AbstractGolemEntity<?, ?> golem, LivingEntity entity) {
        var ownerUUID = golem.getOwnerUUID();
        if (ownerUUID == null) return false;
        if (entity instanceof net.minecraft.world.entity.TamableAnimal ta
                && ownerUUID.equals(ta.getOwnerUUID())) return true;
        if (entity instanceof AbstractGolemEntity ge
                && ownerUUID.equals(ge.getOwnerUUID())) return true;
        try {
            var m = entity.getClass().getMethod("getOwnerUUID");
            if (m.getReturnType() == java.util.UUID.class)
                return ownerUUID.equals(m.invoke(entity));
        } catch (Exception ignored) {}
        return false;
    }
}
