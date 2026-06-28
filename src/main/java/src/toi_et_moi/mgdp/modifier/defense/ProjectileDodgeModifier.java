package src.toi_et_moi.mgdp.modifier.defense;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ProjectileDodgeModifier extends GolemModifier {

	public ProjectileDodgeModifier() {
		super(StatFilterType.HEALTH, 1);
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		if (golem.tickCount % 10 != 0) return;

		LivingEntity target = golem.getTarget();
		if (!(target instanceof Player player)) return;
		if (!player.hasLineOfSight(golem)) return;

		// Only dodge if the player is looking toward the golem
		Vec3 lookDir = player.getLookAngle();
		Vec3 toGolem = new Vec3(
				golem.getX() - player.getX(),
				golem.getEyeY() - player.getEyeY(),
				golem.getZ() - player.getZ()
		).normalize();
		if (lookDir.dot(toGolem) < 0.8) return;

		// Strafe perpendicular to the direction toward the player, random direction
		Vec3 toTarget = new Vec3(
				target.getX() - golem.getX(), 0, target.getZ() - golem.getZ()
		);
		if (toTarget.lengthSqr() < 0.01) return;

		boolean goRight = golem.getRandom().nextBoolean();
		Vec3 dodgeDir = goRight
				? new Vec3(-toTarget.z, 0, toTarget.x)
				: new Vec3(toTarget.z, 0, -toTarget.x);
		dodgeDir = dodgeDir.normalize();

		Vec3 newVel = golem.getDeltaMovement().add(dodgeDir.scale(1.2));
		if (Math.abs(newVel.x) < 100 && Math.abs(newVel.z) < 100) {
			golem.setDeltaMovement(newVel);
			golem.hasImpulse = true;
		}
	}

	@Override
	public List<MutableComponent> getDetail(int v) {
		return List.of(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GREEN));
	}
}
