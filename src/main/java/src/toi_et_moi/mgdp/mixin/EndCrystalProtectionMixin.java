package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

@Mixin(EndCrystal.class)
public class EndCrystalProtectionMixin {

	private static final int CRYSTAL_MAX_HITS = 13;

	@Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
	private void mgdp$protectCrystal(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		EndCrystal crystal = (EndCrystal) (Object) this;

		// Check if any nearby golem has the "始狱终" (EndOfBeginning) upgrade
		boolean hasProtector = false;
		for (AbstractGolemEntity<?, ?> golem : crystal.level().getEntitiesOfClass(
				AbstractGolemEntity.class,
				crystal.getBoundingBox().inflate(32),
				g -> g.getModifiers().containsKey(MGDPModifiers.END_OF_BEGINNING.get())
		)) {
			if (golem.isAlive() && golem.distanceToSqr(crystal) < 32 * 32) {
				hasProtector = true;
				break;
			}
		}

		if (!hasProtector) return;

		// Track hits on this crystal
		int hits = crystal.getPersistentData().getInt("mgdp_crystal_hits") + 1;
		crystal.getPersistentData().putInt("mgdp_crystal_hits", hits);

		if (hits >= CRYSTAL_MAX_HITS) {
			return; // Allow destruction on 13th hit
		}

		cir.setReturnValue(false);
	}
}
