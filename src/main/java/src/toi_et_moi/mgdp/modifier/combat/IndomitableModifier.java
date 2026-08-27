package src.toi_et_moi.mgdp.modifier.combat;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import src.toi_et_moi.mgdp.init.IFlipData;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

/**
 * 锐不可当：满足条件时直接高跳并朝目标打出大量散射箭矢（复刻突变骷髅）。
 * 箭矢继承手持弓附魔；每级箭矢伤害额外 +傀儡攻击力 10%，且每级多打出一轮箭矢（每轮 4 刻 × 6 支，与原著一致）。
 * 箭矢无视无敌帧并无视护甲。冷却 5 秒（概率触发，实际间隔更长）。
 */
public class IndomitableModifier extends GolemModifier {

	private static final String TAG_CD = "mgdp_indomitable_cd";
	private static final String TAG_SHOT = "mgdp_indomitable_shot";
	private static final String TAG_FLIP_TICK = "mgdp_indomitable_flip_tick"; // 后空翻联动动画计时
	public static final String TAG_ARROW = "mgdp_indomitable_arrow";
	public static final String TAG_TARGET = "mgdp_indomitable_target";
	/** 每 tick 朝目标转向的比例（0=纯散射直飞，1=第一刻完全锁定目标中心，即 L2Archery 那串箭效果） */
	public static final float TRACK_RATE = 0.2F;

	private static final int COOLDOWN = 100;      // 固定 5 秒冷却（无目标时同样计时）
	private static final int JUMP_DELAY = 12;     // 跳起后延迟 12 刻再开始弹幕
	private static final int VOLLEY_TICKS = 4;    // 每轮 4 刻
	private static final int ARROWS_PER_TICK = 6; // 每刻 6 支（散射弹幕与连发弹幕各占 6 支）

	public IndomitableModifier() {
		super(StatFilterType.ATTACK, 3);
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		var pdata = golem.getPersistentData();

		// 后空翻动画推进（每 tick 独立计时，与锐不可当状态机互不干扰）
		updateFlipAnimation(golem);

		// 跳起延迟中：跳起后再开始散射
		int shot = pdata.getInt(TAG_SHOT);
		if (shot < 0) {
			shot++;
			if (shot == 0) {
				LivingEntity target = golem.getTarget();
				if (target == null || !target.isAlive()) {
					pdata.putInt(TAG_SHOT, 0);
					pdata.putInt(TAG_CD, COOLDOWN);
					return;
				}
				pdata.putInt(TAG_SHOT, 1);
				fireVolley(golem, target, level);
			} else {
				pdata.putInt(TAG_SHOT, shot);
			}
			return;
		}

		// 散射进行中：每刻发射一轮箭矢
		if (shot > 0) {
			LivingEntity target = golem.getTarget();
			if (target == null || !target.isAlive()) {
				pdata.putInt(TAG_SHOT, 0);
				pdata.putInt(TAG_CD, COOLDOWN);
				return;
			}
			fireVolley(golem, target, level);
			shot++;
			if (shot >= level * VOLLEY_TICKS) {
				pdata.putInt(TAG_SHOT, 0);
				pdata.putInt(TAG_CD, COOLDOWN);
			} else {
				pdata.putInt(TAG_SHOT, shot);
			}
			return;
		}

		// 冷却
		int cd = pdata.getInt(TAG_CD);
		if (cd > 0) {
			pdata.putInt(TAG_CD, cd - 1);
			return;
		}

		// 冷却已好：有目标且满足条件即固定触发（无概率）
		LivingEntity target = golem.getTarget();
		if (target == null || !target.isAlive()) return;
		if (!golem.onGround() || !golem.hasLineOfSight(target)) return;

		// 只先高跳；延迟 JUMP_DELAY 刻后再开始弹幕；触发即进入 5 秒冷却
		doJump(golem, target);
		pdata.putInt(TAG_SHOT, -JUMP_DELAY);
		pdata.putInt(TAG_CD, COOLDOWN);
	}

	private static void doJump(AbstractGolemEntity<?, ?> golem, LivingEntity target) {
		double dx = target.getX() - golem.getX();
		double dz = target.getZ() - golem.getZ();
		float scale = 0.06F + golem.getRandom().nextFloat() * 0.03F;
		if (golem.distanceToSqr(target) < 16.0) {
			// 目标太近：反向跳开并加大力度（复刻突变骷髅）
			dx *= -1.0;
			dz *= -1.0;
			scale *= 5.0F;
		}
		golem.setDeltaMovement(dx * scale, 1.1, dz * scale);
		triggerFlip(golem);
	}

	/** 装了后空翻（Backflip）时，高跳触发后空翻动画（参考后跳升级） */
	private static void triggerFlip(AbstractGolemEntity<?, ?> golem) {
		if (golem.getModifiers().containsKey(MGDPModifiers.BACKFLIP.get())) {
			golem.getPersistentData().putInt(TAG_FLIP_TICK, 1);
		}
	}

	/** 后空翻动画逐 tick 推进，进度 0~400 */
	private static void updateFlipAnimation(AbstractGolemEntity<?, ?> golem) {
		var data = golem.getPersistentData();
		int tick = data.getInt(TAG_FLIP_TICK);
		if (tick <= 0) return;
		int progress = Math.min(tick * 25, 400);
		((IFlipData) golem).mgdp$setFlipProgress(progress);
		if (progress >= 400) {
			data.remove(TAG_FLIP_TICK);
		} else {
			data.putInt(TAG_FLIP_TICK, tick + 1);
		}
	}

	private static void fireVolley(AbstractGolemEntity<?, ?> golem, LivingEntity target, int level) {
		var world = golem.level();
		if (world.isClientSide()) return;
		float atk = (float) golem.getAttributeValue(Attributes.ATTACK_DAMAGE);
		float bonus = atk * 0.1F * level; // 每级 +傀儡攻击力 10%
		// 使用傀儡当前可打出的弹药/武器类型（弓/弩箭矢、三叉戟等）；不消耗弹药
		ItemStack ammo = golem.getProjectile(golem.getMainHandItem());
		// 每刻双弹幕：前 ARROWS_PER_TICK 支为散射，后 ARROWS_PER_TICK 支为连发（箭数一致）
		for (int i = 0; i < ARROWS_PER_TICK * 2; i++) {
			boolean scatter = i < ARROWS_PER_TICK;
			AbstractArrow arrow = createProjectile(golem, world, ammo);
			if (arrow == null) continue;
			arrow.setCritArrow(true);
			arrow.setEnchantmentEffectsFromEntity(golem, 2.0F); // 继承手持弓附魔
			arrow.setBaseDamage(arrow.getBaseDamage() + bonus); // 除箭本身外每级 +10% 攻击力
			arrow.getPersistentData().putBoolean(TAG_ARROW, true);
			arrow.getPersistentData().putInt(TAG_TARGET, target.getId()); // 记录追踪目标
			// 与傀儡正常射出的箭一致：不可拾取，落地后快速消失（DespawnFactor=20）
			arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
			arrow.getPersistentData().putInt("DespawnFactor", 20);
			Vec3 aim;
			if (scatter) {
				// 散射弹幕：原著散布，仅 x/z 在目标 ±3 随机，y 固定 80% 身高处
				double rx = (golem.getRandom().nextFloat() - 0.5F) * 6.0F;
				double rz = (golem.getRandom().nextFloat() - 0.5F) * 6.0F;
				aim = target.position().add(rx, target.getBbHeight() * 0.8, rz);
			} else {
				// 连发弹幕：不散射，直接瞄准目标上半身中心，形成一条连续发射线
				aim = target.position().add(0, target.getBbHeight() * 0.9, 0);
			}
			double dx = aim.x - arrow.getX();
			double dy = aim.y - arrow.getY();
			double dz = aim.z - arrow.getZ();
			double dist = Math.sqrt(dx * dx + dz * dz);
			arrow.shoot(dx, dy + dist * 0.2, dz, 2.0F, 0.0F);
			world.addFreshEntity(arrow);
		}
		golem.playSound(SoundEvents.CROSSBOW_SHOOT, 1.0F,
				1.0F / (golem.getRandom().nextFloat() * 0.4F + 1.2F) + 0.25F);
	}

	/**
	 * 按傀儡当前武器/弹药生成对应投射物：
	 * 弓/弩弹药（ArrowItem）→ 原样创建（药水箭/光灵箭/莱特兰弓艺箭都保留）；
	 * 手持三叉戟 → 投掷 ThrownTrident；
	 * 手持莱特兰武器（L2Weaponry BaseThrowableWeaponItem）→ 投掷对应实体（与 L2ThrowableBehavior 一致）；
	 * 其余回退原版箭。
	 */
	private static AbstractArrow createProjectile(AbstractGolemEntity<?, ?> golem, Level world, ItemStack ammo) {
		if (ammo.getItem() instanceof ArrowItem arrowItem) {
			return arrowItem.createArrow(world, ammo, golem);
		}
		if (golem.getMainHandItem().getItem() instanceof TridentItem) {
			return new ThrownTrident(world, golem, golem.getMainHandItem().copy());
		}
		// 莱特兰武器（L2Weaponry）投掷：反射调用 item.getProjectile(level, user, stack, slot)
		AbstractArrow thrown = createL2Throwable(golem, world);
		if (thrown != null) return thrown;
		return new Arrow(world, golem);
	}

	/**
	 * L2Weaponry 的 BaseThrowableWeaponItem 生成对应投掷实体（BaseThrownWeaponEntity extends AbstractArrow）。
	 * 用反射调用，避免对 L2Weaponry 的编译期硬依赖。
	 */
	private static AbstractArrow createL2Throwable(AbstractGolemEntity<?, ?> golem, Level world) {
		ItemStack main = golem.getMainHandItem();
		if (main.isEmpty()) return null;
		String cls = main.getItem().getClass().getName();
		if (!cls.startsWith("dev.xkmc.l2weaponry.")) return null;
		try {
			var m = main.getItem().getClass().getMethod("getProjectile",
					Level.class, LivingEntity.class, ItemStack.class, int.class);
			Object proj = m.invoke(main.getItem(), world, golem, main, 0);
			if (proj instanceof AbstractArrow arrow) {
				arrow.setBaseDamage((float) golem.getAttributeValue(Attributes.ATTACK_DAMAGE));
				arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
				return arrow;
			}
		} catch (Exception ignored) {
		}
		return null;
	}
}
