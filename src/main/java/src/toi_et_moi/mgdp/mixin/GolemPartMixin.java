package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.content.item.golem.GolemPart;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.HashMap;

@Mixin(value = GolemPart.class, remap = false)
public abstract class GolemPartMixin {

	@ModifyVariable(method = "parseMaterial(Lnet/minecraft/resources/ResourceLocation;)Ldev/xkmc/modulargolems/content/config/GolemMaterial;", at = @At("STORE"), ordinal = 3)
	private HashMap<GolemModifier, Integer> mgdp$removeNullModifierKeys(HashMap<GolemModifier, Integer> map) {
		if (map != null) {
			map.entrySet().removeIf(e -> e.getKey() == null);
		}
		return map;
	}
}
