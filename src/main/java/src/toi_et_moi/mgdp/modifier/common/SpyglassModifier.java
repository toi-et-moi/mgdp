package src.toi_et_moi.mgdp.modifier.common;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

public class SpyglassModifier extends GolemModifier {

    private static final int SCAN_INTERVAL = 40;
    private static final int RADIUS = 32;
    private static final double LINE_SPACING = 2.0;

    public SpyglassModifier() {
        super(StatFilterType.MASS, 1);
    }

    @Override
    public void onAiStep(AbstractGolemEntity<?, ?> golem, int lv) {
        if (golem.level().isClientSide()) return;
        if (!(golem.level() instanceof ServerLevel sl)) return;
        if (golem.tickCount % SCAN_INTERVAL != 0) return;

        // Night vision for owner
        var owner = golem.getOwnerUUID() != null ? sl.getPlayerByUUID(golem.getOwnerUUID()) : null;
        if (owner instanceof ServerPlayer sp && sp.distanceToSqr(golem) < 1600) {
            sp.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, false, false, true));
        }

        BlockPos center = golem.blockPosition();
        int chunkX = center.getX() >> 4;
        int chunkZ = center.getZ() >> 4;
        int radiusChunks = (RADIUS >> 4) + 1;

        var packets = new java.util.ArrayList<net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket>();

        for (int cx = chunkX - radiusChunks; cx <= chunkX + radiusChunks; cx++) {
            for (int cz = chunkZ - radiusChunks; cz <= chunkZ + radiusChunks; cz++) {
                LevelChunk chunk = sl.getChunkSource().getChunk(cx, cz, false);
                if (chunk == null) continue;

                for (var entry : chunk.getBlockEntities().entrySet()) {
                    BlockPos pos = entry.getKey();
                    if (pos.distSqr(center) > RADIUS * RADIUS) continue;
                    BlockEntity be = entry.getValue();
                    if (!(be instanceof BaseContainerBlockEntity)) continue;

                    // Glow particles at container position
                    double cxPos = pos.getX() + 0.5;
                    double cyPos = pos.getY() + 0.5;
                    double czPos = pos.getZ() + 0.5;
                    packets.add(new net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket(
                            net.minecraft.core.particles.ParticleTypes.GLOW, true,
                            cxPos, cyPos, czPos, 0.5f, 0.5f, 0.5f, 0, 6
                    ));

                    // Dotted line from golem to container (every 2 blocks)
                    Vec3 from = new Vec3(center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5);
                    Vec3 to = new Vec3(cxPos, cyPos, czPos);
                    Vec3 dir = to.subtract(from);
                    double len = dir.length();
                    dir = dir.normalize();
                    for (double t = 1; t < len; t += LINE_SPACING) {
                        Vec3 p = from.add(dir.scale(t));
                        packets.add(new net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket(
                                net.minecraft.core.particles.ParticleTypes.END_ROD, true,
                                p.x, p.y, p.z, 0, 0, 0, 0, 1
                        ));
                    }
                }
            }
        }

        if (!packets.isEmpty()) {
            var server = sl.getServer();
            for (var pkt : packets) {
                server.getPlayerList().broadcast(null, center.getX(), center.getY(), center.getZ(),
                        64, sl.dimension(), pkt);
            }
        }
    }
}
