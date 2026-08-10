package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.entity.goals.GolemSwimMoveControl;
import dev.xkmc.modulargolems.content.entity.mode.GolemModes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import dev.xkmc.mob_weapon_api.api.simple.IHoldWeaponBehavior;
import dev.xkmc.mob_weapon_api.registry.WeaponRegistry;
import dev.xkmc.mob_weapon_api.registry.WeaponStatus;

import src.toi_et_moi.mgdp.modifier.FlightPathNavigation;
import src.toi_et_moi.mgdp.jukebox.JukeboxGolem;

import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;

import java.util.List;
import java.util.function.Predicate;


import src.toi_et_moi.mgdp.init.MGDPKeyMappings;
import src.toi_et_moi.mgdp.Config;
import src.toi_et_moi.mgdp.init.MGDPModifiers;
import src.toi_et_moi.mgdp.init.IFlipData;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractGolemEntity.class)
public abstract class AbstractGolemEntityMixin extends Mob implements JukeboxGolem, IFlipData {

    protected AbstractGolemEntityMixin(EntityType<? extends Mob> type, Level level) {
        super(type, level);
    }


    private boolean mgdp$isFlying(AbstractGolemEntity<?, ?> golem) {
        return golem.getModifiers().containsKey(MGDPModifiers.FLIGHT.get())
                || golem.getModifiers().containsKey(MGDPModifiers.ROCKET_FLIGHT.get());
    }

    // ---- Ranged strafe state (mirrors SmartRangedAttackGoal.strafing()) ----
    // We don't drive MoveControl.strafe directly (RangedGoalFlightMixin
    // redirects it to no-op for flying golems); instead we use these in
    // mgdp$aiFlightControl to produce a per-tick horizontal offset inside the
    // kite band.
    @Unique
    private int mgdp$strafeTime = 0;
    @Unique
    private boolean mgdp$strafeClockwise = false;
    @Unique
    private boolean mgdp$strafeBackwards = false;

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void mgdp$flightTravel(Vec3 travelVector, CallbackInfo ci) {
        AbstractGolemEntity<?, ?> golem = (AbstractGolemEntity<?, ?>) (Object) this;
        if (!mgdp$isFlying(golem)) return;
        if (!golem.isMovable()) return;
        if (!golem.isEffectiveAi() && !golem.isControlledByLocalInstance()) return;
        LivingEntity rider = golem.isControlledByLocalInstance() && golem.isVehicle()
                ? (LivingEntity) golem.getControllingPassenger() : null;
        float friction = 0.08F;
        if (rider != null) {
            float yya = 0;
            try {
                Class<?> cls = Class.forName("src.toi_et_moi.mgdp.client.ClientFlightInput");
                yya = (float) cls.getMethod("getVerticalInput").invoke(null);
            } catch (Exception ignored) {}
            travelVector = new Vec3(rider.xxa, yya, rider.zza);
        } else {
            // 🆕 AI 控距：无骑乘时由 mob_weapon_api 判定武器类型，自写完整控距
            Vec3 aiTravel = mgdp$aiFlightControl(golem);
            if (aiTravel.lengthSqr() > 0.0001) {
                travelVector = aiTravel;
                friction = 0.16F;  // 主动飞行加速
            }
        }
        if (rider != null) {
            try {
                Class<?> cls = Class.forName("src.toi_et_moi.mgdp.client.ClientFlightInput");
                if ((boolean) cls.getMethod("isSprinting").invoke(null)) {
                    friction = 0.16F;
                }
            } catch (Exception ignored) {}
        }
        golem.moveRelative(friction, travelVector);
        golem.move(MoverType.SELF, golem.getDeltaMovement());
        golem.setDeltaMovement(golem.getDeltaMovement().multiply(0.91, 0.91, 0.91));
        ci.cancel();
    }

    @Unique
    private static final double AI_HOVER_BAND = 0.15;
    @Unique
    private static final double AI_DEFAULT_RANGED = 10.0;
    @Unique
    private static final double AI_RANGED_SAFE_BUFFER = 4.0;
    @Unique
    private static final double AI_VERTICAL_GAIN = 0.25;
    @Unique
    private static final double AI_VERTICAL_MAX = 0.6;
    @Unique
    private static final double AI_VERTICAL_GAIN_RANGED = 0.15; // ranged: gentle, anti-wither
    @Unique
    private static final double AI_VERTICAL_MAX_RANGED = 0.3;

    /**
     * Self-contained AI flight control. Replaces what the parent mod's SWIM
     * upgrade would have given via the nav system: it only ever "chases" the
     * target and never backs off, so it has no ranged strafing and no
     * intelligent melee "stop on reach". This method does both, based on
     * the weapon type reported by mob_weapon_api's registries.
     *
     * Ranged: wither-style hover directly above the target at a distance of
     *         weapon-range blocks; horizontal position aligns to target XZ,
     *         vertical chases up gently but never chases down.
     * Melee:  fly to within reach, hover, never retreat; vertical chases up
     *         gently but never chases down (so finding a target on the
     *         ground does not pull a flying golem down to it).
     */
    @Unique
    private Vec3 mgdp$aiFlightControl(AbstractGolemEntity<?, ?> golem) {
        LivingEntity target = golem.getTarget();
        if (target == null || !target.isAlive()) return Vec3.ZERO;

        ItemStack mainHand = golem.getMainHandItem();
        WeaponStatus status = mgdp$weaponStatus(mainHand);
        if (status == null) return Vec3.ZERO;

        double reach = golem.getAttributeValue(ForgeMod.ENTITY_REACH.get());
        double meleeDist = reach + target.getBbWidth() * 0.5;

        double dx = target.getX() - golem.getX();
        double dy = target.getY() - golem.getY();
        double dz = target.getZ() - golem.getZ();
        double horizDist = Math.sqrt(dx * dx + dz * dz);

        Vec3 horizDir;
        double vertical;

        if (status.isRanged()) {
            // Ranged kiting, anti-wither-style.
            //  - Keep horizontal distance at idealH (well past melee reach).
            //  - Never chase vertical — vertical chase + a flying target (e.g.
            //    wither) makes both ascend forever.
            //  - If we ever end up directly above/below the target (horizDist ≈
            //    0), break the alignment by picking a random horizontal
            //    direction. Hovering directly above is exactly the wither
            //    behaviour we are trying to avoid.
            //  - Inside the kite band, do the SmartRangedAttackGoal-style
            //    strafe: side-step + occasional forward/back, with a 30%
            //    chance per 20-tick cycle to flip clockwise / backwards.
            //    This is safe for seeTime accumulation because the golem
            //    keeps looking at the target the whole time (lookAt is
            //    set below in the method), so canSee never flips to false
            //    and seeTime keeps ticking up.
            //
            //  v0.4.0 change: back up ONLY when the target is inside our own
            //  melee reach. Earlier the back-up fired at idealH * 0.85
            //  (~12 blocks), which reset seeTime in the parent
            //  SmartRangedAttackGoal on every step (canSee toggling sets
            //  seeTime = 0) and the bow/crossbow could never finish its pull.
            //  The whole band (idealH * 0.85 .. idealH * 1.15) now HOLDS
            //  position so seeTime accumulates. We only step back if the
            //  target has closed into our own melee range — otherwise we
            //  would just stand still while a wither walks up and slaps us.
            double idealH = Math.max(mgdp$rangedRange(golem, mainHand), meleeDist + AI_RANGED_SAFE_BUFFER);

            if (horizDist > idealH * (1.0 + AI_HOVER_BAND)) {
                // Too far — approach on the XZ plane.
                horizDir = new Vec3(dx, 0, dz).normalize();
            } else if (horizDist < meleeDist) {
                // Target has walked into our own melee reach — back up so we
                // don't eat a hit. We are still well within the band
                // geometry, but meleeDist << idealH so seeTime will still
                // reset and the bow will re-pull. That is the intended
                // trade-off: prefer being in range to losing a fight.
                horizDir = new Vec3(-dx, 0, -dz).normalize();
            } else if (horizDist < 0.5D) {
                // Directly above/below the target. dx/dz is zero, so a
                // normalise would NaN — break the vertical alignment with
                // a random horizontal direction so the bow can actually aim
                // (seeTime needs a non-trivial horizDist to stay > 0).
                double angle = golem.getRandom().nextDouble() * Math.PI * 2.0D;
                horizDir = new Vec3(Math.cos(angle), 0, Math.sin(angle));
            } else {
                // Inside the kite band — strafe like vanilla
                // SmartRangedAttackGoal.strafing(). Every 20 ticks there is
                // a 30% chance to flip clockwise and a 30% chance to flip
                // backwards, so the golem's strafe pattern wanders
                // realistically instead of being a fixed circle.
                mgdp$strafeTime++;
                if (mgdp$strafeTime >= 20) {
                    mgdp$strafeTime = 0;
                    if (golem.getRandom().nextFloat() < 0.3F) mgdp$strafeClockwise = !mgdp$strafeClockwise;
                    if (golem.getRandom().nextFloat() < 0.3F) mgdp$strafeBackwards = !mgdp$strafeBackwards;
                }
                float yawRad = golem.getYRot() * (float) (Math.PI / 180.0);
                double fx = -Mth.sin(yawRad);
                double fz =  Mth.cos(yawRad);
                // Right vector = yaw - 90 degrees
                double sx = -Mth.cos(yawRad);
                double sz = -Mth.sin(yawRad);
                float fwd = mgdp$strafeBackwards ? -0.5F : 0.5F;
                float side = mgdp$strafeClockwise ? 0.5F : -0.5F;
                horizDir = new Vec3(fx * fwd + sx * side, 0, fz * fwd + sz * side);
                // Normalise so the speed component fed to moveRelative is
                // unit-length regardless of how fwd/side combine.
                if (horizDir.lengthSqr() > 1e-6) horizDir = horizDir.normalize();
            }
            // No vertical control. Vertical chase is the bug that lets a
            // flying golem and a wither race upward forever.
            vertical = 0;
        } else {
            // Melee: fly to within reach, hover, do not retreat, do not chase down
            double idealDist = meleeDist;
            if (horizDist > idealDist * (1.0 + AI_HOVER_BAND)) {
                horizDir = new Vec3(dx, 0, dz).normalize();
            } else {
                horizDir = Vec3.ZERO; // in or below reach: hold position
            }
            if (dy > 0) {
                vertical = Mth.clamp(dy * AI_VERTICAL_GAIN, 0.1, AI_VERTICAL_MAX);
            } else {
                vertical = 0;
            }
        }

        // Face target
        golem.getLookControl().setLookAt(target, 30.0F, 30.0F);

        return new Vec3(horizDir.x, vertical, horizDir.z);
    }

    @Unique
    private WeaponStatus mgdp$weaponStatus(ItemStack stack) {
        if (stack.isEmpty()) return null;
        var s = WeaponRegistry.BOW.getProperties(stack);
        if (s.isPresent()) return s.get();
        s = WeaponRegistry.CROSSBOW.getProperties(stack);
        if (s.isPresent()) return s.get();
        s = WeaponRegistry.INSTANT.getProperties(stack);
        if (s.isPresent()) return s.get();
        s = WeaponRegistry.HOLD.getProperties(stack);
        if (s.isPresent()) return s.get();
        return null;
    }

    @Unique
    private double mgdp$rangedRange(AbstractGolemEntity<?, ?> golem, ItemStack stack) {
        // HOLD weapons expose range(LivingEntity, ItemStack) directly
        var hold = WeaponRegistry.HOLD.get(golem, stack);
        if (hold.isPresent()) return hold.get().range(golem, stack);
        // BOW / CROSSBOW / INSTANT: fall back to default
        return AI_DEFAULT_RANGED;
    }

    @Inject(method = "updateAttributes", at = @At("TAIL"), remap = false)
    private void mgdp$setFlightMoveControl(CallbackInfo ci) {
        AbstractGolemEntity<?, ?> golem = (AbstractGolemEntity<?, ?>) (Object) this;
        if (mgdp$isFlying(golem)) {
            this.moveControl = new GolemSwimMoveControl(golem);
            this.navigation = new FlightPathNavigation(golem, golem.level());
        }
        if (golem.getModifiers().containsKey(MGDPModifiers.UNSTOPPABLE.get())) {
            golem.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0);
        }
        boolean hasFlight = mgdp$isFlying(golem);
        boolean hasSpirit = golem.getModifiers().containsKey(MGDPModifiers.SPIRIT.get());
        ((EntityAccessor) this).setNoPhysics(hasFlight && hasSpirit);

        java.util.UUID diamondId = dev.xkmc.l2library.util.math.MathHelper.getUUIDFromString("mgdp_diamond_attack");
        java.util.UUID enchDiamondId = dev.xkmc.l2library.util.math.MathHelper.getUUIDFromString("mgdp_enchanted_diamond_attack");
        java.util.UUID crimsonId = dev.xkmc.l2library.util.math.MathHelper.getUUIDFromString("mgdp_crimson_attack");
        java.util.UUID enchCrimsonId = dev.xkmc.l2library.util.math.MathHelper.getUUIDFromString("mgdp_enchanted_crimson_attack");
        java.util.UUID crimsonArmorId = dev.xkmc.l2library.util.math.MathHelper.getUUIDFromString("mgdp_crimson_armor");
        java.util.UUID crimsonToughId = dev.xkmc.l2library.util.math.MathHelper.getUUIDFromString("mgdp_crimson_tough");
        var atk = this.getAttribute(Attributes.ATTACK_DAMAGE);
        var armor = this.getAttribute(Attributes.ARMOR);
        var tough = this.getAttribute(Attributes.ARMOR_TOUGHNESS);
        atk.removeModifier(diamondId);
        atk.removeModifier(enchDiamondId);
        atk.removeModifier(crimsonId);
        atk.removeModifier(enchCrimsonId);
        armor.removeModifier(crimsonArmorId);
        tough.removeModifier(crimsonToughId);
        if (golem.getModifiers().containsKey(MGDPModifiers.DIAMOND_ATTACK.get())) {
            atk.addPermanentModifier(new AttributeModifier(diamondId, "mgdp diamond attack", 0.3, AttributeModifier.Operation.MULTIPLY_BASE));
        }
        if (golem.getModifiers().containsKey(MGDPModifiers.ENCHANTED_DIAMOND_ATTACK.get())) {
            atk.addPermanentModifier(new AttributeModifier(enchDiamondId, "mgdp enchanted diamond attack", 0.6, AttributeModifier.Operation.MULTIPLY_BASE));
        }
        java.util.UUID rocketArmorId = dev.xkmc.l2library.util.math.MathHelper.getUUIDFromString("mgdp_rocket_armor");
        java.util.UUID rocketToughId = dev.xkmc.l2library.util.math.MathHelper.getUUIDFromString("mgdp_rocket_tough");
        armor.removeModifier(rocketArmorId);
        tough.removeModifier(rocketToughId);
        if (golem.getModifiers().containsKey(MGDPModifiers.ROCKET_FLIGHT.get())) {
            armor.addPermanentModifier(new AttributeModifier(rocketArmorId, "mgdp rocket armor", -1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
            tough.addPermanentModifier(new AttributeModifier(rocketToughId, "mgdp rocket tough", -1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        double armorVal = 0;
        double toughVal = 0;
        if (golem.getModifiers().containsKey(MGDPModifiers.CRIMSON_ATTACK.get())) {
            atk.addPermanentModifier(new AttributeModifier(crimsonId, "mgdp crimson attack", 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL));
            armorVal = -0.5;
            toughVal = -0.5;
        }
        if (golem.getModifiers().containsKey(MGDPModifiers.ENCHANTED_CRIMSON_ATTACK.get())) {
            atk.addPermanentModifier(new AttributeModifier(enchCrimsonId, "mgdp enchanted crimson attack", 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
            armorVal = -1.0;
            toughVal = -1.0;
        }
        if (armorVal != 0) {
            armor.addPermanentModifier(new AttributeModifier(crimsonArmorId, "mgdp crimson armor", armorVal, AttributeModifier.Operation.MULTIPLY_BASE));
            tough.addPermanentModifier(new AttributeModifier(crimsonToughId, "mgdp crimson tough", toughVal, AttributeModifier.Operation.MULTIPLY_BASE));
        }
        
    }

    @Inject(method = "canSwim", at = @At("RETURN"), cancellable = true, remap = false)
    private void mgdp$canSwimWithFlight(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            AbstractGolemEntity<?, ?> golem = (AbstractGolemEntity<?, ?>) (Object) this;
            if (mgdp$isFlying(golem)) {
                cir.setReturnValue(true);
            }
        }
    }

    // --- Unstoppable modifier mixins ---
    private boolean mgdp$hasUnstoppable() {
        AbstractGolemEntity<?, ?> golem = (AbstractGolemEntity<?, ?>) (Object) this;
        return golem.getModifiers().containsKey(MGDPModifiers.UNSTOPPABLE.get());
    }

    @Inject(method = "isPushable", at = @At("RETURN"), cancellable = true)
    private void mgdp$unstoppableNoPush(CallbackInfoReturnable<Boolean> cir) {
        if (mgdp$hasUnstoppable()) cir.setReturnValue(false);
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void mgdp$unstoppableImmune(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (mgdp$hasUnstoppable() && source.getEntity() == null
                && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "checkRide", at = @At("HEAD"), cancellable = true, remap = false)
    private void mgdp$checkRide(LivingEntity target, CallbackInfo ci) {
        if (target != null) {
            this.startRiding(target);
        }
        ci.cancel();
    }

    // ========== Jukebox Fields & Methods ==========

    @Unique
    private static final EntityDataAccessor<Boolean> mgdp$JUKEBOX_PLAYING =
            SynchedEntityData.defineId(AbstractGolemEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> mgdp$WINDMILL = SynchedEntityData.defineId(AbstractGolemEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> mgdp$FLIP_PROGRESS =
            SynchedEntityData.defineId(AbstractGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> mgdp$SB_SHIELDS =
            SynchedEntityData.defineId(AbstractGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> mgdp$SB_HP =
            SynchedEntityData.defineId(AbstractGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> mgdp$CS_SHIELDS =
            SynchedEntityData.defineId(AbstractGolemEntity.class, EntityDataSerializers.INT);

    @Unique
    private ItemStack mgdp$jukeboxDisc = ItemStack.EMPTY;

    @Unique
    private int mgdp$jukeboxTick = 0;

    @Override
    public boolean mgdp$isPlaying() {
        try {
            return this.entityData.get(mgdp$JUKEBOX_PLAYING);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void mgdp$setPlaying(boolean playing) {
        try {
            this.entityData.set(mgdp$JUKEBOX_PLAYING, playing);
        } catch (Exception ignored) {}
    }

    @Unique
    public int mgdp$getFlipProgress() {
        try {
            return this.entityData.get(mgdp$FLIP_PROGRESS);
        } catch (Exception e) {
            return 0;
        }
    }

    @Unique
    public void mgdp$setFlipProgress(int progress) {
        try {
            this.entityData.set(mgdp$FLIP_PROGRESS, progress);
        } catch (Exception ignored) {}
    }

    @Unique
    public float mgdp$getWindmill() {
        try {
            return this.entityData.get(mgdp$WINDMILL);
        } catch (Exception e) {
            return 0;
        }
    }

    @Unique
    public void mgdp$setWindmill(float angle) {
        try {
            this.entityData.set(mgdp$WINDMILL, angle);
        } catch (Exception ignored) {}
    }
	@Unique
	public int mgdp$getSbShields() {
		try {
			return this.entityData.get(mgdp$SB_SHIELDS);
		} catch (Exception e) {
			return 0;
		}
	}

	@Unique
	public void mgdp$setSbShields(int shields) {
		try {
			this.entityData.set(mgdp$SB_SHIELDS, shields);
		} catch (Exception ignored) {}
	}

	@Unique
	public int mgdp$getSbHp() {
		try {
			return this.entityData.get(mgdp$SB_HP);
		} catch (Exception e) {
			return 0;
		}
	}

	@Unique
	public void mgdp$setSbHp(int hp) {
		try {
			this.entityData.set(mgdp$SB_HP, hp);
		} catch (Exception ignored) {}
	}
	@Unique
	public int mgdp$getCsShields() {
		try {
			return this.entityData.get(mgdp$CS_SHIELDS);
		} catch (Exception e) {
			return 0;
		}
	}

	@Unique
	public void mgdp$setCsShields(int shields) {
		try {
			this.entityData.set(mgdp$CS_SHIELDS, shields);
		} catch (Exception ignored) {}
	}

    @Override
    public ItemStack mgdp$getDisc() {
        return mgdp$jukeboxDisc;
    }

    @Override
    public void mgdp$setDisc(ItemStack stack) {
        this.mgdp$jukeboxDisc = stack.copy();
    }

    @Override
    public int mgdp$getTick() {
        return mgdp$jukeboxTick;
    }

    @Override
    public void mgdp$setTick(int tick) {
        this.mgdp$jukeboxTick = tick;
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void mgdp$defineJukeboxData(CallbackInfo ci) {
        this.entityData.define(mgdp$JUKEBOX_PLAYING, false);
        this.entityData.define(mgdp$FLIP_PROGRESS, 0);
		this.entityData.define(mgdp$WINDMILL, 0.0F);
		this.entityData.define(mgdp$SB_SHIELDS, 0);
		this.entityData.define(mgdp$SB_HP, 0);
		this.entityData.define(mgdp$CS_SHIELDS, 0);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void mgdp$saveJukeboxData(CompoundTag tag, CallbackInfo ci) {
        tag.put("mgdp_jukebox_disc", mgdp$jukeboxDisc.save(new CompoundTag()));
        tag.putBoolean("mgdp_jukebox_playing", mgdp$isPlaying());
        tag.putInt("mgdp_jukebox_tick", mgdp$jukeboxTick);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void mgdp$loadJukeboxData(CompoundTag tag, CallbackInfo ci) {
        mgdp$jukeboxDisc = ItemStack.of(tag.getCompound("mgdp_jukebox_disc"));
        mgdp$setPlaying(mgdp$jukeboxDisc.isEmpty() ? false : tag.getBoolean("mgdp_jukebox_playing"));
        mgdp$jukeboxTick = 0;
	}

	// When golem clears its target, also clear the auto-aggro cooldown for that mob
	@Inject(method = "setTarget", at = @At("HEAD"))
	private void mgdp$clearAggroOnLoseTarget(LivingEntity target, CallbackInfo ci) {
		if (target != null) return;
		AbstractGolemEntity<?, ?> golem = (AbstractGolemEntity<?, ?>) (Object) this;
		LivingEntity old = golem.getTarget();
		if (old != null) aaggroLast.remove(old.getUUID());
	}

	// === Auto-aggro per-mob cooldown: each mob can be re-targeted at most once per 100 ticks ===
	private static final java.util.Map<java.util.UUID, Long> aaggroLast = new java.util.HashMap<>();
	private static int aaggroCleanTick;

	@Redirect(method = "setTarget",
			  at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;setTarget(Lnet/minecraft/world/entity/LivingEntity;)V"))
	private void mgdp$redirectAutoAggro(Mob mob, LivingEntity target) {
		if (!Config.mobAutoAggro) return;

		long now = this.level().getGameTime();
		Long last = aaggroLast.get(mob.getUUID());
		if (last != null && now - last < 100) return;

		aaggroLast.put(mob.getUUID(), now);

		// Periodically purge entries older than 10s (200 ticks)
		if (++aaggroCleanTick % 100 == 0) {
			aaggroLast.values().removeIf(v -> now - v > 200);
		}

		mob.setTarget(target);
	}
    }

