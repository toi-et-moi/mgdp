package src.toi_et_moi.mgdp.modifier.buff;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class CroneModifier extends GolemModifier {

    private static final int BUFF_INTERVAL = 80;
    private static final int ATTACK_INTERVAL = 60;
    private static final int SUPPORT_INTERVAL = 80;
    private static final double POTION_SPEED = 1.5;
    private static final double SUPPORT_RANGE = 16.0;

    private static MobEffect goety(String name) {
        var effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("goety", name));
        return effect != null ? effect : MobEffects.GLOWING; // fallback if Goety not loaded
    }

    private static final List<MobEffectInstance> ATTACK_POTIONS = List.of(
            new MobEffectInstance(goety("sapped"), 200, 1),
            new MobEffectInstance(goety("cursed"), 150, 0),
            new MobEffectInstance(goety("freezing"), 150, 0),
            new MobEffectInstance(goety("flammable"), 200, 2),
            new MobEffectInstance(goety("sun_allergy"), 200, 2),
            new MobEffectInstance(goety("arrowmantic"), 200, 0),
            new MobEffectInstance(goety("flimsy"), 200, 1),
            new MobEffectInstance(goety("gold_touched"), 200, 4),
            new MobEffectInstance(goety("acid_venom"), 200, 1),
            new MobEffectInstance(goety("ender_ground"), 200, 1),
            new MobEffectInstance(goety("nyctophobia"), 200, 1)
    );

    private static final List<MobEffectInstance> BUFF_POOL = List.of(
            new MobEffectInstance(goety("repulsive"), 1000, 1),
            new MobEffectInstance(goety("photosynthesis"), 1000, 1),
            new MobEffectInstance(goety("iron_hide"), 1000, 2),
            new MobEffectInstance(goety("rallying"), 1000, 0),
            new MobEffectInstance(goety("shielding"), 1000, 0),
            new MobEffectInstance(goety("deflective"), 1000, 0),
            new MobEffectInstance(goety("leeching"), 1000, 1),
            new MobEffectInstance(goety("climbing"), 1000, 0),
            new MobEffectInstance(goety("radiance"), 1000, 0),
            new MobEffectInstance(goety("corpse_eater"), 1000, 1)
    );

    private static final List<MobEffectInstance> ATTACK_LIST;
    private static final List<MobEffectInstance> BUFF_LIST;

    static {
        ATTACK_LIST = List.copyOf(ATTACK_POTIONS);
        BUFF_LIST = List.copyOf(BUFF_POOL);
    }

    public CroneModifier() {
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

        // Self-buff
        if (tick % BUFF_INTERVAL == 0) {
            var buff = BUFF_LIST.get(golem.level().random.nextInt(BUFF_LIST.size()));
            golem.addEffect(new MobEffectInstance(buff));
        }

        // Attack potion
        if (tick % ATTACK_INTERVAL == 0) {
            var target = golem.getTarget();
            if (target != null && target.isAlive() && golem.distanceToSqr(target) < 900) {
                var eff1 = ATTACK_LIST.get(golem.level().random.nextInt(ATTACK_LIST.size()));
                var eff2 = ATTACK_LIST.get(golem.level().random.nextInt(ATTACK_LIST.size()));
                throwPotion(golem, target.position().add(0, target.getBbHeight() * 0.5, 0),
                        List.of(eff1, eff2), level);
            }
        }

        // Support potion
        if (tick % SUPPORT_INTERVAL == 0) {
            var owner = golem.getOwner();
            if (owner != null && owner.isAlive() && golem.distanceToSqr(owner) < SUPPORT_RANGE * SUPPORT_RANGE) {
                var b1 = BUFF_LIST.get(golem.level().random.nextInt(BUFF_LIST.size()));
                var b2 = BUFF_LIST.get(golem.level().random.nextInt(BUFF_LIST.size()));
                throwPotion(golem, owner.position().add(0, owner.getBbHeight() * 0.5, 0),
                        List.of(b1, b2), level);
            }
        }
    }

    private void throwPotion(AbstractGolemEntity<?, ?> golem, Vec3 target, List<MobEffectInstance> effects, int level) {
        ItemStack potionStack = new ItemStack(level >= 2 ? Items.LINGERING_POTION : Items.SPLASH_POTION);
        PotionUtils.setCustomEffects(potionStack, effects);

        ThrownPotion potion = new ThrownPotion(golem.level(), golem);
        potion.setItem(potionStack);
        potion.setPos(golem.getX(), golem.getEyeY() - 0.3, golem.getZ());

        Vec3 dir = target.subtract(potion.position()).normalize();
        potion.setDeltaMovement(dir.scale(POTION_SPEED));

        golem.level().addFreshEntity(potion);
    }
}
