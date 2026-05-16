package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.entity.goals.GolemSwimMoveControl;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import src.toi_et_moi.mgdp.modifier.FlightPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;
import net.minecraft.client.Minecraft;
import src.toi_et_moi.mgdp.init.MGDPKeyMappings;
import src.toi_et_moi.mgdp.init.MGDPModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import src.toi_et_moi.mgdp.compat.CreateCompat;

@Mixin(AbstractGolemEntity.class)
public abstract class AbstractGolemEntityMixin extends Mob {

    protected AbstractGolemEntityMixin(EntityType<? extends Mob> type, Level level) {
        super(type, level);
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void mgdp$onAiStep(CallbackInfo ci) {
        AbstractGolemEntity<?, ?> golem = (AbstractGolemEntity<?, ?>) (Object) this;
        if (!golem.level().isClientSide && ModList.get().isLoaded("create")) {
            CreateCompat.tryDriveHandCrank(golem);
        }
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void mgdp$flightTravel(Vec3 travelVector, CallbackInfo ci) {
        AbstractGolemEntity<?, ?> golem = (AbstractGolemEntity<?, ?>) (Object) this;
        if (!golem.getModifiers().containsKey(MGDPModifiers.FLIGHT.get())) return;
        if (!golem.isEffectiveAi() && !golem.isControlledByLocalInstance()) return;

        LivingEntity rider = golem.isControlledByLocalInstance() && golem.isVehicle()
                ? (LivingEntity) golem.getControllingPassenger() : null;

        if (rider != null) {
            Minecraft mc = Minecraft.getInstance();
            float yya = 0;
            if (mc.options.keyJump.isDown()) yya = 1.0F;
            else if (MGDPKeyMappings.FLIGHT_DESCEND.isDown()) yya = -1.0F;
            travelVector = new Vec3(rider.xxa, yya, rider.zza);
        }

        float friction = 0.08F;
        if (rider != null && Minecraft.getInstance().options.keySprint.isDown()) {
            friction = 0.16F;
        }
        golem.moveRelative(friction, travelVector);
        golem.move(MoverType.SELF, golem.getDeltaMovement());
        golem.setDeltaMovement(golem.getDeltaMovement().multiply(0.91, 0.91, 0.91));
        ci.cancel();
    }

    @Inject(method = "updateAttributes", at = @At("TAIL"), remap = false)
    private void mgdp$setFlightMoveControl(CallbackInfo ci) {
        AbstractGolemEntity<?, ?> golem = (AbstractGolemEntity<?, ?>) (Object) this;
        if (golem.getModifiers().containsKey(MGDPModifiers.FLIGHT.get())) {
            this.moveControl = new GolemSwimMoveControl(golem);
            this.navigation = new FlightPathNavigation(golem, golem.level());
        }
    }

    @Inject(method = "canSwim", at = @At("RETURN"), cancellable = true, remap = false)
    private void mgdp$canSwimWithFlight(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            AbstractGolemEntity<?, ?> golem = (AbstractGolemEntity<?, ?>) (Object) this;
            if (golem.getModifiers().containsKey(MGDPModifiers.FLIGHT.get())) {
                cir.setReturnValue(true);
            }
        }
    }
}
