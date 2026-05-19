package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.entity.goals.GolemSwimMoveControl;
import dev.xkmc.modulargolems.content.entity.mode.GolemModes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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


@Mixin(AbstractGolemEntity.class)
public abstract class AbstractGolemEntityMixin extends Mob {

    protected AbstractGolemEntityMixin(EntityType<? extends Mob> type, Level level) {
        super(type, level);
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void mgdp$onAiStep(CallbackInfo ci) {
        AbstractGolemEntity<?, ?> golem = (AbstractGolemEntity<?, ?>) (Object) this;
        if (!golem.level().isClientSide && ModList.get().isLoaded("create")
                && golem.getMode() == GolemModes.STAND) {
            try {
                Class<?> cc = Class.forName("src.toi_et_moi.mgdp.compat.CreateCompat");
                cc.getMethod("tryDriveHandCrank", AbstractGolemEntity.class).invoke(null, golem);
            } catch (Exception ignored) {}
        }
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void mgdp$flightTravel(Vec3 travelVector, CallbackInfo ci) {
        AbstractGolemEntity<?, ?> golem = (AbstractGolemEntity<?, ?>) (Object) this;
        if (!golem.getModifiers().containsKey(MGDPModifiers.FLIGHT.get())) return;
        if (!golem.isMovable()) return;
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
        if (rider != null && MGDPKeyMappings.FLIGHT_SPRINT.isDown()) {
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
        if (golem.getModifiers().containsKey(MGDPModifiers.UNSTOPPABLE.get())) {
            golem.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0);
        }
        boolean hasFlight = golem.getModifiers().containsKey(MGDPModifiers.FLIGHT.get());
        boolean hasSpirit = golem.getModifiers().containsKey(MGDPModifiers.SPIRIT.get());
        ((EntityAccessor) this).setNoPhysics(hasFlight && hasSpirit);
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

    // --- Unstoppable modifier mixins ---

    private boolean mgdp$hasUnstoppable() {
        AbstractGolemEntity<?, ?> golem = (AbstractGolemEntity<?, ?>) (Object) this;
        return golem.getModifiers().containsKey(MGDPModifiers.UNSTOPPABLE.get());
    }

    @Inject(method = "isPushable", at = @At("RETURN"), cancellable = true)
    private void mgdp$unstoppableNoPush(CallbackInfoReturnable<Boolean> cir) {
        if (mgdp$hasUnstoppable()) cir.setReturnValue(false);
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void mgdp$unstoppableImmune(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (mgdp$hasUnstoppable() && source.getEntity() == null
                && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void mgdp$unstoppableLockSpeed(CallbackInfo ci) {
        if (!mgdp$hasUnstoppable()) return;
        var attr = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr == null || attr.getValue() >= attr.getBaseValue()) return;
        var toRemove = attr.getModifiers().stream()
                .filter(m -> m.getAmount() < 0)
                .map(AttributeModifier::getId)
                .toList();
        toRemove.forEach(attr::removeModifier);
    }

}
