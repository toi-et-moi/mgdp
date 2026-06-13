package src.toi_et_moi.mgdp.modifier.defense;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;

public class ChargedShieldModifier extends GolemModifier {

    private static final int[] REGEN_INTERVALS = {300, 200, 100}; // 15/10/5 seconds
    private static final int SHIELDS_PER_LEVEL = 5;
    private static final String TAG_SHIELDS = "mgdp_shields";
    private static final String TAG_REGEN = "mgdp_shield_regen";

    public ChargedShieldModifier() {
        super(StatFilterType.HEALTH, 3);
    }

    @Override
    public List<MutableComponent> getDetail(int v) {
        int idx = Math.min(v - 1, REGEN_INTERVALS.length - 1);
        int interval = REGEN_INTERVALS[idx] / 20;
        int shields = v * SHIELDS_PER_LEVEL;
        return List.of(Component.translatable(getDescriptionId() + ".desc", shields, interval)
                .withStyle(ChatFormatting.GREEN));
    }

    public static void recharge(AbstractGolemEntity<?, ?> golem, int level) {
        CompoundTag tag = golem.getPersistentData();
        int shields = tag.getInt(TAG_SHIELDS);
        int max = level * SHIELDS_PER_LEVEL;
        if (shields < max) {
            tag.putInt(TAG_SHIELDS, shields + 1);
        }
    }

    @Override
    public void onHurt(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
        CompoundTag tag = golem.getPersistentData();
        int shields = tag.getInt(TAG_SHIELDS);
        if (shields <= 0) return;

        event.setAmount(0);
        shields--;
        tag.putInt(TAG_SHIELDS, shields);
    }

    @Override
    public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
        if (golem.level().isClientSide()) return;
        int idx = Math.min(level - 1, REGEN_INTERVALS.length - 1);
        int interval = REGEN_INTERVALS[idx];
        int maxShields = level * SHIELDS_PER_LEVEL;

        CompoundTag tag = golem.getPersistentData();
        if (!tag.contains(TAG_SHIELDS)) {
            tag.putInt(TAG_SHIELDS, maxShields);
            tag.putLong(TAG_REGEN, golem.level().getGameTime());
            return;
        }
        int shields = tag.getInt(TAG_SHIELDS);
        if (shields >= maxShields) return;

        long lastRegen = tag.getLong(TAG_REGEN);
        long now = golem.level().getGameTime();
        long elapsed = now - lastRegen;
        if (elapsed >= interval) {
            int toAdd = (int) (elapsed / interval);
            shields = Math.min(maxShields, shields + toAdd);
            tag.putInt(TAG_SHIELDS, shields);
            tag.putLong(TAG_REGEN, now - (elapsed % interval));
        golem.level().playSound(null, golem.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.NEUTRAL, 1.0F, 1.0F);
        }
    }
}
