package src.toi_et_moi.mgdp.modifier.conqueror;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import src.toi_et_moi.mgdp.init.IConquerorData;

import java.util.List;
import java.util.Random;

public class ConquerorModifier extends GolemModifier {

	public static final int MAX_STAR = 6;
	private static final Random RANDOM = new Random();

	public ConquerorModifier() {
		super(StatFilterType.HEALTH, 1);
	}

	public static int getStarFromRatio(double ratio) {
		if (ratio >= 200) return 6;
		if (ratio >= 120) return 5;
		if (ratio >= 70) return 4;
		if (ratio >= 35) return 3;
		if (ratio >= 15) return 2;
		if (ratio >= 5) return 1;
		return 0;
	}

	private static double getDamageBoost(int star) {
		return switch (star) {
			case 1 -> 0.20;
			case 2 -> 0.30;
			case 3 -> 0.40;
			case 4 -> 0.50;
			case 5 -> 0.75;
			case 6 -> 1.00;
			default -> 0;
		};
	}

	private static double getDamageReduction(int star) {
		return switch (star) {
			case 1 -> 0.10;
			case 2 -> 0.20;
			case 3 -> 0.30;
			case 4 -> 0.40;
			case 5 -> 0.75;
			case 6 -> 0.90;
			default -> 0;
		};
	}

	private static float getInvulnSeconds(int star) {
		return switch (star) {
			case 2 -> 0.5f;
			case 3 -> 1.0f;
			case 4 -> 1.5f;
			case 5 -> 2.0f;
			case 6 -> 5.0f;
			default -> 0;
		};
	}

	private static int getIgnoreDistance(int star) {
		return switch (star) {
			case 3 -> 35;
			case 4 -> 25;
			case 5 -> 15;
			case 6 -> 5;
			default -> Integer.MAX_VALUE;
		};
	}

	private static double getIgnoreChance(int star) {
		return switch (star) {
			case 4 -> 0.10;
			case 5 -> 0.25;
			case 6 -> 0.50;
			default -> 0;
		};
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		if (!(golem instanceof IConquerorData data)) return;

		double total = data.mgdp$getVetHp();
		double ratio = golem.getMaxHealth() > 0 ? total / golem.getMaxHealth() : 0;
		int newStar = getStarFromRatio(ratio);
		int oldStar = data.mgdp$getVetStar();

		if (newStar != oldStar) {
			data.mgdp$setVetStar(newStar);
			if (newStar > oldStar) {
				var owner = golem.getOwner();
				if (owner != null) {
					owner.sendSystemMessage(Component.translatable(
							"message.mgdp.conqueror_upgrade",
							golem.getDisplayName(), newStar));
				}
			}
		}

		if (newStar >= 3) {
			golem.getActiveEffects().stream()
					.filter(e -> !e.getEffect().isBeneficial())
					.toList()
					.forEach(e -> golem.removeEffect(e.getEffect()));
		} else if (newStar >= 2) {
			List<MobEffectInstance> effects = List.copyOf(golem.getActiveEffects());
			for (MobEffectInstance inst : effects) {
				if (!inst.getEffect().isBeneficial() && inst.getDuration() > 200) {
					golem.addEffect(new MobEffectInstance(inst.getEffect(), 200,
							inst.getAmplifier(), inst.isAmbient(), inst.isVisible()));
				}
			}
		}
	}

	@Override
	public void onHurt(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		if (!(golem instanceof IConquerorData data)) return;
		int star = data.mgdp$getVetStar();
		if (star < 1) return;

		int tick = golem.tickCount;

		float invulnSec = getInvulnSeconds(star);
		if (invulnSec > 0 && tick < data.mgdp$getInvulnUntil()) {
			event.setCanceled(true);
			return;
		}

		int maxDist = getIgnoreDistance(star);
		if (maxDist < Integer.MAX_VALUE && event.getSource().getEntity() instanceof LivingEntity attacker) {
			double dist = golem.distanceTo(attacker);
			if (dist > maxDist) {
				event.setCanceled(true);
				return;
			}
		}

		double chance = getIgnoreChance(star);
		if (chance > 0 && RANDOM.nextDouble() < chance) {
			event.setCanceled(true);
			return;
		}

		double reduction = getDamageReduction(star);
		if (reduction > 0) {
			event.setAmount(event.getAmount() * (float) (1 - reduction));
		}

		if (invulnSec > 0) {
			data.mgdp$setInvulnUntil(tick + (int) (invulnSec * 20));
		}
	}

	@Override
	public void onHurtTarget(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		if (!(golem instanceof IConquerorData data)) return;
		int star = data.mgdp$getVetStar();
		if (star < 1) return;

		float baseAmount = event.getAmount();
		double boost = getDamageBoost(star);
		if (boost > 0) {
			event.setAmount(baseAmount * (float) (1 + boost));
		}
	}
}
