package src.toi_et_moi.mgdp.modifier.buff;

import dev.xkmc.modulargolems.content.modifier.base.PotionDefenseModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class PhantomModifier extends PotionDefenseModifier {

	public PhantomModifier() {
		super(2, () -> ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("youkaishomecoming", "phantom")));
	}
}
