package src.toi_et_moi.mgdp.entity;

import dev.xkmc.l2library.base.BaseEntity;
import dev.xkmc.l2serial.serialization.SerialClass;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 守卫者激光瞄准实体：蓄力期间在目标处渲染随音效闪烁的红瞄准线与收缩红圈；
 * 蓄满后切换为发白光的粗直线光束，短暂存在后消失。
 */
@SerialClass
public class GuardianLaserTargetEntity extends BaseEntity implements OwnableEntity {

	public static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(GuardianLaserTargetEntity.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> CHARGE = SynchedEntityData.defineId(GuardianLaserTargetEntity.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> FLASH = SynchedEntityData.defineId(GuardianLaserTargetEntity.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> BEAM = SynchedEntityData.defineId(GuardianLaserTargetEntity.class, EntityDataSerializers.INT);

	public static final int CHARGE_TICKS = 80;
	public static final int BEAM_TICKS = 8;

	@SerialClass.SerialField
	public UUID owner;
	@SerialClass.SerialField
	public int beamTicks;
	public int nextBeat = 1;

	public LivingEntity ownerCache;

	public GuardianLaserTargetEntity(EntityType<?> type, Level level) {
		super(type, level);
	}

	public GuardianLaserTargetEntity(EntityType<?> type, Level level, LivingEntity owner) {
		super(type, level);
		this.owner = owner.getUUID();
		ownerCache = owner;
		entityData.set(OWNER_ID, owner.getId());
		entityData.set(CHARGE, 0);
		entityData.set(FLASH, 0);
		entityData.set(BEAM, 0);
		nextBeat = 1;
	}

	@Override
	protected void defineSynchedData() {
		entityData.define(OWNER_ID, -1);
		entityData.define(CHARGE, 0);
		entityData.define(FLASH, 0);
		entityData.define(BEAM, 0);
	}

	public int getCharge() {
		return entityData.get(CHARGE);
	}

	/** 连发时从指定蓄力进度重新生成瞄准实体（跳过完整蓄力，直接进入收尾阶段） */
	public void setCharge(int charge) {
		entityData.set(CHARGE, charge);
	}

	public int getFlash() {
		return entityData.get(FLASH);
	}

	public int getBeam() {
		return entityData.get(BEAM);
	}

	@Override
	public void tick() {
		LivingEntity owner = getOwner();
		if (!level().isClientSide()) {
			if (entityData.get(BEAM) == 1) {
				// 发射阶段：锁定发射时的位置（不再跟随目标），目标被激光击败也不中断，白光显示完再消失
				beamTicks++;
				if (beamTicks >= BEAM_TICKS) {
					discard();
					return;
				}
			} else {
				// 蓄力阶段
				if (owner == null || !owner.isAlive()) {
					discard();
					return;
				}
				int charge = entityData.get(CHARGE) + 1;
				entityData.set(CHARGE, charge);
				if (charge >= CHARGE_TICKS) {
					// 完成蓄力 → BEAM（锁定当前位置，下一 tick 起不再跟随）
					entityData.set(BEAM, 1);
					entityData.set(FLASH, 0);
					beamTicks = 0;
				} else {
					LivingEntity target = owner instanceof AbstractGolemEntity<?, ?> g ? g.getTarget() : null;
					int remaining = CHARGE_TICKS - charge;
					if (target == null || !target.isAlive()) {
						// 目标死亡：若已进入收尾（剩≤5刻，激光即将发射），仍完成 BEAM 锁定最后位置；否则丢弃
						if (remaining <= 5) {
							entityData.set(BEAM, 1);
							entityData.set(FLASH, 0);
							beamTicks = 0;
						} else {
							discard();
							return;
						}
					} else {
						// 瞄准实体跟随目标
						setPos(target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ());
						boolean flash = false;
						if (remaining > 5) {
							// 与音效同步：bit 播放的刻即为闪烁亮时刻（同一节拍公式）
							int interval = Math.max(1, 12 - charge * 11 / CHARGE_TICKS);
							if (charge >= nextBeat) {
								flash = true;
								nextBeat = charge + interval;
							}
						}
						entityData.set(FLASH, flash ? 1 : 0);
					}
				}
			}
		}
		super.tick();
	}

	@Override
	public @Nullable UUID getOwnerUUID() {
		return owner;
	}

	@Override
	public @Nullable LivingEntity getOwner() {
		if (ownerCache != null) return ownerCache;
		var ans = level().getEntity(entityData.get(OWNER_ID));
		if (ans instanceof LivingEntity le) {
			ownerCache = le;
		}
		return ownerCache;
	}

	@Override
	public boolean shouldRender(double p_20296_, double p_20297_, double p_20298_) {
		return true;
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double p_19883_) {
		return true;
	}
}
