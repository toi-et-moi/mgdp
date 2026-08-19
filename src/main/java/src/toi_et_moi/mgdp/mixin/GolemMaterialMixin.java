package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.content.config.GolemMaterial;
import dev.xkmc.modulargolems.content.item.upgrade.IUpgradeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(value = GolemMaterial.class, remap = false)
public abstract class GolemMaterialMixin {

	// Golems saved with compat materials/upgrades whose mod was removed deserialize those
	// entries as null: drop them before the modifier/attribute rebuild instead of crashing on load.
	private static List<GolemMaterial> cleanNullMaterials(Collection<GolemMaterial> materials) {
		if (materials == null) return null;
		var cleaned = new ArrayList<GolemMaterial>();
		for (GolemMaterial mat : materials) {
			if (mat != null) {
				mat.modifiers().entrySet().removeIf(e -> e.getKey() == null);
				cleaned.add(mat);
			}
		}
		return cleaned;
	}

	private static List<IUpgradeItem> cleanNullUpgrades(Collection<IUpgradeItem> upgrades) {
		if (upgrades == null) return null;
		var cleaned = new ArrayList<IUpgradeItem>();
		for (IUpgradeItem up : upgrades) {
			if (up != null) cleaned.add(up);
		}
		return cleaned;
	}

	@ModifyVariable(method = "collectModifiers(Ljava/util/Collection;Ljava/util/Collection;)Ljava/util/HashMap;", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private static Collection<GolemMaterial> mgdp$cleanNullMaterials(Collection<GolemMaterial> materials) {
		return cleanNullMaterials(materials);
	}

	@ModifyVariable(method = "collectModifiers(Ljava/util/Collection;Ljava/util/Collection;)Ljava/util/HashMap;", at = @At("HEAD"), ordinal = 1, argsOnly = true)
	private static Collection<IUpgradeItem> mgdp$cleanNullUpgrades(Collection<IUpgradeItem> upgrades) {
		return cleanNullUpgrades(upgrades);
	}

	@ModifyVariable(method = "collectAttributes(Ljava/util/List;Ljava/util/List;)Ljava/util/Map;", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private static List<GolemMaterial> mgdp$cleanNullMaterialsAttr(List<GolemMaterial> materials) {
		return cleanNullMaterials(materials);
	}

	@ModifyVariable(method = "collectAttributes(Ljava/util/List;Ljava/util/List;)Ljava/util/Map;", at = @At("HEAD"), ordinal = 1, argsOnly = true)
	private static List<IUpgradeItem> mgdp$cleanNullUpgradesAttr(List<IUpgradeItem> upgrades) {
		return cleanNullUpgrades(upgrades);
	}
}
