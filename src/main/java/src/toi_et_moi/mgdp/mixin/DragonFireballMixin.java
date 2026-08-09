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
import src.toi_et_moi.mgdp.modifier.combat.DragonBreathModifier;

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

        // Smooth homing with target leading:
        // - acceleration is blended toward the aim point (limited turn per tick -> no sharp corners)
        // - the aim point leads the target's movement, so fast flyers like phantoms get hit
        Vec3 center = target.position().add(0, target.getBbHeight() * 0.5, 0);
        Vec3 toTarget = center.subtract(self.position());
        double dist = toTarget.length();
        if (dist < 0.01) return;

        // Lead: aim ahead of the target's velocity for roughly the flight time (clamped to avoid overleading)
        double leadTicks = net.minecraft.util.Mth.clamp(dist / 2.5, 0, 15);
        Vec3 aim = center.add(target.getDeltaMovement().scale(leadTicks));
        Vec3 desired = aim.subtract(self.position()).normalize().scale(0.5);

        Vec3 oldAccel = new Vec3(self.xPower, self.yPower, self.zPower);
        double oldLen = oldAccel.length();
        if (oldLen > 0.5) {
            oldAccel = oldAccel.scale(0.5 / oldLen);
        }
        // Far away: steer harder; close up: steer gentler so the fireball doesn't orbit the target
        double blend = net.minecraft.util.Mth.clamp(dist / 25.0, 0.04, 0.25);
        Vec3 newAccel = oldAccel.scale(1 - blend).add(desired.scale(blend));
        self.xPower = newAccel.x;
        self.yPower = newAccel.y;
        self.zPower = newAccel.z;
    }
}
