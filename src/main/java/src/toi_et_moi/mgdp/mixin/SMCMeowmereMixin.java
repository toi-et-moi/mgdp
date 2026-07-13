package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractGolemEntity.class)
public abstract class SMCMeowmereMixin {

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void mgdp$meowmereAura(CallbackInfo ci) {
        AbstractGolemEntity<?, ?> golem = (AbstractGolemEntity<?, ?>) (Object) this;
        if (golem.level().isClientSide()) return;
        if (!net.minecraftforge.fml.ModList.get().isLoaded("smc")) return;

        LivingEntity target = golem.getTarget();
        if (target == null || !target.isAlive()) return;

        var type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("smc", "meow_ball"));
        var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("smc", "meowmere"));
        if (type == null || item == null) return;
        if (!golem.getMainHandItem().is(item)) return;

        if (golem.tickCount % 15 != 0) return;

        try {
            Entity e = type.create(golem.level());
            if (e == null) return;
            e.setPos(golem.getX(), golem.getEyeY() - 0.3, golem.getZ());

            // Set Projectile.owner (vanilla method, call directly not reflection)
            if (e instanceof Projectile proj) {
                proj.setOwner(golem);
            }

            // Set MeowBall.owner via reflection (SMC class is NOT obfuscated)
            try { e.getClass().getMethod("setOwner", LivingEntity.class).invoke(e, golem); } catch (Exception ignored) {}

            // Set rendering item: try rainbow_cookie like SMC does, fallback to meowmere
            var renderItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation("smc", "rainbow_cookie"));
            if (e instanceof ThrowableItemProjectile tip) {
                tip.setItem(new ItemStack(renderItem != null ? renderItem : item));
            }

            // Set rotation and velocity directly (not reflection)
            e.setYRot(golem.getYHeadRot());
            e.setXRot(golem.getXRot());
            double dx = target.getX() - golem.getX();
            double dy = target.getEyeY() - golem.getEyeY();
            double dz = target.getZ() - golem.getZ();
            double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (d > 0.01) {
                e.setDeltaMovement(dx / d * 1.5, dy / d * 1.5, dz / d * 1.5);
            }

            e.getPersistentData().putUUID("mgdp_aura_owner", golem.getUUID());
            golem.level().addFreshEntity(e);
        } catch (Exception ex) {
            src.toi_et_moi.mgdp.Mgdp.LOGGER.warn("SMCMeowmere: failed", ex);
        }
    }
}
