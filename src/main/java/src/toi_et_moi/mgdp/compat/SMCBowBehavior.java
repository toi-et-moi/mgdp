package src.toi_et_moi.mgdp.compat;
import net.minecraft.core.registries.BuiltInRegistries;

import dev.xkmc.mob_weapon_api.api.projectile.BowUseContext;
import dev.xkmc.mob_weapon_api.example.behavior.SimpleBowBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class SMCBowBehavior extends SimpleBowBehavior {

    /** Predict target position at chest height based on target movement velocity and arrow speed */
    public static Vec3 predictPos(LivingEntity user, LivingEntity target, double arrowSpeed) {
        Vec3 pos = target.getEyePosition(); // aim at eye level (~chest height)
        Vec3 vel = target.getDeltaMovement();
        if (vel.lengthSqr() > 0.01 && user.distanceTo(target) >= 0.1) {
            double t = user.distanceTo(target) / arrowSpeed;
            pos = pos.add(vel.scale(t));
        }
        return pos;
    }

    private final Supplier<Boolean> active;

    public SMCBowBehavior(Supplier<Boolean> active) {
        this.active = active;
    }

    private static boolean hasQuickStrike(LivingEntity user) {
        return user instanceof dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity golem
                && golem.getModifiers().containsKey(src.toi_et_moi.mgdp.init.MGDPModifiers.QUICK_STRIKE.get());
    }

    @Override
    public int getStandardPullTime(BowUseContext ctx, ItemStack stack) {
        boolean qs = hasQuickStrike(ctx.user());
        ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id != null && id.getNamespace().equals("smc") && id.getPath().equals("rainbow_bow"))
            return qs ? 1 : 16;
        if (qs) return 1;
        return super.getStandardPullTime(ctx, stack);
    }

    @Override
    public int shootArrow(BowUseContext ctx, float power, ItemStack bowStack, InteractionHand hand) {
        if (!active.get() || !net.minecraftforge.fml.ModList.get().isLoaded("smc"))
            return super.shootArrow(ctx, power, bowStack, hand);

        ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(bowStack.getItem());
        if (id == null || !id.getNamespace().equals("smc"))
            return super.shootArrow(ctx, power, bowStack, hand);

        String path = id.getPath();

        if (path.equals("rainbow_bow")) {
            return shootRainbow(ctx, power, bowStack, hand);
        } else if (path.equals("frostium_bow") || path.equals("perfrostite_bow")) {
            return shootFrostium(ctx, power, bowStack, hand);
        }

        return super.shootArrow(ctx, power, bowStack, hand);
    }

    private int shootRainbow(BowUseContext ctx, float power, ItemStack bowStack, InteractionHand hand) {
        var user = ctx.user();
        var level = user.level();

        // Create RainbowArrow
        var type = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation("smc", "rainbow_arrow"));
        if (type == null) return super.shootArrow(ctx, power, bowStack, hand);

        try {
            var ctor = Class.forName("com.starmeow.smc.entities.projectiles.RainbowArrow")
                    .getConstructor(net.minecraft.world.entity.EntityType.class, net.minecraft.world.level.Level.class);
            AbstractArrow arrow = (AbstractArrow) ctor.newInstance(type, level);
            arrow.setPos(user.getX(), user.getEyeY() - 0.3, user.getZ());
            arrow.setOwner(user);
            try {
                var f = arrow.getClass().getDeclaredField("canSummon");
                f.setAccessible(true);
                f.setBoolean(arrow, true);
            } catch (Exception ignored) {}

            // Apply Power enchantment
            int powerLv = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, bowStack);
            if (powerLv > 0) {
                arrow.setBaseDamage(arrow.getBaseDamage() + powerLv * 0.5 + 0.5);
            }

            // Aim toward target with prediction
            net.minecraft.world.entity.Mob mob = user instanceof net.minecraft.world.entity.Mob m ? m : null;
            LivingEntity tgt = mob != null ? mob.getTarget() : null;
            if (tgt != null) {
                Vec3 aim = predictPos(user, tgt, power * 8.0).subtract(user.getEyePosition(1.0F));
                double d = aim.length();
                if (d > 0.01) {
                    arrow.setDeltaMovement(aim.scale(power * 8.0F / d));
                    arrow.hasImpulse = true;
                }
            } else {
                arrow.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, power * 8.0F, 1.0F);
            }
            level.addFreshEntity(arrow);
            return hasQuickStrike(user) ? 4 : 12;
        } catch (Exception e) {
            src.toi_et_moi.mgdp.Mgdp.LOGGER.warn("SMCBow: rainbow failed", e);
            return super.shootArrow(ctx, power, bowStack, hand);
        }
    }

    private int shootFrostium(BowUseContext ctx, float power, ItemStack bowStack, InteractionHand hand) {
        var user = ctx.user();
        var level = user.level();

        // Main arrow uses super.shootArrow (correct AI aim + ammo/enchantment handling)
        int cooldown = super.shootArrow(ctx, power, bowStack, hand);

        // Get aim direction from golem's target (with prediction) for extra arrows
        net.minecraft.world.entity.Mob mob = user instanceof net.minecraft.world.entity.Mob m ? m : null;
        LivingEntity target = mob != null ? mob.getTarget() : null;
        Vec3 aimDir;
        if (target != null) {
            aimDir = predictPos(user, target, power * 3.0).subtract(user.getEyePosition(1.0F));
        } else {
            aimDir = user.getLookAngle();
        }
        double d = aimDir.length();
        if (d < 0.01) return cooldown;
        aimDir = aimDir.normalize();

        // Get ammo type for extra arrows
        ItemStack ammo = ctx.getPreferredProjectile(bowStack);
        if (ammo.isEmpty()) ammo = new ItemStack(Items.ARROW);

        // Fire 2 extra arrows with same direction as main arrow, velocity spread
        int powerLv = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, bowStack);
        for (int s = 1; s <= 2; s++) {
            AbstractArrow extra;
            if (ammo.getItem() instanceof ArrowItem ai) {
                extra = ai.createArrow(level, ammo, user);
            } else {
                extra = new Arrow(level, user);
            }
            extra.getPersistentData().putBoolean("SMC_FROM_FROSTIUM_BOW", true);
            extra.pickup = AbstractArrow.Pickup.DISALLOWED;
            extra.setPos(user.getX(), user.getEyeY() - 0.3, user.getZ());
            if (powerLv > 0) {
                extra.setBaseDamage(extra.getBaseDamage() + powerLv * 0.5 + 0.5);
            }
            float speed = power * 3.0F - 0.3F * s;
            extra.setDeltaMovement(aimDir.scale(speed));
            extra.hasImpulse = true;
            level.addFreshEntity(extra);
        }

        return cooldown;
    }
}
