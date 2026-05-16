package src.toi_et_moi.mgdp.modifier;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

import java.util.List;

public class FlightPathNavigation extends GroundPathNavigation {

    private BlockPos targetPos = BlockPos.ZERO;
    private double speedModifier;
    private boolean hasTarget;
    private Path dummyPath;

    public FlightPathNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    public Path createPath(BlockPos pPos, int pDistance) {
        this.dummyPath = new Path(List.of(new Node(pPos.getX(), pPos.getY(), pPos.getZ())), pPos, true);
        return this.dummyPath;
    }

    @Override
    public Path getPath() {
        return this.dummyPath;
    }

    @Override
    public boolean moveTo(double x, double y, double z, double speedModifier) {
        this.targetPos = BlockPos.containing(x, y, z);
        this.speedModifier = speedModifier;
        this.hasTarget = true;
        this.mob.getMoveControl().setWantedPosition(x, y, z, speedModifier);
        return true;
    }

    @Override
    public boolean moveTo(Entity pEntity, double pSpeed) {
        return this.moveTo(pEntity.getX(), pEntity.getY(), pEntity.getZ(), pSpeed);
    }

    @Override
    public boolean moveTo(Path pPath, double pSpeed) {
        if (pPath != null && pPath.getTarget() != null) {
            BlockPos target = pPath.getTarget();
            return this.moveTo(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, pSpeed);
        }
        return false;
    }

    @Override
    public boolean isDone() {
        if (!this.hasTarget) return true;
        return this.mob.blockPosition().distSqr(this.targetPos) < 2.25;
    }

    @Override
    public void tick() {
        if (this.hasTarget && this.mob.getMoveControl().hasWanted()) {
            return;
        }
        if (this.hasTarget && !this.isDone()) {
            this.mob.getMoveControl().setWantedPosition(
                    this.targetPos.getX() + 0.5,
                    this.targetPos.getY() + 0.5,
                    this.targetPos.getZ() + 0.5,
                    this.speedModifier);
        }
    }

    @Override
    public void stop() {
        this.hasTarget = false;
        this.targetPos = BlockPos.ZERO;
        this.dummyPath = null;
        this.mob.getMoveControl().setWantedPosition(
                this.mob.getX(), this.mob.getY(), this.mob.getZ(), 0.0);
    }
}
