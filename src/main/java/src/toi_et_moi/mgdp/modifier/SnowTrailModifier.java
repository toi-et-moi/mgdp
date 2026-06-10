package src.toi_et_moi.mgdp.modifier;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.List;

public class SnowTrailModifier extends GolemModifier {

    private static final int FROST_LEVEL = 2;

    public SnowTrailModifier() {
        super(StatFilterType.MASS, 1);
    }

    @Override
    public List<net.minecraft.network.chat.MutableComponent> getDetail(int v) {
        return List.of(net.minecraft.network.chat.Component.translatable(getDescriptionId() + ".desc")
                .withStyle(net.minecraft.ChatFormatting.GREEN));
    }

    @Override
    public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
        if (golem.level().isClientSide) return;
        Level levelWorld = golem.level();

        // Frost Walker effect - freeze water around the golem
        if (golem.onGround()) {
            int frostLevel = FROST_LEVEL + level - 1;
            FrostWalkerEnchantment.onEntityMoved(golem, levelWorld, golem.blockPosition(), frostLevel);
        }

        // Snow trail - place snow layers like a snow golem
        if (!levelWorld.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_MOBGRIEFING)) return;

        BlockPos pos = golem.blockPosition();
        if (levelWorld.getBiome(pos).is(BiomeTags.SNOW_GOLEM_MELTS)) return;

        BlockState snow = Blocks.SNOW.defaultBlockState();
        for (int i = 0; i < 4; i++) {
            int dx = (i % 2 * 2 - 1);
            int dz = ((i / 2 + 1) % 2 * 2 - 1);
            BlockPos target = pos.offset(dx, 0, dz);
            if (levelWorld.getBlockState(target).isAir()
                    && snow.canSurvive(levelWorld, target)
                    && levelWorld.isUnobstructed(snow, target, CollisionContext.empty())) {
                levelWorld.setBlockAndUpdate(target, snow);
            }
        }
    }
}
