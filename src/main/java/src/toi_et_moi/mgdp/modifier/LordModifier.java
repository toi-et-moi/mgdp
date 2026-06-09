package src.toi_et_moi.mgdp.modifier;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LordModifier extends GolemModifier {

    private static final Map<UUID, ServerBossEvent> BOSS_BARS = new HashMap<>();

    public LordModifier() {
        super(StatFilterType.MASS, 1);
    }

    @Override
    public List<MutableComponent> getDetail(int v) {
        return List.of(
                Component.translatable(getDescriptionId() + ".desc").withStyle(net.minecraft.ChatFormatting.GREEN));
    }

    @Override
    public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
        if (golem.level().isClientSide) return;

        UUID id = golem.getUUID();
        ServerBossEvent bar = BOSS_BARS.get(id);

        // Golem died or removed - clean up
        if (!golem.isAlive()) {
            if (bar != null) {
                bar.removeAllPlayers();
                BOSS_BARS.remove(id);
            }
            return;
        }

        // Create boss bar if needed
        if (bar == null) {
            bar = new ServerBossEvent(
                    golem.getDisplayName(),
                    BossEvent.BossBarColor.PURPLE,
                    BossEvent.BossBarOverlay.PROGRESS);
            BOSS_BARS.put(id, bar);
        }

        bar.setName(golem.getDisplayName());

        // Update health
        bar.setProgress(golem.getHealth() / golem.getMaxHealth());

        // Update visibility - add/remove nearby players
        var server = golem.getServer();
        if (server != null) {
            double range = 64.0;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                boolean nearby = player.distanceToSqr(golem) < range * range;
                boolean hasPlayer = bar.getPlayers().contains(player);
                if (nearby && !hasPlayer) {
                    bar.addPlayer(player);
                } else if (!nearby && hasPlayer) {
                    bar.removePlayer(player);
                }
            }
        }
    }

    /** Clean up boss bar for a golem that's being removed */
    public static void onGolemRemoved(AbstractGolemEntity<?, ?> golem) {
        UUID id = golem.getUUID();
        ServerBossEvent bar = BOSS_BARS.remove(id);
        if (bar != null) {
            bar.removeAllPlayers();
        }
    }
}
