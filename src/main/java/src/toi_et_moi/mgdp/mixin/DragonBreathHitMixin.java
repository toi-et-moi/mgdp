package src.toi_et_moi.mgdp.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DragonFireball.class)
public class DragonBreathHitMixin {

    @Inject(method = "onHit", at = @At("HEAD"))
    private void mgdp$dragonHit(HitResult result, CallbackInfo ci) {
        DragonFireball self = (DragonFireball) (Object) this;
        if (!(self.level() instanceof ServerLevel sl)) return;

        var tag = self.getPersistentData();
        if (!tag.contains("mgdp_atk")) return;

        float atkDmg = tag.getFloat("mgdp_atk");
        float explosionDmg = tag.getFloat("mgdp_explosion");

        sl.explode(self, self.getX(), self.getY(), self.getZ(),
                explosionDmg, net.minecraft.world.level.Level.ExplosionInteraction.NONE);
    }
}
