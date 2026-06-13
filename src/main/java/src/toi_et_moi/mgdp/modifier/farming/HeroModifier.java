package src.toi_et_moi.mgdp.modifier.farming;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import src.toi_et_moi.mgdp.Mgdp;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

import java.util.List;

import static dev.xkmc.modulargolems.init.registrate.GolemModifiers.EMERALD;

@Mod.EventBusSubscriber(modid = Mgdp.MODID)
public class HeroModifier extends GolemModifier {

	private static final TagKey<EntityType<?>> RAIDERS = TagKey.create(
			Registries.ENTITY_TYPE, new ResourceLocation("minecraft", "raiders"));

	public HeroModifier() {
		super(StatFilterType.MASS, 1);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onLivingDeath(LivingDeathEvent event) {
		if (!(event.getSource().getEntity() instanceof AbstractGolemEntity<?, ?> golem)) return;
		if (golem.level().isClientSide()) return;
		if (!golem.getModifiers().containsKey(MGDPModifiers.HERO.get())) return;

		LivingEntity killed = event.getEntity();
		if (!killed.getType().is(RAIDERS)) return;

		Player owner = golem.getOwner();
		if (owner == null) return;

		int emeraldLevel = golem.getModifiers().getOrDefault(EMERALD.get(), 0);
		int addDuration = 1200 * (1 + emeraldLevel);

		MobEffectInstance existing = owner.getEffect(MobEffects.HERO_OF_THE_VILLAGE);
		if (existing != null) {
			owner.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE,
					existing.getDuration() + addDuration, existing.getAmplifier(),
					existing.isAmbient(), existing.isVisible(), existing.showIcon()));
		} else {
			owner.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, addDuration, 0));
		}
	}

	@Override
	public List<MutableComponent> getDetail(int v) {
		return List.of(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GREEN));
	}
}
