package src.toi_et_moi.mgdp.modifier.buff;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class FrostBurstModifier extends GolemModifier {

	private static final int DURATION = 200; // 10 seconds

	public FrostBurstModifier() {
		super(StatFilterType.HEALTH, 3);
	}

	@Override
	public void onHurtTarget(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		apply(golem, event.getEntity(), level);
	}

	@Override
	public void onHurt(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		if (event.getSource().getEntity() instanceof LivingEntity attacker) {
			apply(golem, attacker, level);
		}
	}

	private void apply(AbstractGolemEntity<?, ?> golem, LivingEntity target, int level) {
		if (target.level().isClientSide()) return;
		if (target == golem || target == golem.getOwner()) return;
		if (target instanceof Player p && (p.isCreative() || p.isSpectator())) return;
		if (golem.isAlliedTo(target)) return;

		MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("smc", "frost_burst"));
		if (effect == null) return;

		MobEffectInstance existing = target.getEffect(effect);
		if (existing != null) {
			int newAmp = existing.getAmplifier() + level;
			target.addEffect(new MobEffectInstance(effect, existing.getDuration(), newAmp, false, false));
		} else {
			target.addEffect(new MobEffectInstance(effect, DURATION, level - 1, false, false));
		}
	}
}
