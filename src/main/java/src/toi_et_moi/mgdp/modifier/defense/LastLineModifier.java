package src.toi_et_moi.mgdp.modifier.defense;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class LastLineModifier extends GolemModifier {

	private static final ResourceKey<Biome> FINAL_PLATEAU = ResourceKey.create(Registries.BIOME,
			new ResourceLocation("twilightforest", "final_plateau"));

	public LastLineModifier() {
		super(StatFilterType.HEALTH, 1);
	}

	@Override
    public void onAttacked(AbstractGolemEntity<?, ?> golem, LivingAttackEvent event, int level) {
		if (golem.level().isClientSide()) return;
		if (!ModList.get().isLoaded("twilightforest")) return;

		// Only activate in the Final Plateau biome
		var biome = golem.level().getBiome(golem.blockPosition());
		if (!biome.is(FINAL_PLATEAU)) return;

		// Get the direct damage source entity
		Entity source = event.getSource().getEntity();
		if (source == null) return;

		// Don't reduce damage from golems or Twilight Forest entities
		if (source instanceof AbstractGolemEntity) return;
		var typeId = ForgeRegistries.ENTITY_TYPES.getKey(source.getType());
		if (typeId != null && "twilightforest".equals(typeId.getNamespace())) return;

		// Completely negate damage from all other sources
        event.setCanceled(true);
	}
}
