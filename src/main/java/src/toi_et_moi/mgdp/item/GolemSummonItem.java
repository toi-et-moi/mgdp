package src.toi_et_moi.mgdp.item;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.registries.ForgeRegistries;
import src.toi_et_moi.mgdp.Config;
import src.toi_et_moi.mgdp.compat.GolemDungeonsCompat;

import java.util.ArrayList;
import java.util.List;

public class GolemSummonItem extends Item {

    private final ResourceLocation spawnConfigId;

    public GolemSummonItem(Properties props, ResourceLocation spawnConfigId) {
        super(props);
        this.spawnConfigId = spawnConfigId;
    }

    /** 铁砧修复材料：golemdungeons 的试炼勋章（软依赖，未安装时返回 null） */
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        Item medal = ForgeRegistries.ITEMS.getValue(new ResourceLocation("golemdungeons", "trial_medal"));
        return medal != null && repair.is(medal);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        var player = ctx.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (!(ctx.getLevel() instanceof ServerLevel sl)) return InteractionResult.SUCCESS;

        if (!GolemDungeonsCompat.isLoaded()) return InteractionResult.PASS;

        if (player.isShiftKeyDown()) {
            return summonFormation(sl, player, ctx);
        }

        LivingEntity golem = GolemDungeonsCompat.summonFactionGolem(spawnConfigId, sl, player.getUUID());
        if (golem == null) return InteractionResult.PASS;

        golem.setPos(ctx.getClickLocation());
        recursiveAdd(sl, golem);

        if (!player.getAbilities().instabuild) {
            ctx.getItemInHand().hurtAndBreak(1, player, (e) -> e.broadcastBreakEvent(ctx.getHand()));
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * 潜行使用：一次消耗全部剩余耐久，召唤对应数量的无主傀儡，
     * 并以点击位置为中心按方形网格平铺在周围地面上（摆阵形用）。
     */
    private InteractionResult summonFormation(ServerLevel level, Player player, UseOnContext ctx) {
        ItemStack stack = ctx.getItemInHand();
        int count = stack.isDamageableItem() ? stack.getMaxDamage() - stack.getDamageValue() : 1;
        if (count <= 0) return InteractionResult.PASS;

        List<LivingEntity> golems = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            LivingEntity golem = GolemDungeonsCompat.summonFactionGolem(spawnConfigId, level, null);
            if (golem != null) golems.add(golem);
        }
        if (golems.isEmpty()) return InteractionResult.PASS;

        int step = Config.summonSpacing;
        BlockPos center = BlockPos.containing(ctx.getClickLocation());
        List<BlockPos> offsets = formationOffsets(count, step);
        for (int i = 0; i < golems.size(); i++) {
            BlockPos off = offsets.get(i);
            int x = center.getX() + off.getX();
            int z = center.getZ() + off.getZ();
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            LivingEntity golem = golems.get(i);
            golem.setPos(x + 0.5, y, z + 0.5);
            recursiveAdd(level, golem);
        }

        if (!player.getAbilities().instabuild) {
            stack.hurtAndBreak(stack.getMaxDamage() - stack.getDamageValue(), player, (e) -> e.broadcastBreakEvent(ctx.getHand()));
        }
        return InteractionResult.SUCCESS;
    }

    /** 从中心向外逐圈铺开的方形网格偏移，首项为中心 (0,0)。 */
    private static List<BlockPos> formationOffsets(int count, int step) {
        List<BlockPos> list = new ArrayList<>(count);
        list.add(BlockPos.ZERO);
        for (int r = 1; list.size() < count; r++) {
            int d = r * step;
            for (int i = -r; i <= r && list.size() < count; i++) list.add(new BlockPos(i * step, 0, -d));
            for (int j = -r + 1; j <= r && list.size() < count; j++) list.add(new BlockPos(d, 0, j * step));
            for (int i = r - 1; i >= -r && list.size() < count; i--) list.add(new BlockPos(i * step, 0, d));
            for (int j = r - 1; j > -r && list.size() < count; j--) list.add(new BlockPos(-d, 0, j * step));
        }
        return list;
    }

    private static void recursiveAdd(ServerLevel level, Entity e) {
        level.addFreshEntity(e);
        for (var x : e.getPassengers()) {
            x.setPos(e.position());
            recursiveAdd(level, x);
        }
    }
}
