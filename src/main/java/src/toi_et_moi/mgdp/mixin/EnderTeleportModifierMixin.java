package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.compat.materials.l2complements.EnderTeleportModifier;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.init.data.MGConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

/**
 * Flying-aware EnderTeleport.
 * <p>
 * The parent mod's `teleport(entity, x, y, z)` walks downward from the
 * requested Y until it finds a solid block and forces the golem to land
 * on top of it (randomTeleport(..., solidY + 1, ...)). So even if we pass
 * y + 50 as the destination, the golem is dragged back to the floor on
 * every ender-port.
 * <p>
 * We cannot just raise Y — we have to bypass that "find ground" loop
 * entirely for flying golems. The fix is: for a flying golem, pick a
 * random point inside a cube centred on the golem's own position
 * (so it can never wander off and forget its flight) and only call
 * randomTeleport if that point is in free space (feet + head both
 * air). randomTeleport itself does the actual move; we just re-fire
 * the EnderEntity event, the game-event and the enderman teleport
 * sound around it so the visuals are identical to a normal ender-port.
 */
@Mixin(EnderTeleportModifier.class)
public abstract class EnderTeleportModifierMixin {

    /** Radius of the random cube, in blocks, around the golem's own pos. */
    @Unique
    private static final int MGDP$FLIGHT_TELE_RADIUS = 8;

    /** Max attempts to find a free-space landing point, mirroring the parent. */
    @Unique
    private static final int MGDP$FLIGHT_TELE_ATTEMPTS = 16;

    @Invoker(value = "teleport", remap = false)
    public static boolean mgdp$callTeleport(AbstractGolemEntity<?, ?> entity, double pX, double pY, double pZ) {
        throw new AssertionError();
    }

    /**
     * @author mgdp
     * @reason The parent's implementation teleports the golem to target's
     *         XZ then snaps it to whatever solid block sits below that
     *         point. For a flying golem this pulls it out of the air on
     *         every hit. We branch on the golem's flight modifier and
     *         either keep the parent's ground-snap behaviour (for ground
     *         golems) or reroute to a cube-centred random point that
     *         respects the flight state.
     *
     *         The active-teleport path (called from EnderTeleportGoal.tick
     *         when the nav is stuck or the target is more than 6 blocks
     *         away) is the "closing distance" path: it picks a random
     *         point inside a cube around the TARGET so the golem ends
     *         up close to the fight. mgdp$flightTeleportRandom (used by
     *         the passive onAttacked path) instead picks a point around
     *         the golem itself so it can disengage.
     */
    @Overwrite(remap = false)
    public static boolean teleportTowards(AbstractGolemEntity<?, ?> entity, Entity pTarget) {
        boolean flying = entity.getModifiers().containsKey(MGDPModifiers.FLIGHT.get())
                || entity.getModifiers().containsKey(MGDPModifiers.ROCKET_FLIGHT.get());
        if (!flying) {
            // Ground golem: keep the parent's exact behaviour — teleport to
            // target's XZ and snap down to the surface.
            return mgdp$callTeleport(entity, pTarget.getX(), pTarget.getY(), pTarget.getZ());
        }
        // Flying golem (ACTIVE — closing distance): cube around the target.
        return mgdp$flightTeleportTowardsTarget(entity, pTarget);
    }

    /**
     * @author mgdp
     * @reason onAttacked() (the path that fires when a golem is hit) calls
     *         teleport(entity) with no args, NOT teleportTowards(entity, target).
     *         That private no-arg method picks a random y inside
     *         `[-teleportRadius, +teleportRadius]` from the golem's own
     *         position, then routes through teleport(entity, x, y, z) which
     *         in turn walks downward to the nearest solid block and calls
     *         LivingEntity.randomTeleport(..., true) — both of which force
     *         a flying golem to the floor. We overwrite so the flying branch
     *         routes through mgdp$flightTeleportRandom (cube random +
     *         free-space check + setPos + animation), and the ground
     *         branch keeps the parent's original behaviour.
     */
    @Overwrite(remap = false)
    private static boolean teleport(AbstractGolemEntity<?, ?> entity) {
        if (entity.level().isClientSide() || !entity.isAlive()) return false;
        boolean flying = entity.getModifiers().containsKey(MGDPModifiers.FLIGHT.get())
                || entity.getModifiers().containsKey(MGDPModifiers.ROCKET_FLIGHT.get());
        if (!flying) {
            // Keep the parent's exact y-range (target_y ± teleportRadius) and
            // delegate to the public teleport(entity, x, y, z) for the
            // ground-snap + randomTeleport flow. We do this by calling the
            // public teleport method through our @Invoker helper.
            int r = MGConfig.COMMON.teleportRadius.get();
            double d0 = entity.getX() + (entity.getRandom().nextDouble() - 0.5D) * r * 2.0D;
            double d1 = entity.getY() + (double) (entity.getRandom().nextInt(r * 2) - r);
            double d2 = entity.getZ() + (entity.getRandom().nextDouble() - 0.5D) * r * 2.0D;
            return mgdp$callTeleport(entity, d0, d1, d2);
        }
        // Flying golem: bypass the find-ground + randomTeleport entirely.
        return mgdp$flightTeleportRandom(entity);
    }

    /**
     * Active teleport for flying golems: EnderTeleportGoal.tick() calls
     * teleportTowards(entity, target) when the nav is stuck or the target
     * is far away, with the goal of closing the distance. For a flying
     * golem we want to land in a cube around the TARGET, not around the
     * golem — otherwise the active teleport would push the golem *away*
     * from the fight half the time. The cube is the same size (radius 8)
     * and uses the same free-space + EnderEntity + setPos pipeline as the
     * passive path.
     */
    @Unique
    private static boolean mgdp$flightTeleportTowardsTarget(AbstractGolemEntity<?, ?> entity, Entity target) {
        if (entity.level().isClientSide() || !entity.isAlive()) return false;
        var random = entity.getRandom();
        double r = MGDP$FLIGHT_TELE_RADIUS;
        double cx = target.getX();
        double cy = target.getY();
        double cz = target.getZ();
        for (int i = 0; i < MGDP$FLIGHT_TELE_ATTEMPTS; i++) {
            double x = cx + (random.nextDouble() - 0.5D) * r * 2.0D;
            double y = cy + (random.nextDouble() - 0.5D) * r * 2.0D;
            double z = cz + (random.nextDouble() - 0.5D) * r * 2.0D;
            if (mgdp$tryFlightTeleport(entity, x, y, z)) return true;
        }
        return false;
    }

    @Unique
    private static boolean mgdp$flightTeleportRandom(AbstractGolemEntity<?, ?> entity) {
        if (entity.level().isClientSide() || !entity.isAlive()) return false;
        var random = entity.getRandom();
        double cx = entity.getX();
        double cy = entity.getY();
        double cz = entity.getZ();
        double r = MGDP$FLIGHT_TELE_RADIUS;
        for (int i = 0; i < MGDP$FLIGHT_TELE_ATTEMPTS; i++) {
            double x = cx + (random.nextDouble() - 0.5D) * r * 2.0D;
            double y = cy + (random.nextDouble() - 0.5D) * r * 2.0D;
            double z = cz + (random.nextDouble() - 0.5D) * r * 2.0D;
            if (mgdp$tryFlightTeleport(entity, x, y, z)) return true;
        }
        return false;
    }

    @Unique
    private static boolean mgdp$tryFlightTeleport(AbstractGolemEntity<?, ?> entity, double x, double y, double z) {
        // Need two free cells: feet + head, otherwise the golem clips into a wall.
        BlockPos feet = BlockPos.containing(x, y, z);
        BlockPos head = feet.above();
        if (entity.level().getBlockState(feet).blocksMotion()) return false;
        if (entity.level().getBlockState(head).blocksMotion()) return false;

        // Ender entity event — vanilla enderman also fires this; mod compat
        // and other mods' "no teleport here" hooks expect it.
        EntityTeleportEvent.EnderEntity event = ForgeEventFactory.onEnderTeleport(entity, x, y, z);
        if (event.isCanceled()) return false;

        Vec3 before = entity.position();
        // Bypass randomTeleport entirely: it has a `while (y > min && !solid)
        // y--` loop that snaps us to the nearest ground regardless of what y
        // we passed in. The `pOnlySpawnable` flag only controls the
        // ender-anim event, NOT the ground snap. So for a flying golem we
        // setPos directly and replay the bits randomTeleport would have
        // done around the move (animation packet, navigation reset, sound).
        entity.setPos(event.getTargetX(), event.getTargetY(), event.getTargetZ());
        // 46 = EnderTeleport packet id (broadcastEntityEvent). Other clients
        // play the enderman-style swirl particles from this.
        entity.level().broadcastEntityEvent(entity, (byte) 46);
        // AbstractGolemEntity → GuardedEntity → PathfinderMob → Mob, so the
        // nav always exists — just call stop() unconditionally. This is the
        // bit vanilla randomTeleport does after a successful teleport.
        entity.getNavigation().stop();
        entity.level().gameEvent(GameEvent.TELEPORT, before, GameEvent.Context.of(entity));
        if (!entity.isSilent()) {
            entity.level().playSound(null, before.x, before.y, before.z,
                    SoundEvents.ENDERMAN_TELEPORT, entity.getSoundSource(), 1.0F, 1.0F);
            entity.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        }
        return true;
    }
}
