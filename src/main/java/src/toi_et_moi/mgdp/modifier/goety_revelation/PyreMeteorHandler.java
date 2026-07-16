package src.toi_et_moi.mgdp.modifier.goety_revelation;

import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import src.toi_et_moi.mgdp.Mgdp;

@Mod.EventBusSubscriber(modid = Mgdp.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PyreMeteorHandler {

    @SubscribeEvent
    public static void onMeteorHit(ProjectileImpactEvent event) {
        if (!net.minecraftforge.fml.ModList.get().isLoaded("goety")) return;

        var projectile = event.getProjectile();
        var type = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(projectile.getType());
        if (type == null || !type.getNamespace().equals("goety") || !type.getPath().equals("nether_meteor"))
            return;

        // Our tagged meteor: if owner is gone (golem recycled), cancel impact (no damage/explosion)
        var data = projectile.getPersistentData();
        if (!data.hasUUID("mgdp_meteor_owner")) return;

        var owner = projectile.getOwner();
        if (owner == null || !owner.isAlive()) {
            event.setCanceled(true);
        }
    }
}
