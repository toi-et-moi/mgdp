package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.mob_weapon_api.example.goal.SmartRangedAttackGoal;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

/**
 * When the golem is flying, the parent mod's SmartRangedAttackGoal.strafing()
 * still runs and (very importantly) maintains `seeTime` — the "ticks spent
 * with line-of-sight to target" counter that the bow goal's tick() checks
 * before allowing an arrow release (seeTime > 0 → release, seeTime == 0 →
 * keep drawing). If we cancel the whole strafing() method, seeTime stays at
 * 0 forever and the golem draws the bow but never fires.
 *
 * So we cannot cancel strafing(). What we CAN safely no-op is its two
 * side-effects that fight our self-written flight AI:
 *
 *   - nav.moveTo(target, speed)         → pulls the golem toward the target
 *   - moveControl.strafe(forward, side) → side-steps via the parent move control
 *
 * Both still execute when the golem is on the ground; for a flying golem
 * our AbstractGolemEntityMixin.mgdp$aiFlightControl is the only thing
 * driving position, so these calls just confuse the picture (and the
 * GolemSwimMoveControl would actually apply wantedPos → super.tick() in
 * the airborne branch).
 *
 * doMelee() is left alone: it does not touch seeTime, and on a flying
 * golem our AI pushes it to weapon range before its canReachTarget check
 * could possibly fire doHurtTarget anyway.
 */
@Mixin(SmartRangedAttackGoal.class)
public abstract class RangedGoalFlightMixin {

    // remap is left at its default (true) for these two redirectors: their
    // target classes (PathNavigation, MoveControl) are vanilla Minecraft,
    // whose runtime class files still hold SRG names (m_xxxxx_). The
    // built-in refmap will translate the official name above into the
    // matching SRG descriptor before the bytecode is scanned. Adding
    // remap = false here (as we do for the slim_mapped_official parent mod)
    // would skip that translation and (0/1) succeed.
    @Redirect(method = "strafing",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/navigation/PathNavigation;moveTo(Lnet/minecraft/world/entity/Entity;D)Z"))
    private boolean mgdp$disableNavMoveToForFlight(PathNavigation nav, Entity target, double speed) {
        Mob mob = ((SmartRangedAccessor) this).getMob();
        if (mob instanceof AbstractGolemEntity<?, ?> golem
                && (golem.getModifiers().containsKey(MGDPModifiers.FLIGHT.get())
                    || golem.getModifiers().containsKey(MGDPModifiers.ROCKET_FLIGHT.get()))) {
            return true; // pretend success; nav path isn't useful for a flying golem
        }
        return nav.moveTo(target, speed);
    }

    @Redirect(method = "strafing",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/control/MoveControl;strafe(FF)V"))
    private void mgdp$disableStrafeForFlight(MoveControl ctrl, float forward, float sideways) {
        Mob mob = ((SmartRangedAccessor) this).getMob();
        if (mob instanceof AbstractGolemEntity<?, ?> golem
                && (golem.getModifiers().containsKey(MGDPModifiers.FLIGHT.get())
                    || golem.getModifiers().containsKey(MGDPModifiers.ROCKET_FLIGHT.get()))) {
            return; // do not let the parent move control side-step our flying golem
        }
        ctrl.strafe(forward, sideways);
    }
}
