package src.toi_et_moi.mgdp.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraftforge.fml.ModList;
import src.toi_et_moi.mgdp.compat.GolemDungeonsCompat;

public class GolemSummonItem extends Item {

    private final ResourceLocation spawnConfigId;

    public GolemSummonItem(Properties props, ResourceLocation spawnConfigId) {
        super(props);
        this.spawnConfigId = spawnConfigId;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        var player = ctx.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (!(ctx.getLevel() instanceof ServerLevel sl)) return InteractionResult.SUCCESS;

        if (!GolemDungeonsCompat.isLoaded()) return InteractionResult.PASS;

        LivingEntity golem = GolemDungeonsCompat.summonFactionGolem(spawnConfigId, sl, player);
        if (golem == null) return InteractionResult.PASS;

        golem.setPos(ctx.getClickLocation());
        recursiveAdd(sl, golem);

        if (!player.getAbilities().instabuild) {
            ctx.getItemInHand().hurtAndBreak(1, player, (e) -> e.broadcastBreakEvent(ctx.getHand()));
        }
        return InteractionResult.SUCCESS;
    }

    private static void recursiveAdd(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.Entity e) {
        level.addFreshEntity(e);
        for (var x : e.getPassengers()) {
            x.setPos(e.position());
            recursiveAdd(level, x);
        }
    }
}
