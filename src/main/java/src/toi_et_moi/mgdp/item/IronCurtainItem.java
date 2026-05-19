package src.toi_et_moi.mgdp.item;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class IronCurtainItem extends Item {

    private static final int CHARGE_TIME = 40;  // 2 seconds
    private static final int DURATION = 1000;   // 50 seconds
    private static final double RANGE = 16.0;
    private static final String TAG_EXPIRY = "mgdp_iron_curtain";

    public IronCurtainItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000; // Maximum use duration
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        return stack;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingTicks) {
        if (entity instanceof Player player) {
            int used = getUseDuration(stack) - remainingTicks;
            if (used == CHARGE_TIME) {
                level.playSound(player, player.blockPosition(), SoundEvents.BEACON_ACTIVATE,
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.5F);
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) return;
        int usedTicks = getUseDuration(stack) - timeLeft;
        if (usedTicks < CHARGE_TIME) return;

        if (!level.isClientSide) {
            activate(player, level);
            if (!player.isCreative()) {
                player.getCooldowns().addCooldown(this, 6000); // 5 minutes
            }
        }
    }

    private void activate(Player player, Level level) {
        long expiry = level.getGameTime() + DURATION;
        AABB area = player.getBoundingBox().inflate(RANGE);

        List<LivingEntity> allies = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e.isAlive() && isAlly(player, e));

        for (LivingEntity ally : allies) {
            ally.getPersistentData().putLong(TAG_EXPIRY, expiry);
        }

        Component msg = Component.literal("Warning:Iron Curtain Activated!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
        for (Player p : level.players()) {
            p.displayClientMessage(msg, false);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.ELDER_GUARDIAN_CURSE,
                net.minecraft.sounds.SoundSource.PLAYERS, 1.5F, 1.0F);
    }

    private boolean isAlly(Player player, Entity entity) {
        if (entity == player) return true;
        if (entity.isAlliedTo(player)) return true;
        if (entity instanceof AbstractGolemEntity<?, ?> golem) {
            return player.getUUID().equals(golem.getOwnerUUID());
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level level,
                                 List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item.mgdp.iron_curtain.desc").withStyle(ChatFormatting.DARK_RED));
    }

    public static boolean isProtected(LivingEntity entity) {
        CompoundTag data = entity.getPersistentData();
        if (!data.contains(TAG_EXPIRY)) return false;
        long expiry = data.getLong(TAG_EXPIRY);
        if (entity.level().getGameTime() > expiry) {
            data.remove(TAG_EXPIRY);
            return false;
        }
        return true;
    }
}
