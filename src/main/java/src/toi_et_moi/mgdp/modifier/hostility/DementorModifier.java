package src.toi_et_moi.mgdp.modifier.hostility;

import dev.xkmc.l2damagetracker.contents.attack.CreateSourceEvent;
import dev.xkmc.l2damagetracker.contents.damage.DefaultDamageState;
import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;

public class DementorModifier extends GolemModifier {

	private static final double DAMAGE_REDUCTION_BASE = 20.0;

	public DementorModifier() {
		super(StatFilterType.HEALTH, 1);
	}

	@Override
	public void modifySource(AbstractGolemEntity<?, ?> golem, CreateSourceEvent event, int level) {
		if (event.getResult() != null) {
			event.enable(DefaultDamageState.BYPASS_ARMOR);
		}
	}

	@Override
	public void onHurt(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		float amount = event.getAmount();
		double def = DAMAGE_REDUCTION_BASE;
		float reduced = amount < def ? (float) (amount / def) : (float) (Math.log(amount) / Math.log(def));
		event.setAmount(Math.min(amount, reduced));
	}

	@Override
	public List<MutableComponent> getDetail(int v) {
		return List.of(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GREEN));
	}
}
