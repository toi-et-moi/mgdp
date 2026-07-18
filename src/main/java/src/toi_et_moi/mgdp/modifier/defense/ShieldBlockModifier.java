package src.toi_et_moi.mgdp.modifier.defense;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import src.toi_et_moi.mgdp.init.IFlipData;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

import java.util.List;

public class ShieldBlockModifier extends GolemModifier {

	private static final int MAX_SHIELDS = 5;
	private static final int REFRESH_INTERVAL = 300; // 15 seconds
	private static final String TAG_SHIELDS = "mgdp_sb_shields";
	private static final String TAG_SHIELD_HP = "mgdp_sb_shield_hp";
	private static final String TAG_REFRESH = "mgdp_sb_refresh";

	public ShieldBlockModifier() {
		super(StatFilterType.HEALTH, 1);
	}

	private static int getLayerMaxHp(AbstractGolemEntity<?, ?> golem) {
		return Math.max(10, (int) (golem.getMaxHealth() * 0.05f));
	}

	private static void syncToClient(AbstractGolemEntity<?, ?> golem, CompoundTag tag) {
		if (!(golem instanceof IFlipData data)) return;
		int shields = tag.getInt(TAG_SHIELDS);
		int hp = tag.contains(TAG_SHIELD_HP) ? tag.getInt(TAG_SHIELD_HP) : 0;
		data.mgdp$setSbShields(shields);
		data.mgdp$setSbHp(hp);
	}

	@Override
	public List<MutableComponent> getDetail(int v) {
		return List.of(Component.translatable(getDescriptionId() + ".desc", MAX_SHIELDS)
				.withStyle(ChatFormatting.GREEN));
	}

	@Override
	public void onAttacked(AbstractGolemEntity<?, ?> golem, LivingAttackEvent event, int level) {
		if (event.getSource().is(DamageTypeTags.BYPASSES_SHIELD)) return;
		CompoundTag tag = golem.getPersistentData();
		int shields = tag.getInt(TAG_SHIELDS);
		if (shields <= 0) return;

		int hp = tag.getInt(TAG_SHIELD_HP);
		int maxHp = getLayerMaxHp(golem);

		// If shield HP not initialized, full restore
		if (hp <= 0) {
			tag.putInt(TAG_SHIELD_HP, maxHp);
			hp = maxHp;
		}

		float amount = event.getAmount();
		if (amount >= hp) {
			// Current layer breaks
			shields--;
			tag.putInt(TAG_SHIELDS, shields);
			if (shields > 0) {
				tag.putInt(TAG_SHIELD_HP, maxHp);
			} else {
				tag.remove(TAG_SHIELD_HP);
			}
			golem.level().playSound(null, golem.blockPosition(), SoundEvents.SHIELD_BREAK,
					SoundSource.NEUTRAL, 1.0F, 0.8F);
		} else {
			tag.putInt(TAG_SHIELD_HP, hp - (int) amount);
			golem.level().playSound(null, golem.blockPosition(), SoundEvents.SHIELD_BLOCK,
					SoundSource.NEUTRAL, 1.0F, 0.8F);
		}

		event.setCanceled(true);
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		CompoundTag tag = golem.getPersistentData();

		if (!tag.contains(TAG_SHIELDS)) {
			tag.putInt(TAG_SHIELDS, MAX_SHIELDS);
			tag.putInt(TAG_SHIELD_HP, getLayerMaxHp(golem));
			tag.putLong(TAG_REFRESH, golem.level().getGameTime());
			syncToClient(golem, tag);
			return;
		}

		syncToClient(golem, tag);
		int shields = tag.getInt(TAG_SHIELDS);
		int hp = tag.getInt(TAG_SHIELD_HP);
		int maxHp = getLayerMaxHp(golem);
		if (shields >= MAX_SHIELDS && hp >= maxHp) return;

		long lastRefresh = tag.getLong(TAG_REFRESH);
		long now = golem.level().getGameTime();
		if (now - lastRefresh < REFRESH_INTERVAL) return;

		tag.putInt(TAG_SHIELDS, MAX_SHIELDS);
		tag.putInt(TAG_SHIELD_HP, maxHp);
		tag.putLong(TAG_REFRESH, now);
		golem.level().playSound(null, golem.blockPosition(), SoundEvents.SMITHING_TABLE_USE,
				SoundSource.NEUTRAL, 1.0F, 1.0F);
		syncToClient(golem, tag);
	}
}
