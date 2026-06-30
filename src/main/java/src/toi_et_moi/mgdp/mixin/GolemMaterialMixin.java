package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.content.config.GolemMaterial;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Collection;

@Mixin(value = GolemMaterial.class, remap = false)
public abstract class GolemMaterialMixin {

	@ModifyVariable(method = "collectModifiers(Ljava/util/Collection;Ljava/util/Collection;)Ljava/util/HashMap;", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private static Collection<GolemMaterial> mgdp$cleanNullModifiers(Collection<GolemMaterial> materials) {
		if (materials == null) return null;
		for (GolemMaterial mat : materials) {
			mat.modifiers().entrySet().removeIf(e -> e.getKey() == null);
		}
		return materials;
	}
}
