package src.toi_et_moi.mgdp.compat;

import dev.xkmc.mob_weapon_api.api.projectile.ProjectileWeaponUser;
import dev.xkmc.mob_weapon_api.api.simple.IInstantWeaponBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;

public class MeowmereBehavior implements IInstantWeaponBehavior {

	private static final double RANGE = 35.0;
	private static final int COOLDOWN = 15;

	@Override
	public double range(ProjectileWeaponUser user, ItemStack stack) {
		return RANGE;
	}

	@Override
	public int trigger(ProjectileWeaponUser user, ItemStack stack, LivingEntity target) {
		LivingEntity shooter = user.user();
		if (shooter == null || shooter.level().isClientSide()) return COOLDOWN;
		if (!net.minecraftforge.fml.ModList.get().isLoaded("smc")) return COOLDOWN;

		try {
			var type = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation("smc", "meow_ball"));
			if (type == null) return COOLDOWN;

			Entity e = type.create(shooter.level());
			if (e == null) return COOLDOWN;
			e.setPos(shooter.getX(), shooter.getEyeY() - 0.3, shooter.getZ());

			if (e instanceof Projectile proj) {
				proj.setOwner(shooter);
			}

			// Set MeowBall owner via reflection (SMC class is not obfuscated)
			try { e.getClass().getMethod("setOwner", LivingEntity.class).invoke(e, shooter); } catch (Exception ignored) {}

			var renderItem = BuiltInRegistries.ITEM.get(new ResourceLocation("smc", "rainbow_cookie"));
			var meowmere = BuiltInRegistries.ITEM.get(new ResourceLocation("smc", "meowmere"));
			if (e instanceof ThrowableItemProjectile tip) {
				tip.setItem(new ItemStack(renderItem != null ? renderItem : meowmere));
			}

			e.setYRot(shooter.getYHeadRot());
			e.setXRot(shooter.getXRot());
			double dx = target.getX() - shooter.getX();
			double dy = target.getEyeY() - shooter.getEyeY();
			double dz = target.getZ() - shooter.getZ();
			double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
			if (d > 0.01) {
				e.setDeltaMovement(dx / d * 1.5, dy / d * 1.5, dz / d * 1.5);
			}

			e.getPersistentData().putUUID("mgdp_aura_owner", shooter.getUUID());
			shooter.level().addFreshEntity(e);
		} catch (Exception ex) {
			src.toi_et_moi.mgdp.Mgdp.LOGGER.warn("MeowmereBehavior: failed", ex);
		}

		return COOLDOWN;
	}
}
