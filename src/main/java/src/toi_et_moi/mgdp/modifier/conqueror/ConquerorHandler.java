package src.toi_et_moi.mgdp.modifier.conqueror;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import src.toi_et_moi.mgdp.Mgdp;
import src.toi_et_moi.mgdp.init.IConquerorData;

@Mod.EventBusSubscriber(modid = Mgdp.MODID)
public class ConquerorHandler {

	@SubscribeEvent
	public static void onLivingDeath(LivingDeathEvent event) {
		if (!(event.getSource().getEntity() instanceof AbstractGolemEntity<?, ?> golem)) return;
		LivingEntity victim = event.getEntity();
		double hp = victim.getMaxHealth();
		if (hp <= 0) return;

		if (golem instanceof IConquerorData data) {
			data.mgdp$addVetHp(hp);
		}
	}
}
