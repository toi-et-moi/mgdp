package src.toi_et_moi.mgdp.modifier.hostility;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import src.toi_et_moi.mgdp.Mgdp;

@Mod.EventBusSubscriber(modid = Mgdp.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SelfDestructModifier extends GolemModifier {

	public SelfDestructModifier() {
		super(StatFilterType.HEALTH, 1);
	}

	/** Called from mixin when golem is removed (retrieval) */
	public static void explode(AbstractGolemEntity<?, ?> golem) {
		if (golem.level().isClientSide) return;
		if (!golem.getModifiers().containsKey(
				src.toi_et_moi.mgdp.init.MGDPModifiers.SELF_DESTRUCT.get())) return;
		doExplode(golem);
	}

	/** Called when golem dies */
	@SubscribeEvent
	public static void onDeath(LivingDeathEvent event) {
		if (!(event.getEntity() instanceof AbstractGolemEntity<?, ?> golem)) return;
		if (golem.level().isClientSide) return;
		if (!golem.getModifiers().containsKey(
				src.toi_et_moi.mgdp.init.MGDPModifiers.SELF_DESTRUCT.get())) return;
		doExplode(golem);
	}

	private static void doExplode(AbstractGolemEntity<?, ?> golem) {
		var data = golem.getPersistentData();
		if (data.getBoolean("mgdp_self_destructed")) return; // prevent double explosion
		data.putBoolean("mgdp_self_destructed", true);

		int power = Math.max(1, (int) (golem.getMaxHealth() / 10.0f));
		golem.level().explode(golem, golem.getX(), golem.getY(), golem.getZ(),
				power, src.toi_et_moi.mgdp.Config.destructionMode
						? Level.ExplosionInteraction.BLOCK
						: Level.ExplosionInteraction.NONE);
	}
}
