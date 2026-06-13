package src.toi_et_moi.mgdp.modifier.hostility;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import src.toi_et_moi.mgdp.Mgdp;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

import java.util.List;

@Mod.EventBusSubscriber(modid = Mgdp.MODID)
public class GrenadeModifier extends GolemModifier {

	public GrenadeModifier() {
		super(StatFilterType.ATTACK, 5);
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onLivingHurt(LivingHurtEvent event) {
		if (!(event.getSource().getDirectEntity() instanceof ShulkerBullet bullet)) return;
		if (!(bullet.getOwner() instanceof AbstractGolemEntity<?, ?> golem)) return;
		if (golem.level().isClientSide()) return;
		int level = golem.getModifiers().getOrDefault(MGDPModifiers.GRENADE.get(), 0);
		if (level <= 0) return;

		float atk = (float) golem.getAttributeValue(Attributes.ATTACK_DAMAGE);
		float total = event.getAmount() + atk + atk * 0.2F * level;

		event.setCanceled(true);
		event.getEntity().hurt(golem.damageSources().mobAttack(golem), total);

		golem.level().explode(golem,
				event.getEntity().getX(), event.getEntity().getY(0.5), event.getEntity().getZ(),
				2.0F + level * 2.0F, Level.ExplosionInteraction.NONE);
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		if (golem.tickCount % 30 != 0) return;

		LivingEntity target = golem.getTarget();
		if (target == null || !target.isAlive() || !golem.canAttack(target)) return;
		if (!golem.getSensing().hasLineOfSight(target)) return;
		if (golem.distanceToSqr(target) > 1600) return;

		ShulkerBullet bullet = new ShulkerBullet(golem.level(), golem, target, Direction.Axis.Y);
		bullet.setPos(golem.getX(), golem.getY(0.5), golem.getZ());
		bullet.getPersistentData().putBoolean("mgdp_grenade", true);
		golem.level().addFreshEntity(bullet);
	}

	@Override
    public void onAttacked(AbstractGolemEntity<?, ?> entity, LivingAttackEvent event, int level) {
		if (level <= 0) return;
		if (event.getSource().is(DamageTypeTags.IS_EXPLOSION)) {
			event.setCanceled(true);
		}
	}

	@Override
	public List<MutableComponent> getDetail(int v) {
		return List.of(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GREEN));
	}
}
