package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import src.toi_et_moi.mgdp.init.IConquerorData;

@Mixin(AbstractGolemEntity.class)
public abstract class ConquerorDataMixin implements IConquerorData {

	private static final String TAG_VET_HP = "mgdp_vet_hp";
	private static final String TAG_VET_INVULN = "mgdp_vet_invuln";

	@Unique
	private static final EntityDataAccessor<Integer> mgdp$VET_STAR =
			SynchedEntityData.defineId(AbstractGolemEntity.class, EntityDataSerializers.INT);

	@Unique
	private double mgdp$vetTotalHp = 0;

	@Unique
	private int mgdp$vetInvulnUntil = 0;

	@Override
	public double mgdp$getVetHp() {
		return mgdp$vetTotalHp;
	}

	@Override
	public void mgdp$addVetHp(double hp) {
		mgdp$vetTotalHp += hp;
	}

	@Override
	public int mgdp$getVetStar() {
		return ((Entity) (Object) this).getEntityData().get(mgdp$VET_STAR);
	}

	@Override
	public void mgdp$setVetStar(int star) {
		((Entity) (Object) this).getEntityData().set(mgdp$VET_STAR, star);
	}

	@Override
	public int mgdp$getInvulnUntil() {
		return mgdp$vetInvulnUntil;
	}

	@Override
	public void mgdp$setInvulnUntil(int tick) {
		mgdp$vetInvulnUntil = tick;
	}

	@Inject(method = "defineSynchedData", at = @At("TAIL"))
	private void mgdp$defineVetData(CallbackInfo ci) {
		((Entity) (Object) this).getEntityData().define(mgdp$VET_STAR, 0);
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void mgdp$saveVetData(CompoundTag tag, CallbackInfo ci) {
		tag.putDouble(TAG_VET_HP, mgdp$vetTotalHp);
		tag.putInt(TAG_VET_INVULN, mgdp$vetInvulnUntil);
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void mgdp$loadVetData(CompoundTag tag, CallbackInfo ci) {
		mgdp$vetTotalHp = tag.getDouble(TAG_VET_HP);
		mgdp$vetInvulnUntil = tag.getInt(TAG_VET_INVULN);
	}
}
