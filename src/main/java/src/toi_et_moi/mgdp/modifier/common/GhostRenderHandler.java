package src.toi_et_moi.mgdp.modifier.common;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import src.toi_et_moi.mgdp.Mgdp;
import src.toi_et_moi.mgdp.init.MGDPModifiers;
import src.toi_et_moi.mgdp.mixin.EntityRendererAccessor;

import java.util.IdentityHashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Mgdp.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GhostRenderHandler {

    private static final Map<LivingEntityRenderer<?, ?>, Float> SAVED_SHADOWS = new IdentityHashMap<>();

    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        if (!(event.getEntity() instanceof AbstractGolemEntity<?, ?> golem)) return;
        if (!golem.getModifiers().containsKey(MGDPModifiers.GHOST.get())) return;

        LivingEntityRenderer<?, ?> renderer = event.getRenderer();
        SAVED_SHADOWS.put(renderer, ((EntityRendererAccessor) renderer).getShadowRadius());
        ((EntityRendererAccessor) renderer).setShadowRadius(0);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        Float saved = SAVED_SHADOWS.remove(event.getRenderer());
        if (saved != null) {
            ((EntityRendererAccessor) event.getRenderer()).setShadowRadius(saved);
        }
    }
}
