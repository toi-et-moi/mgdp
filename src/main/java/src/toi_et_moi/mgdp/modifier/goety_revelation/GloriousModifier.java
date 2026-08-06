package src.toi_et_moi.mgdp.modifier.goety_revelation;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class GloriousModifier extends GolemModifier {

	private static final int DURATION = 200;

	public GloriousModifier() {
		super(StatFilterType.HEALTH, 1);
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		if (!net.minecraftforge.fml.ModList.get().isLoaded("goety")) return;
		if (golem.tickCount % 20 != 0) return;

		// 舍己为人 II (IV with 万众归一): damage taken heals allies in 60 blocks by the same amount
		var altruistic = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("goety", "altruistic"));
		if (altruistic != null) {
			boolean all = golem.getModifiers().containsKey(src.toi_et_moi.mgdp.init.MGDPModifiers.THE_GENESIS.get());
			golem.addEffect(new MobEffectInstance(altruistic, DURATION, all ? 3 : 1));
		}
	}

	@Override
	public void onHurtTarget(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		if (golem.level().isClientSide()) return;
		var target = event.getEntity();
		if (!golem.canAttack(target)) return;
		if (target == golem || target == golem.getOwner()) return;

		// +300% damage vs bosses
		if (target.getType().is(Tags.EntityTypes.BOSSES)) {
			event.setAmount(event.getAmount() * 4.0F);
		}
	}
}
