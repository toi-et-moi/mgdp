package src.toi_et_moi.mgdp.modifier.common;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import src.toi_et_moi.mgdp.Mgdp;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

@Mod.EventBusSubscriber(modid = Mgdp.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MoonShadowHandler {

	private static final int RADIUS = 64;

	@SubscribeEvent
	public static void onMobSpawn(MobSpawnEvent.FinalizeSpawn event) {
		// Only block natural spawns (not spawners, spawn eggs, commands, etc.)
		if (event.getSpawnType() != MobSpawnType.NATURAL) return;

		Mob mob = event.getEntity();
		Level level = (Level) mob.level();

		// Check if any nearby golem has the upgrade
		boolean hasProtection = false;
		boolean isNight = level.isNight();
		int spawnDist = RADIUS;

		for (AbstractGolemEntity<?, ?> golem : level.getEntitiesOfClass(
				AbstractGolemEntity.class,
				mob.getBoundingBox().inflate(spawnDist),
				g -> g.isAlive() &&
					(g.getModifiers().containsKey(MGDPModifiers.MOON_SHADOW.get()) ||
					 g.getModifiers().containsKey(MGDPModifiers.TIME_AXIS.get()))
		)) {
			if (golem.distanceToSqr(mob) < spawnDist * spawnDist) {
				// 月影 only works at night; 时轴 works always
				if (golem.getModifiers().containsKey(MGDPModifiers.TIME_AXIS.get()) ||
					(golem.getModifiers().containsKey(MGDPModifiers.MOON_SHADOW.get()) && isNight)) {
					hasProtection = true;
					break;
				}
			}
		}

		if (hasProtection) {
			event.setSpawnCancelled(true);
		}
	}
}
