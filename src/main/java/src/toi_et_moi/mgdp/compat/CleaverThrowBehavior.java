package src.toi_et_moi.mgdp.compat;

import dev.xkmc.mob_weapon_api.api.projectile.ProjectileWeaponUser;
import dev.xkmc.mob_weapon_api.example.behavior.ThrowableBehavior;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;

public class CleaverThrowBehavior extends ThrowableBehavior {

	@Override
	protected Projectile getProjectile(ProjectileWeaponUser user, ItemStack stack, LivingEntity target, int charge) {
		LivingEntity shooter = user.user();
		Arrow arrow = new Arrow(shooter.level(), shooter);
		arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
		arrow.setPierceLevel((byte) 127);
		arrow.setEffectsFromItem(ItemStack.EMPTY); // clear any status effects

		float weaponDmg = 0;
		var attrs = stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(
				net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
		for (var m : attrs) {
			if (m.getOperation() == AttributeModifier.Operation.ADDITION) {
				weaponDmg += (float) m.getAmount();
			}
		}
		float tierBonus = stack.getItem() instanceof TieredItem tiered
				? tiered.getTier().getAttackDamageBonus() : 0;
		float golemBase = (float) shooter.getAttributeValue(
				net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) - weaponDmg;
		if (golemBase < 0) golemBase = 0;
		float baseDmg = weaponDmg + golemBase + tierBonus;
		arrow.setBaseDamage(baseDmg);
		return arrow;
	}
}
