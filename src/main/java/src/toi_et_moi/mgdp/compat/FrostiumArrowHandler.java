package src.toi_et_moi.mgdp.compat;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;
import src.toi_et_moi.mgdp.Mgdp;

@Mod.EventBusSubscriber(modid = Mgdp.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FrostiumArrowHandler {

    private static final String KEY = "SMC_FROM_FROSTIUM_BOW";

    @SubscribeEvent
    public static void onArrowHit(ProjectileImpactEvent event) {
        if (!ModList.get().isLoaded("smc")) return;

        var projectile = event.getProjectile();
        if (!projectile.getPersistentData().contains(KEY)) return;
        if (!(projectile.getOwner() instanceof AbstractGolemEntity)) return;
        if (!(event.getRayTraceResult() instanceof net.minecraft.world.phys.EntityHitResult hit)) return;
        if (!(hit.getEntity() instanceof LivingEntity living)) return;
        if (projectile.getOwner() == hit.getEntity()) return;

        living.invulnerableTime = 0;
    }
}
