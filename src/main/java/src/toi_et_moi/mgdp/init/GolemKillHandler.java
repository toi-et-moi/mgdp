package src.toi_et_moi.mgdp.init;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import src.toi_et_moi.mgdp.Mgdp;
import src.toi_et_moi.mgdp.advancement.InitTrigger;

@Mod.EventBusSubscriber(modid = Mgdp.MODID)
public class GolemKillHandler {

	@SubscribeEvent
	public static void onLivingDeath(LivingDeathEvent event) {
		if (!(event.getSource().getEntity() instanceof AbstractGolemEntity<?, ?> golem)) return;
		if (golem.level().isClientSide()) return;
		if (MGDPStats.GOLEM_KILLS == null) return;
		Player owner = golem.getOwner();
		if (owner instanceof ServerPlayer sp) {
			sp.awardStat(MGDPStats.GOLEM_KILLS);
			if (InitTrigger.GOLEM_KILL != null) {
				InitTrigger.GOLEM_KILL.trigger(sp);
			}
		}
	}
}
