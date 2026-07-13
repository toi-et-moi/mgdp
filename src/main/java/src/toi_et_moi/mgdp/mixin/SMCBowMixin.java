package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.mob_weapon_api.api.projectile.BowUseContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(dev.xkmc.mob_weapon_api.example.behavior.SimpleBowBehavior.class)
public class SMCBowMixin {

    private static boolean mgdp$revelationBow; // flag for Revelation Bow velocity doubling

    @Redirect(method = "shootArrow", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BowItem;customArrow(Lnet/minecraft/world/entity/projectile/AbstractArrow;)Lnet/minecraft/world/entity/projectile/AbstractArrow;"), remap = false)
    private AbstractArrow mgdp$redirectCustomArrow(BowItem bow, AbstractArrow arrow) {
        AbstractArrow result = bow.customArrow(arrow);

        // Check for Revelation Bow (Goety: Revelation closed-source addon)
        if (net.minecraftforge.fml.ModList.get().isLoaded("goety_revelation")) {
            ResourceLocation rid = ForgeRegistries.ITEMS.getKey(bow);
            if (rid != null && rid.getNamespace().equals("goety_revelation") && rid.getPath().equals("bow_of_revelation")) {
                try {
                    var type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("goety", "death_arrow"));
                    if (type != null) {
                        var ctor = Class.forName("com.Polarice3.Goety.common.entities.projectiles.DeathArrow")
                                .getConstructor(net.minecraft.world.entity.EntityType.class, net.minecraft.world.level.Level.class);
                        AbstractArrow da = (AbstractArrow) ctor.newInstance(type, arrow.level());
                        da.setPos(arrow.position());
                        da.setDeltaMovement(arrow.getDeltaMovement());
                        da.setOwner(arrow.getOwner());
                        // Copy base damage from source arrow (actual 2x applied in ProjectileImpactEvent handler)
                        da.setBaseDamage(arrow.getBaseDamage());
                        mgdp$revelationBow = true;

                        // Check for Ascension Halo in curios (same pattern as DisarmModifier)
                        if (arrow.getOwner() instanceof LivingEntity owner &&
                                net.minecraftforge.fml.ModList.get().isLoaded("curios")) {
                            try {
                                var haloItem = ForgeRegistries.ITEMS.getValue(
                                        new ResourceLocation("goety_revelation", "ascension_halo"));
                                if (haloItem != null) {
                                    var helper = Class.forName("top.theillusivec4.curios.api.CuriosApi")
                                            .getMethod("getCuriosHelper").invoke(null);
                                    var opt = helper.getClass().getMethod("getCuriosHandler", LivingEntity.class)
                                            .invoke(helper, owner);
                                    var handler = opt.getClass().getMethod("orElse", Object.class)
                                            .invoke(opt, (Object) null);
                                    if (handler != null) {
                                        var curios = (java.util.Map<String, Object>) handler.getClass()
                                                .getMethod("getCurios").invoke(handler);
                                        for (var entry : curios.entrySet()) {
                                            var stacksHandler = entry.getValue();
                                            var stacks = stacksHandler.getClass().getMethod("getStacks").invoke(stacksHandler);
                                            var slots = (int) stacks.getClass().getMethod("getSlots").invoke(stacks);
                                            for (int s = 0; s < slots; s++) {
                                                var stack = (net.minecraft.world.item.ItemStack) stacks.getClass()
                                                        .getMethod("getStackInSlot", int.class).invoke(stacks, s);
                                                if (stack.is(haloItem)) {
                                                    da.getPersistentData().putBoolean("mgdp_revelation_halo", true);
                                                    break;
                                                }
                                            }
                                            if (da.getPersistentData().contains("mgdp_revelation_halo")) break;
                                        }
                                    }
                                }
                            } catch (Exception ignored) {}
                        }
                        return da;
                    }
                } catch (Exception e) {
                    src.toi_et_moi.mgdp.Mgdp.LOGGER.warn("SMCBow: revelation death_arrow failed", e);
                }
            }
        }

        if (!net.minecraftforge.fml.ModList.get().isLoaded("smc")) return result;

        ResourceLocation id = ForgeRegistries.ITEMS.getKey(bow);
        if (id == null || !id.getNamespace().equals("smc")) return result;

        if (id.getPath().equals("rainbow_bow")) {
            try {
                var type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("smc", "rainbow_arrow"));
                if (type == null) return result;
                var ctor = Class.forName("com.starmeow.smc.entities.projectiles.RainbowArrow")
                        .getConstructor(net.minecraft.world.entity.EntityType.class, net.minecraft.world.level.Level.class);
                AbstractArrow ra = (AbstractArrow) ctor.newInstance(type, arrow.level());
                ra.setPos(arrow.position());
                ra.setDeltaMovement(arrow.getDeltaMovement());
                ra.setOwner(arrow.getOwner());
                try {
                    var f = ra.getClass().getDeclaredField("canSummon");
                    f.setAccessible(true);
                    f.setBoolean(ra, true);
                } catch (Exception ignored) {}
                return ra;
            } catch (Exception e) {
                src.toi_et_moi.mgdp.Mgdp.LOGGER.warn("SMCBow: rainbow_arrow failed", e);
            }
        }

        return result;
    }

    // Make Frost Arrows work with Infinity enchant on Frostium/Perfrostite bow
    @Redirect(method = "shootArrow", at = @At(value = "INVOKE", target = "Ldev/xkmc/mob_weapon_api/api/projectile/BowUseContext;hasInfiniteArrow(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"), remap = false)
    private boolean mgdp$frostInfinity(BowUseContext ctx, ItemStack bowStack, ItemStack arrowStack) {
        if (ctx.hasInfiniteArrow(bowStack, arrowStack)) return true;

        if (!net.minecraftforge.fml.ModList.get().isLoaded("smc")) return false;
        ResourceLocation bowId = ForgeRegistries.ITEMS.getKey(bowStack.getItem());
        if (bowId == null || !bowId.getNamespace().equals("smc")) return false;
        if (!bowId.getPath().equals("frostium_bow") && !bowId.getPath().equals("perfrostite_bow")) return false;
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, bowStack) <= 0) return false;

        ResourceLocation frostArrowId = new ResourceLocation("smc", "frost_arrow");
        return arrowStack.is(ForgeRegistries.ITEMS.getValue(frostArrowId));
    }

    @Inject(method = "shootArrow", at = @At("RETURN"), remap = false)
    private void mgdp$frostiumExtraArrows(BowUseContext ctx, float power, ItemStack bowStack, InteractionHand hand, CallbackInfoReturnable<Integer> cir) {
        if (!net.minecraftforge.fml.ModList.get().isLoaded("smc")) return;

        ResourceLocation id = ForgeRegistries.ITEMS.getKey(bowStack.getItem());
        if (id == null || !id.getNamespace().equals("smc")) return;
        if (!id.getPath().equals("frostium_bow") && !id.getPath().equals("perfrostite_bow")) return;

        var user = ctx.user();
        var level = user.level();
        if (level.isClientSide()) return;

        ItemStack ammo = ctx.getPreferredProjectile(bowStack);
        if (ammo.isEmpty()) {
            ammo = new ItemStack(Items.ARROW);
        }

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

            extra.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, power * 3.0F - 0.3F * s, 1.0F);

            level.addFreshEntity(extra);
        }
    }

    // Double arrow velocity for Revelation Bow
    @Redirect(method = "shootArrow", at = @At(value = "INVOKE", target = "Ldev/xkmc/mob_weapon_api/api/projectile/ProjectileWeaponUseContext$AimResult;shoot(Lnet/minecraft/world/entity/projectile/Projectile;F)V"), remap = false)
    private void mgdp$revelationSpeed(dev.xkmc.mob_weapon_api.api.projectile.ProjectileWeaponUseContext.AimResult result,
                                      Projectile arrow, float velocity) {
        if (mgdp$revelationBow) {
            mgdp$revelationBow = false;
            result.shoot(arrow, velocity * 2.0F);
        } else {
            result.shoot(arrow, velocity);
        }
    }
}
