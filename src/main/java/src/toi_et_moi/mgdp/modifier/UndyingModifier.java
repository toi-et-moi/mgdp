package src.toi_et_moi.mgdp.modifier;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import src.toi_et_moi.mgdp.Mgdp;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

import java.util.List;

@Mod.EventBusSubscriber(modid = Mgdp.MODID)
public class UndyingModifier extends GolemModifier {

	public UndyingModifier() {
		super(StatFilterType.HEALTH, 1);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onLivingDeath(LivingDeathEvent event) {
		if (!(event.getEntity() instanceof AbstractGolemEntity<?, ?> golem)) return;
		if (golem.level().isClientSide()) return;
		if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
		if (!golem.getModifiers().containsKey(MGDPModifiers.UNDYING.get())) return;

		event.setCanceled(true);
		golem.setHealth(golem.getMaxHealth());

		if (golem.level() instanceof ServerLevel sl) {
			sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
					golem.getX(), golem.getY() + 1, golem.getZ(),
					30, 0.5, 0.5, 0.5, 0.5);
			sl.playSound(null, golem.blockPosition(), SoundEvents.TOTEM_USE,
					SoundSource.PLAYERS, 1.0F, 1.0F);
		}
	}

	@Override
	public List<MutableComponent> getDetail(int v) {
		return List.of(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GREEN));
	}
}
