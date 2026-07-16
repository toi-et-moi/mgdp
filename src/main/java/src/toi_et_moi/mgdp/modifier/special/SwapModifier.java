package src.toi_et_moi.mgdp.modifier.special;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import src.toi_et_moi.mgdp.Mgdp;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

import java.util.*;

@Mod.EventBusSubscriber(modid = Mgdp.MODID)
public class SwapModifier extends GolemModifier {

    private static final double SWAP_RANGE = 64.0;
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();

    public SwapModifier() {
        super(StatFilterType.MASS, 1);
    }

    @Override
    public List<net.minecraft.network.chat.MutableComponent> getDetail(int v) {
        return List.of(
                net.minecraft.network.chat.Component.translatable(getDescriptionId() + ".line1")
                        .withStyle(net.minecraft.ChatFormatting.GREEN),
                net.minecraft.network.chat.Component.translatable(getDescriptionId() + ".line2")
                        .withStyle(net.minecraft.ChatFormatting.GREEN)
        );
    }

    // Active swap: called when player presses the swap key
    public static void trySwapByPlayer(ServerPlayer player) {
        if (onCooldown(player)) return;
        AbstractGolemEntity<?, ?> target = findSwapGolem(player);
        if (target == null) return;
        doSwap(player, target);
        setCooldown(player);
    }

    // Passive swap: triggered on fatal damage
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.getEntity().level().isClientSide) return;
        if (onCooldown(player)) return;
        // Check if this damage would be fatal (HP after damage <= 0)
        float healthAfter = player.getHealth() - event.getAmount();
        if (healthAfter > 0 && !player.isDeadOrDying() && !player.isInvulnerableTo(player.level().damageSources().generic())) {
            return;
        }
        AbstractGolemEntity<?, ?> target = findSwapGolem(player);
        if (target == null) return;
        event.setCanceled(true);
        doSwap(player, target);
        setCooldown(player);
    }

    private static AbstractGolemEntity<?, ?> findSwapGolem(Player player) {
        var level = player.level();
        List<AbstractGolemEntity<?, ?>> candidates = new ArrayList<>();
        for (var golem : level.getEntitiesOfClass(AbstractGolemEntity.class,
                player.getBoundingBox().inflate(SWAP_RANGE),
                e -> e.isAlive() && e.getOwnerUUID() != null
                        && e.getOwnerUUID().equals(player.getUUID())
                        && e.getModifiers().containsKey(MGDPModifiers.SWAP.get()))) {
            candidates.add(golem);
        }
        if (candidates.isEmpty()) return null;
        // Pick the farthest golem for better escape distance
        candidates.sort(java.util.Comparator.comparingDouble(
                g -> -g.distanceToSqr(player)));
        return candidates.get(0);
    }

    private static void doSwap(ServerPlayer player, AbstractGolemEntity<?, ?> golem) {
        var pPos = player.position();
        var gPos = golem.position();

        // Transfer negative effects from player to golem
        var toTransfer = new java.util.ArrayList<net.minecraft.world.effect.MobEffectInstance>();
        for (var effect : player.getActiveEffects()) {
            if (!effect.getEffect().isBeneficial()) {
                toTransfer.add(new net.minecraft.world.effect.MobEffectInstance(effect));
            }
        }
        for (var effect : toTransfer) {
            player.removeEffect(effect.getEffect());
            golem.addEffect(effect);
        }

        // Also transfer fire ticks to golem
        int fire = player.getRemainingFireTicks();
        if (fire > 0) {
            golem.setRemainingFireTicks(fire);
            player.setRemainingFireTicks(0);
        }

        player.teleportTo(gPos.x, gPos.y, gPos.z);
        golem.teleportTo(pPos.x, pPos.y, pPos.z);
        player.level().playSound(null, pPos.x, pPos.y, pPos.z,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
        player.level().playSound(null, gPos.x, gPos.y, gPos.z,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private static boolean onCooldown(Player player) {
        Long time = COOLDOWNS.get(player.getUUID());
        return time != null && time > player.level().getGameTime();
    }

    private static void setCooldown(Player player) {
        int ticks = src.toi_et_moi.mgdp.Config.swapCooldown * 20;
        COOLDOWNS.put(player.getUUID(), player.level().getGameTime() + ticks);
    }
}
