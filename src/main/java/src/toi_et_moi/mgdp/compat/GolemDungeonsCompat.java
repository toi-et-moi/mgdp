package src.toi_et_moi.mgdp.compat;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Soft dependency wrapper for GolemDungeons mod.
 * Uses reflection so MGDP compiles without GolemDungeons as a dependency.
 */
public class GolemDungeonsCompat {

    private static final String GD_MODID = "golemdungeons";
    private static boolean loaded;
    private static Object spawnRegistry;
    private static Method getEntryMethod;
    private static Method summonMethod;

    static {
        loaded = ModList.get().isLoaded(GD_MODID);
        if (loaded) {
            try {
                Class<?> gdClass = Class.forName("dev.xkmc.golemdungeons.init.GolemDungeons");
                Field spawnField = gdClass.getField("SPAWN");
                spawnRegistry = spawnField.get(null);

                getEntryMethod = spawnRegistry.getClass().getMethod("getEntry", ResourceLocation.class);

                Class<?> spawnConfigClass = Class.forName("dev.xkmc.golemdungeons.content.config.SpawnConfig");
                summonMethod = spawnConfigClass.getMethod("summon", ServerLevel.class);

            } catch (Exception e) {
                loaded = false;
            }
        }
    }

    public static boolean isLoaded() {
        return loaded;
    }

    /**
     * Summon a golem from a GolemDungeons faction SpawnConfig as a player-owned golem.
     *
     * @param configId the SpawnConfig ResourceLocation
     * @param level    the server level
     * @param player   the player to set as owner
     * @return the summoned golem, or null if failed
     */
    @Nullable
    public static LivingEntity summonFactionGolem(ResourceLocation configId, ServerLevel level, Player player) {
        if (!loaded) return null;

        try {
            Object entry = getEntryMethod.invoke(spawnRegistry, configId);
            if (entry == null) return null;

            LivingEntity summoned = (LivingEntity) summonMethod.invoke(entry, level);
            if (summoned == null) return null;

            // If the returned entity is a hostile non-golem mount (e.g. Ravager),
            // discard it and return the golem rider instead. Passive mounts (Horse, etc.) are kept.
            if (summoned instanceof Enemy && !summoned.getPassengers().isEmpty()) {
                var firstRider = summoned.getPassengers().get(0);
                if (firstRider instanceof AbstractGolemEntity<?, ?> age) {
                    age.setOwnerUUID(player.getUUID());
                    firstRider.stopRiding();
                    summoned.discard();
                    return age;
                }
            }

            // Normal case: returned entity is the golem itself or a golem mount
            setOwnerRecursive(summoned, player.getUUID());
            return summoned;
        } catch (Exception e) {
            return null;
        }
    }

    private static void setOwnerRecursive(Entity entity, java.util.UUID owner) {
        if (entity instanceof AbstractGolemEntity<?, ?> age) {
            age.setOwnerUUID(owner);
        }
        for (var passenger : entity.getPassengers()) {
            setOwnerRecursive(passenger, owner);
        }
    }
}
