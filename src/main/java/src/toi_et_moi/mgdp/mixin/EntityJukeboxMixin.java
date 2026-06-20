package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.RecordItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import src.toi_et_moi.mgdp.jukebox.JukeboxGolem;
import src.toi_et_moi.mgdp.jukebox.JukeboxPacket;
import src.toi_et_moi.mgdp.modifier.special.LordModifier;

@Mixin(Entity.class)
public class EntityJukeboxMixin {

    @Inject(method = "remove(Lnet/minecraft/world/entity/Entity$RemovalReason;)V", at = @At("HEAD"))
    private void mgdp$stopJukeboxOnRemove(Entity.RemovalReason reason, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self.level().isClientSide) return;
        if (!(self instanceof AbstractGolemEntity<?, ?> golem)) return;
        if (!(golem instanceof JukeboxGolem jb)) return;
        if (!jb.mgdp$isPlaying()) return;

        jb.mgdp$setPlaying(false);
        jb.mgdp$setTick(0);

        var disc = jb.mgdp$getDisc();
        // Stop NetMusic sound if applicable
        if (!disc.isEmpty() && src.toi_et_moi.mgdp.jukebox.NetMusicCompat.isLoaded()
                && src.toi_et_moi.mgdp.jukebox.NetMusicCompat.isNetMusicDisc(disc)) {
            var stopPacket = new src.toi_et_moi.mgdp.jukebox.packet.NetMusicSoundPacket(
                    src.toi_et_moi.mgdp.jukebox.packet.NetMusicSoundPacket.Action.STOP, golem.getId(), "", 0, "");
            for (var p : golem.getServer().getPlayerList().getPlayers()) {
                if (p.distanceToSqr(golem) < 96 * 96) {
                    src.toi_et_moi.mgdp.Mgdp.PACKET_HANDLER.send(
                            net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> p), stopPacket);
                }
            }
        }
        if (!disc.isEmpty() && disc.getItem() instanceof RecordItem ri) {
            var soundId = ri.getSound().getLocation();
            // Send to owner directly (TRACKING_ENTITY may not work during removal)
            if (golem.getOwnerUUID() != null && golem.getServer() != null) {
                var owner = golem.getServer().getPlayerList().getPlayer(golem.getOwnerUUID());
                if (owner != null) {
                    JukeboxPacket.stopRecordForPlayer(owner, soundId, golem.getId());
                }
            }
            // Also attempt tracking-based (best-effort for nearby non-owner players)
            JukeboxPacket.stopRecordForTracking(golem, soundId);
        }
    }

    @Inject(method = "remove(Lnet/minecraft/world/entity/Entity$RemovalReason;)V", at = @At("TAIL"))
    private void mgdp$lordCleanupOnRemove(Entity.RemovalReason reason, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self.level().isClientSide) return;
        if (self instanceof AbstractGolemEntity<?, ?> golem) {
            LordModifier.onGolemRemoved(golem);
            src.toi_et_moi.mgdp.modifier.hostility.SelfDestructModifier.explode(golem);
        }
    }
}
