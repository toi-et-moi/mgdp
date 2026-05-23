package src.toi_et_moi.mgdp.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import src.toi_et_moi.mgdp.modifier.DragonBreathModifier;

import java.util.UUID;

@Mixin(AbstractHurtingProjectile.class)
public abstract class DragonFireballMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void mgdp$homeTowardTarget(CallbackInfo ci) {
        if (!((Object) this instanceof DragonFireball self)) return;
        if (self.level().isClientSide) return;

        var tag = self.getPersistentData();
        if (!tag.hasUUID("mgdp_target")) return;

        LivingEntity target = null;
        try {
            UUID uuid = tag.getUUID("mgdp_target");
            if (self.level() instanceof ServerLevel sl) {
                target = (LivingEntity) sl.getEntity(uuid);
            }
        } catch (Exception e) { return; }

        if (target == null || !target.isAlive()) {
            tag.remove("mgdp_target");
            return;
        }

        Vec3 dir = target.position().subtract(self.position()).normalize();
        self.xPower = dir.x * 0.3;
        self.yPower = dir.y * 0.3;
        self.zPower = dir.z * 0.3;
    }
}
