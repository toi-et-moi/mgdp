package src.toi_et_moi.mgdp.compat.goety_revelation;
import net.minecraft.core.registries.BuiltInRegistries;

import dev.xkmc.mob_weapon_api.api.projectile.BowUseContext;
import dev.xkmc.mob_weapon_api.example.behavior.SimpleBowBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import src.toi_et_moi.mgdp.compat.SMCBowBehavior;

public class RevelationBowBehavior extends SimpleBowBehavior {

    @Override
    public int getStandardPullTime(BowUseContext ctx, ItemStack stack) {
        if (ctx.user() instanceof dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity golem
                && golem.getModifiers().containsKey(src.toi_et_moi.mgdp.init.MGDPModifiers.QUICK_STRIKE.get()))
            return 1;
        return 6;
    }

    @Override
    public int shootArrow(BowUseContext ctx, float power, ItemStack bowStack, InteractionHand hand) {
        var user = ctx.user();
        var level = user.level();

        // Create DeathArrow from Goety
        var type = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation("goety", "death_arrow"));
        if (type == null) return super.shootArrow(ctx, power, bowStack, hand);

        try {
            var ctor = Class.forName("com.Polarice3.Goety.common.entities.projectiles.DeathArrow")
                    .getConstructor(net.minecraft.world.entity.EntityType.class, net.minecraft.world.level.Level.class);
            AbstractArrow arrow = (AbstractArrow) ctor.newInstance(type, level);
            arrow.setPos(user.getX(), user.getEyeY() - 0.3, user.getZ());
            arrow.setOwner(user);
            // 2.5x base damage
            arrow.setBaseDamage(arrow.getBaseDamage() * 2.5);
            // Apply Power enchantment
            int powerLv = net.minecraft.world.item.enchantment.EnchantmentHelper
                    .getItemEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.POWER_ARROWS, bowStack);
            if (powerLv > 0) {
                arrow.setBaseDamage(arrow.getBaseDamage() + powerLv * 0.5 + 0.5);
            }

            // Check for Ascension Halo in curios
            boolean hasHalo = hasAscensionHalo(user);
            if (hasHalo) {
                arrow.getPersistentData().putBoolean("mgdp_revelation_halo", true);
            }

            // Aim toward target with prediction
            net.minecraft.world.entity.Mob mob = user instanceof net.minecraft.world.entity.Mob m ? m : null;
            LivingEntity tgt = mob != null ? mob.getTarget() : null;
            if (tgt != null) {
                Vec3 aim = SMCBowBehavior.predictPos(user, tgt, power * 12.0).subtract(user.getEyePosition(1.0F));
                double d = aim.length();
                if (d > 0.01) {
                    arrow.setDeltaMovement(aim.scale(power * 12.0F / d));
                    arrow.hasImpulse = true;
                }
            } else {
                arrow.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, power * 12.0F, 1.0F);
            }
            // Play apostle shoot sound
            try {
                var sound = net.minecraftforge.registries.ForgeRegistries.SOUND_EVENTS.getValue(
                        new ResourceLocation("goety", "apostle_shoot"));
                if (sound != null) {
                    level.playSound(null, user.getX(), user.getY(), user.getZ(),
                            sound, SoundSource.NEUTRAL, 1.0F, 1.0F);
                }
            } catch (Exception ignored) {}
            level.addFreshEntity(arrow);
            return user instanceof dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity g
                    && g.getModifiers().containsKey(src.toi_et_moi.mgdp.init.MGDPModifiers.QUICK_STRIKE.get()) ? 4 : 20;
        } catch (Exception e) {
            src.toi_et_moi.mgdp.Mgdp.LOGGER.warn("RevelationBow: death_arrow failed", e);
            return super.shootArrow(ctx, power, bowStack, hand);
        }
    }

    private boolean hasAscensionHalo(LivingEntity user) {
        if (!net.minecraftforge.fml.ModList.get().isLoaded("curios")) return false;
        if (!net.minecraftforge.fml.ModList.get().isLoaded("goety_revelation")) return false;
        try {
            var haloItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation("goety_revelation", "ascension_halo"));
            if (haloItem == null) return false;
            var helper = Class.forName("top.theillusivec4.curios.api.CuriosApi")
                    .getMethod("getCuriosHelper").invoke(null);
            var opt = helper.getClass().getMethod("getCuriosHandler", LivingEntity.class)
                    .invoke(helper, user);
            var handler = opt.getClass().getMethod("orElse", Object.class).invoke(opt, (Object) null);
            if (handler == null) return false;
            var curios = (java.util.Map<String, Object>) handler.getClass()
                    .getMethod("getCurios").invoke(handler);
            for (var entry : curios.entrySet()) {
                var stacksHandler = entry.getValue();
                var stacks = stacksHandler.getClass().getMethod("getStacks").invoke(stacksHandler);
                int slots = (int) stacks.getClass().getMethod("getSlots").invoke(stacks);
                for (int s = 0; s < slots; s++) {
                    var stack = (ItemStack) stacks.getClass()
                            .getMethod("getStackInSlot", int.class).invoke(stacks, s);
                    if (stack.is(haloItem)) return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }
}
