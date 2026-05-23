package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.entity.goals.GolemSwimMoveControl;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

@Mixin(GolemSwimMoveControl.class)
public abstract class GolemSwimMoveControlMixin extends MoveControl {

	@Shadow(remap = false)
	private AbstractGolemEntity<?, ?> golem;

	protected GolemSwimMoveControlMixin() { super(null); }

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void mgdp$flightTick(CallbackInfo ci) {
		if (!golem.getModifiers().containsKey(MGDPModifiers.FLIGHT.get())
				&& !golem.getModifiers().containsKey(MGDPModifiers.ROCKET_FLIGHT.get())) return;
		if (!golem.isMovable()) {
			golem.setDeltaMovement(Vec3.ZERO);
			ci.cancel();
			return;
		}
		flightTickImpl();
		ci.cancel();
	}

	private void flightTickImpl() {
		boolean moving = this.operation == MoveControl.Operation.MOVE_TO
				&& !this.golem.getNavigation().isDone();

		if (!moving) {
			Vec3 delta = this.golem.getDeltaMovement();
			this.golem.setDeltaMovement(delta.x * 0.7, delta.y * 0.7, delta.z * 0.7);
			return;
		}

		double dx = this.getWantedX() - this.golem.getX();
		double dy = this.getWantedY() - this.golem.getY();
		double dz = this.getWantedZ() - this.golem.getZ();
		double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (dist < 0.01) return;

		float yaw = (float) (Mth.atan2(dz, dx) * (180F / Math.PI)) - 90.0F;
		this.golem.setYRot(this.rotlerp(this.golem.getYRot(), yaw, 90.0F));
		this.golem.yBodyRot = this.golem.getYRot();

		float maxSpeed = (float) (this.speedModifier * this.golem.getAttributeValue(Attributes.MOVEMENT_SPEED));
		float speed = Mth.lerp(0.125F, this.golem.getSpeed(), maxSpeed);
		this.golem.setSpeed(speed);

		double factor = speed * 0.05;
		this.golem.setDeltaMovement(this.golem.getDeltaMovement().add(
				factor * dx, factor * dy, factor * dz));
	}
}
