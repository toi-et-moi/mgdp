package src.toi_et_moi.mgdp.modifier.buff;

import dev.xkmc.modulargolems.content.core.GolemStatType;
import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.modifier.base.AttributeGolemModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.RegistryManager;

public class ManaOverloadModifier extends AttributeGolemModifier {

	public ManaOverloadModifier() {
		super(1, new AttrEntry(
				() -> {
					if (!ModList.get().isLoaded("golemmagicka")) return null;
					var reg = RegistryManager.ACTIVE.getRegistry(new ResourceLocation("modulargolems", "stat_type"));
					if (reg == null) return null;
					var stat = reg.getValue(new ResourceLocation("golemmagicka", "mana_regen"));
					return stat instanceof GolemStatType s ? s : null;
				},
				() -> 9.99)
		);
	}
}
