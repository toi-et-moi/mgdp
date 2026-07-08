package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(AbstractGolemEntity.class)
public abstract class SMCExcaliburMixin {

	@Unique private static EntityType<?> mgdp$auraType;
	@Unique private static net.minecraft.world.item.Item mgdp$excaliburItem;
	@Unique private static boolean mgdp$smcChecked;

	@Inject(method = "aiStep", at = @At("TAIL"))
	private void mgdp$excaliburAura(CallbackInfo ci) {
		AbstractGolemEntity<?, ?> golem = (AbstractGolemEntity<?, ?>) (Object) this;
		if (golem.level().isClientSide()) return;

		LivingEntity target = golem.getTarget();
		if (target == null || !target.isAlive()) return;

		if (!mgdp$smcChecked) {
			mgdp$smcChecked = true;
			mgdp$auraType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("smc", "sword_aura"));
			mgdp$excaliburItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation("smc", "excalibur"));
		}
		if (mgdp$auraType == null || mgdp$excaliburItem == null) return;
		if (!golem.getMainHandItem().is(mgdp$excaliburItem)) return;

		if (golem.tickCount % 15 != 0) return;

		try {
			Entity e = mgdp$auraType.create(golem.level());
			e.setPos(golem.getX(), golem.getEyeY() - 0.3, golem.getZ());
			if (e instanceof Projectile p) {
				try { e.getClass().getMethod("setOwner", LivingEntity.class).invoke(e, golem); } catch (Exception ignored) {}
				double dx = target.getX() - golem.getX();
				double dy = target.getEyeY() - golem.getEyeY();
				double dz = target.getZ() - golem.getZ();
				double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
				p.setDeltaMovement(dx / d * 2.0, dy / d * 2.0, dz / d * 2.0);
				p.hasImpulse = true;
			}
			// Apply Divine Judgement enchantment level
			try {
				var enchant = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("smc", "divine_judgement"));
				if (enchant != null) {
					int lv = golem.getMainHandItem().getEnchantmentLevel(enchant);
					if (lv > 0) e.getClass().getMethod("setDivineLevel", int.class).invoke(e, lv);
				}
			} catch (Exception ignored) {}
			e.getPersistentData().putUUID("mgdp_aura_owner", golem.getUUID());
			golem.level().addFreshEntity(e);
		} catch (Exception ex) {
			src.toi_et_moi.mgdp.Mgdp.LOGGER.warn("SMCExcalibur: failed", ex);
		}
	}

}
