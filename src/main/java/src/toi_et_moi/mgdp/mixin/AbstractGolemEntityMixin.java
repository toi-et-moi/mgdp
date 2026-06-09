package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.entity.goals.GolemSwimMoveControl;
import dev.xkmc.modulargolems.content.entity.mode.GolemModes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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

import src.toi_et_moi.mgdp.modifier.FlightPathNavigation;
import src.toi_et_moi.mgdp.jukebox.JukeboxGolem;

import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;

import java.util.List;
import java.util.function.Predicate;

import net.minecraft.client.Minecraft;

import src.toi_et_moi.mgdp.init.MGDPKeyMappings;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractGolemEntity.class)
public abstract class AbstractGolemEntityMixin extends Mob implements JukeboxGolem {

    protected AbstractGolemEntityMixin(EntityType<? extends Mob> type, Level level) {
        super(type, level);
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void mgdp$onAiStep(CallbackInfo ci) {
        AbstractGolemEntity<?, ?> golem = (AbstractGolemEntity<?, ?>) (Object) this;
        if (!golem.level().isClientSide && ModList.get().isLoaded("create")
                && golem.getMode() == GolemModes.STAND) {
            try {
                Class<?> cc = Class.forName("src.toi_et_moi.mgdp.compat.CreateCompat");
                cc.getMethod("tryDriveHandCrank", AbstractGolemEntity.class).invoke(null, golem);
            } catch (Exception ignored) {}
        }
    }

    private boolean mgdp$isFlying(AbstractGolemEntity<?, ?> golem) {
        return golem.getModifiers().containsKey(MGDPModifiers.FLIGHT.get())
                || golem.getModifiers().containsKey(MGDPModifiers.ROCKET_FLIGHT.get());
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void mgdp$flightTravel(Vec3 travelVector, CallbackInfo ci) {
        AbstractGolemEntity<?, ?> golem = (AbstractGolemEntity<?, ?>) (Object) this;
        if (!mgdp$isFlying(golem)) return;
        if (!golem.isMovable()) return;
        if (!golem.isEffectiveAi() && !golem.isControlledByLocalInstance()) return;
        LivingEntity rider = golem.isControlledByLocalInstance() && golem.isVehicle()
                ? (LivingEntity) golem.getControllingPassenger() : null;
        if (rider != null) {
            Minecraft mc = Minecraft.getInstance();
            float yya = 0;
            if (mc.options.keyJump.isDown()) yya = 1.0F;
            else if (MGDPKeyMappings.FLIGHT_DESCEND.isDown()) yya = -1.0F;
            travelVector = new Vec3(rider.xxa, yya, rider.zza);
        }
        float friction = 0.08F;
        if (rider != null && MGDPKeyMappings.FLIGHT_SPRINT.isDown()) {
            friction = 0.16F;
        }
        golem.moveRelative(friction, travelVector);
        golem.move(MoverType.SELF, golem.getDeltaMovement());
        golem.setDeltaMovement(golem.getDeltaMovement().multiply(0.91, 0.91, 0.91));
        ci.cancel();
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

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void mgdp$unstoppableLockSpeed(CallbackInfo ci) {
        if (!mgdp$hasUnstoppable()) return;
        var attr = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr == null || attr.getValue() >= attr.getBaseValue()) return;
        var toRemove = attr.getModifiers().stream()
                .filter(m -> m.getAmount() < 0)
                .map(AttributeModifier::getId)
                .toList();
        toRemove.forEach(attr::removeModifier);
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void mgdp$dodgeProjectiles(CallbackInfo ci) {
        try {
            AbstractGolemEntity<?, ?> golem = (AbstractGolemEntity<?, ?>) (Object) this;
            if (golem.level().isClientSide()) return;
            if (!golem.getModifiers().containsKey(src.toi_et_moi.mgdp.init.MGDPModifiers.PROJECTILE_DODGE.get())) return;
            Vec3 golemPos = golem.position();
            for (Entity e : golem.level().getEntitiesOfClass(Entity.class,
                    golem.getBoundingBox().inflate(10))) {
                if (e == golem) continue;
                if (e instanceof AbstractArrow && e.tickCount > 100) continue;
                Vec3 vel = e.getDeltaMovement();
                if (vel.lengthSqr() < (e instanceof Projectile ? 0.01 : 0.5)) continue;
                Entity owner = e instanceof Projectile ? ((Projectile)e).getOwner() : null;
                if (owner == golem || owner == golem.getOwner()) continue;
                Vec3 rel = golemPos.subtract(e.position());
                double t = rel.dot(vel) / vel.lengthSqr();
                if (t < 0 || t > 15) continue;
                Vec3 closest = e.position().add(vel.scale(t));
                if (closest.distanceToSqr(golemPos) > 9) continue;
                Vec3 dodge = vel.cross(new Vec3(0, 1, 0)).normalize();
                if (dodge.lengthSqr() < 0.5) dodge = new Vec3(1, 0, 0);
                float strength = t < 5 ? 1.0f : 0.5f;
                Vec3 newVel = golem.getDeltaMovement().add(dodge.scale(strength));
                if (Math.abs(newVel.x) < 100 && Math.abs(newVel.z) < 100) {
                    golem.setDeltaMovement(newVel);
                }
                break;
            }
        } catch (Exception ignored) {}
    }

    // --- Ride: allow golems to mount larger entities ---
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

    @Unique
    private ItemStack mgdp$jukeboxDisc = ItemStack.EMPTY;

    @Unique
    private int mgdp$jukeboxTick = 0;

    @Override
    public boolean mgdp$isPlaying() {
        return this.entityData.get(mgdp$JUKEBOX_PLAYING);
    }

    @Override
    public void mgdp$setPlaying(boolean playing) {
        this.entityData.set(mgdp$JUKEBOX_PLAYING, playing);
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
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void mgdp$saveJukeboxData(CompoundTag tag, CallbackInfo ci) {
        tag.put("mgdp_jukebox_disc", mgdp$jukeboxDisc.save(new CompoundTag()));
        tag.putBoolean("mgdp_jukebox_playing", mgdp$isPlaying());
        tag.putInt("mgdp_jukebox_tick", mgdp$jukeboxTick);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void mgdp$loadJukeboxData(CompoundTag tag, CallbackInfo ci) {
        AbstractGolemEntity<?, ?> golem = (AbstractGolemEntity<?, ?>) (Object) this;
        mgdp$jukeboxDisc = ItemStack.of(tag.getCompound("mgdp_jukebox_disc"));
        mgdp$setPlaying(tag.getBoolean("mgdp_jukebox_playing"));
        mgdp$jukeboxTick = 0;

        // Re-trigger sound if loaded while playing (e.g. golem retrieved and placed back)
        if (!golem.level().isClientSide && mgdp$isPlaying() && !mgdp$jukeboxDisc.isEmpty()
                && mgdp$jukeboxDisc.getItem() instanceof net.minecraft.world.item.RecordItem ri
                && golem.getOwnerUUID() != null && golem.getServer() != null) {
            var owner = golem.getServer().getPlayerList().getPlayer(golem.getOwnerUUID());
            if (owner != null) {
                src.toi_et_moi.mgdp.jukebox.JukeboxPacket.playRecordForPlayer(owner, ri.getSound().getLocation(), golem.getId());
            }
        }
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void mgdp$jukeboxTick(CallbackInfo ci) {
        AbstractGolemEntity<?, ?> golem = (AbstractGolemEntity<?, ?>) (Object) this;
        if (golem.level().isClientSide) return;

        // Auto-stop if disc removed while playing
        if (mgdp$isPlaying() && mgdp$jukeboxDisc.isEmpty()) {
            mgdp$setPlaying(false);
            mgdp$jukeboxTick = 0;
            golem.level().levelEvent(null, 1011, golem.blockPosition(), 0);
            return;
        }

        if (!mgdp$isPlaying()) return;
        mgdp$jukeboxTick++;

        // Safety auto-stop after ~5 minutes
        if (mgdp$jukeboxTick > 20 * 60 * 5) {
            mgdp$setPlaying(false);
            mgdp$jukeboxTick = 0;
            golem.level().levelEvent(null, 1011, golem.blockPosition(), 0);
        }
    }

    /** Make golem act as a boss entity when it has the Lord upgrade */
    public boolean isBoss() {
        AbstractGolemEntity<?, ?> golem = (AbstractGolemEntity<?, ?>) (Object) this;
        return golem.getModifiers().containsKey(src.toi_et_moi.mgdp.init.MGDPModifiers.LORD.get());
    }
}
