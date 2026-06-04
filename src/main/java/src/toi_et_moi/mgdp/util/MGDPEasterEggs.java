package src.toi_et_moi.mgdp.util;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import src.toi_et_moi.mgdp.Mgdp;

@Mod.EventBusSubscriber(modid = Mgdp.MODID)
public final class MGDPEasterEggs {

	private static int flintGuard = 0;

	private MGDPEasterEggs() {}

	@SubscribeEvent
	public static void onBalloonSlayer(LivingHurtEvent event) {
		if (!(event.getSource().getDirectEntity() instanceof AbstractGolemEntity<?, ?> golem)) return;
		if (!golem.getMainHandItem().is(Mgdp.SIMPLE_GOLEM_SPEAR.get())) return;

		String name = event.getEntity().getName().getString().toLowerCase();
		if (name.contains("balloon") || name.contains("气球")) {
			event.setAmount(event.getAmount() * 661F);
		}
	}

	@SubscribeEvent
	public static void onFlintExplosion(LivingAttackEvent event) {
		if (flintGuard > 0) return;
		if (!(event.getSource().getDirectEntity() instanceof AbstractGolemEntity<?, ?> golem)) return;
		if (!golem.getMainHandItem().is(Items.FLINT_AND_STEEL)
				&& !golem.getOffhandItem().is(Items.FLINT_AND_STEEL)) return;
		if (event.getEntity().level().isClientSide()) return;

		event.getEntity().setSecondsOnFire(5);
		flintGuard++;
		event.getEntity().level().explode(golem, event.getEntity().getX(), event.getEntity().getY(),
				event.getEntity().getZ(), 2.0F, Level.ExplosionInteraction.NONE);
		flintGuard--;
	}
}
