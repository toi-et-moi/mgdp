package src.toi_et_moi.mgdp.modifier.special;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.item.upgrade.IUpgradeItem;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.List;

public class VersatilityModifier extends GolemModifier {

	private static final TagKey<Item> DARK_TAG = TagKey.create(Registries.ITEM,
			new ResourceLocation("mgdp", "dark_upgrades"));
	private static final int MAX_FREE = 5;

	public VersatilityModifier() {
		super(StatFilterType.HEALTH, 1);
	}

	@Override
	public int addSlot(List<IUpgradeItem> list, int lv) {
		int count = 0;
		for (IUpgradeItem upgrade : list) {
			if (upgrade instanceof Item item && item.builtInRegistryHolder().is(DARK_TAG)) {
				count++;
			}
		}
		return Math.min(count, MAX_FREE);
	}
}
