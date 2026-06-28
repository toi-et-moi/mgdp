package src.toi_et_moi.mgdp.item;

import dev.xkmc.modulargolems.content.item.upgrade.UpgradeItem;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import dev.xkmc.modulargolems.content.modifier.base.ModifierInstance;

import java.util.List;
import java.util.function.Supplier;

public class ConditionalUpgradeItem extends UpgradeItem {

	private final Supplier<GolemModifier> modifier;
	private final int level;

	public ConditionalUpgradeItem(Properties props, Supplier<GolemModifier> mod, int level, boolean display) {
		super(props, display);
		this.modifier = mod;
		this.level = level;
	}

	@Override
	public List<ModifierInstance> get() {
		GolemModifier mod = modifier.get();
		if (mod == null) return List.of();
		return List.of(new ModifierInstance(mod, level));
	}
}
