package src.toi_et_moi.mgdp.compat;

import dev.xkmc.mob_weapon_api.api.projectile.ProjectileWeaponUser;
import dev.xkmc.mob_weapon_api.api.simple.IInstantWeaponBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class ExcaliburBehavior implements IInstantWeaponBehavior {

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

		try {
			var type = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation("smc", "sword_aura"));
			if (type == null) return COOLDOWN;

			Entity e = type.create(shooter.level());
			if (e == null) return COOLDOWN;
			e.setPos(shooter.getX(), shooter.getEyeY() - 0.3, shooter.getZ());

			if (e instanceof Projectile p) {
				// Set owner via reflection (SMC class is not obfuscated)
				try { e.getClass().getMethod("setOwner", LivingEntity.class).invoke(e, shooter); } catch (Exception ignored) {}

				double dx = target.getX() - shooter.getX();
				double dy = target.getEyeY() - shooter.getEyeY();
				double dz = target.getZ() - shooter.getZ();
				double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
				p.setDeltaMovement(dx / d * 2.0, dy / d * 2.0, dz / d * 2.0);
				p.hasImpulse = true;
			}

			// Apply Divine Judgement enchantment level
			try {
				var enchant = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("smc", "divine_judgement"));
				if (enchant != null) {
					int lv = stack.getEnchantmentLevel(enchant);
					if (lv > 0) e.getClass().getMethod("setDivineLevel", int.class).invoke(e, lv);
				}
			} catch (Exception ignored) {}

			e.getPersistentData().putUUID("mgdp_aura_owner", shooter.getUUID());
			shooter.level().addFreshEntity(e);
		} catch (Exception ex) {
			src.toi_et_moi.mgdp.Mgdp.LOGGER.warn("ExcaliburBehavior: failed", ex);
		}

		return COOLDOWN;
	}
}
