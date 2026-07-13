package src.toi_et_moi.mgdp.modifier.combat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import src.toi_et_moi.mgdp.Mgdp;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Mgdp.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RevelationArrowHandler {

    private static final List<ResourceLocation> NEGATIVE_EFFECTS = new ArrayList<>();
    private static boolean effectsLoaded = false;

    private static void loadEffects() {
        if (effectsLoaded) return;
        effectsLoaded = true;

        // Vanilla negative effects
        addEffect("minecraft", "slowness");
        addEffect("minecraft", "weakness");
        addEffect("minecraft", "poison");
        addEffect("minecraft", "wither");
        addEffect("minecraft", "hunger");
        addEffect("minecraft", "blindness");
        addEffect("minecraft", "levitation");
        addEffect("minecraft", "mining_fatigue");
        addEffect("minecraft", "unluck");
        addEffect("minecraft", "darkness");

        // Goety negative effects
        if (ModList.get().isLoaded("goety")) {
            addEffect("goety", "illague");
            addEffect("goety", "summon_down");
            addEffect("goety", "gold_touched");
            addEffect("goety", "busted");
            addEffect("goety", "burn_hex");
            addEffect("goety", "sapped");
            addEffect("goety", "wane");
            addEffect("goety", "cursed");
            addEffect("goety", "freezing");
            addEffect("goety", "doom");
            addEffect("goety", "acid_venom");
            addEffect("goety", "spasms");
            addEffect("goety", "wild_rage");
            addEffect("goety", "tangled");
            addEffect("goety", "void_touched");
            addEffect("goety", "impaired");
            addEffect("goety", "hysteria");
            addEffect("goety", "wounded");
            addEffect("goety", "crippled");
            addEffect("goety", "stunned");
            addEffect("goety", "soul_hunger");
            addEffect("goety", "ender_ground");
            addEffect("goety", "ender_flux");
            addEffect("goety", "nyctophobia");
            addEffect("goety", "arrowmantic");
            addEffect("goety", "plunge");
            addEffect("goety", "flimsy");
            addEffect("goety", "sense_loss");
        }
    }

    private static void addEffect(String namespace, String path) {
        var effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(namespace, path));
        if (effect != null) {
            NEGATIVE_EFFECTS.add(new ResourceLocation(namespace, path));
        }
    }

    @SubscribeEvent
    public static void onArrowHit(ProjectileImpactEvent event) {
        if (!ModList.get().isLoaded("goety_revelation")) return;

        var projectile = event.getProjectile();
        if (!(projectile.getOwner() instanceof dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity)) return;
        if (!(event.getRayTraceResult() instanceof net.minecraft.world.phys.EntityHitResult hit)) return;
        if (!(hit.getEntity() instanceof LivingEntity living)) return;
        if (projectile.getOwner() == hit.getEntity()) return;

        // Check if this is a DeathArrow shot by a golem using the Revelation Bow
        // All DeathArrows fired by golems from the Revelation Bow get random negative effects
        var type = ForgeRegistries.ENTITY_TYPES.getKey(projectile.getType());
        if (type == null || !type.getNamespace().equals("goety") || !type.getPath().equals("death_arrow"))
            return;

        // Double the arrow's base damage (the customArrow redirect might be overwritten by bow logic)
        if (projectile instanceof net.minecraft.world.entity.projectile.AbstractArrow aa) {
            // Check persistent data for Ascension Halo flag (set by SMCBowMixin)
            boolean halo = projectile.getPersistentData().getBoolean("mgdp_revelation_halo");
            double dmg = aa.getBaseDamage() * 2.5;
            if (halo) dmg *= 2.0;
            aa.setBaseDamage(dmg);
        }

        // Ignore projectile invulnerability frames
        living.invulnerableTime = 0;

        // Forced random negative effect (always applies)
        loadEffects();
        if (!NEGATIVE_EFFECTS.isEmpty()) {
            var rng = living.level().random;
            var chosen = NEGATIVE_EFFECTS.get(rng.nextInt(NEGATIVE_EFFECTS.size()));
            var effect = ForgeRegistries.MOB_EFFECTS.getValue(chosen);
            if (effect != null) {
                int level = rng.nextInt(4); // 0-3 representing levels 1-4
                living.addEffect(new MobEffectInstance(effect, 80, level));
            }
        }
    }
}
