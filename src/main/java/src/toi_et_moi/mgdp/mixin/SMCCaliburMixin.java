package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractGolemEntity.class)
public abstract class SMCCaliburMixin {

	@Unique private static net.minecraft.world.item.Item mgdp$caliburItem;
	@Unique private static Block mgdp$caliburBlock;
	@Unique private static boolean mgdp$smcCalChecked;

	@Inject(method = "aiStep", at = @At("TAIL"))
	private void mgdp$caliburCompat(CallbackInfo ci) {
		AbstractGolemEntity<?, ?> golem = (AbstractGolemEntity<?, ?>) (Object) this;
		if (golem.level().isClientSide()) return;

		if (!mgdp$smcCalChecked) {
			mgdp$smcCalChecked = true;
			mgdp$caliburItem = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(new ResourceLocation("smc", "calibur"));
			mgdp$caliburBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("smc", "calibur_block"));
		}
		if (mgdp$caliburItem == null) return;

		ItemStack hand = golem.getMainHandItem();

		// Suppress Calibur debuff for strong golems
		if (hand.is(mgdp$caliburItem)) {
			double atk = golem.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
			if (atk >= 30) {
				golem.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 4, false, false));
			}
		}

		// Dual-golem awakening: two golems (attack >= 30), one with Calibur + one empty-handed
		if (hand.is(mgdp$caliburItem) && golem.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) >= 30) {
			for (var other : golem.level().getEntitiesOfClass(AbstractGolemEntity.class,
					golem.getBoundingBox().inflate(3),
					g -> g != golem && g.isAlive() && g.getMainHandItem().isEmpty()
							)) {
				int paired = golem.getPersistentData().getInt("mgdp_calibur_paired");
				paired++;
				if (paired >= 200) {
					dualAwaken(golem, other);
				} else {
					golem.getPersistentData().putInt("mgdp_calibur_paired", paired);
					if (paired % 15 == 0 && golem.level() instanceof net.minecraft.server.level.ServerLevel sl) {
						double mx = (golem.getX() + other.getX()) / 2;
						double my = (golem.getY() + other.getY()) / 2 + 0.5;
						double mz = (golem.getZ() + other.getZ()) / 2;
						sl.sendParticles(new net.minecraft.core.particles.BlockParticleOption(
								net.minecraft.core.particles.ParticleTypes.BLOCK,
								net.minecraft.world.level.block.Blocks.STONE_SLAB.defaultBlockState()),
							mx, my, mz, 8, 0.4, 0.4, 0.4, 0.1);
						sl.playSound(null, mx, my, mz,
								net.minecraft.sounds.SoundEvents.STONE_BREAK,
								net.minecraft.sounds.SoundSource.BLOCKS, 0.6f, 1.0f);
					}
				}
				return;
			}
			golem.getPersistentData().putInt("mgdp_calibur_paired", 0);
		} else {
			golem.getPersistentData().putInt("mgdp_calibur_paired", 0);
		}

		// Pull Calibur from block (attack >= 30 + 5s countdown)
		if (mgdp$caliburBlock != null
				&& golem.getMainHandItem().isEmpty()
				&& golem.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) >= 30) {
			BlockPos pos = golem.blockPosition();
			BlockPos found = null;
			for (int dx = -2; dx <= 2; dx++)
				for (int dy = -2; dy <= 2; dy++)
					for (int dz = -2; dz <= 2; dz++) {
						BlockPos check = pos.offset(dx, dy, dz);
						if (golem.level().getBlockState(check).is(mgdp$caliburBlock))
							found = check;
					}
			if (found != null) {
				int pull = golem.getPersistentData().getInt("mgdp_calibur_pulling");
				pull++;
				if (pull >= 100) {
					pullCalibur(golem, found);
					golem.getPersistentData().putInt("mgdp_calibur_pulling", 0);
				} else {
					golem.getPersistentData().putInt("mgdp_calibur_pulling", pull);
					if (pull % 20 == 0 && golem.level() instanceof net.minecraft.server.level.ServerLevel sl) {
						sl.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,
								found.getX() + 0.5, found.getY() + 1, found.getZ() + 0.5,
								5, 0.3, 0.3, 0.3, 0.1);
						sl.playSound(null, found, net.minecraft.sounds.SoundEvents.STONE_HIT,
								net.minecraft.sounds.SoundSource.BLOCKS, 0.5f, 1.5f);
					}
				}
			} else {
				golem.getPersistentData().putInt("mgdp_calibur_pulling", 0);
			}
		} else {
			golem.getPersistentData().putInt("mgdp_calibur_pulling", 0);
		}
	}

	private void pullCalibur(AbstractGolemEntity<?, ?> golem, BlockPos pos) {
		golem.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(mgdp$caliburItem));
		caliburEffects(golem, pos);
		golem.level().destroyBlock(pos, false);
	}

	private void dualAwaken(AbstractGolemEntity<?, ?> holder, AbstractGolemEntity<?, ?> empty) {
		var excaliburItem = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(new ResourceLocation("smc", "excalibur"));
		if (excaliburItem != null)
			holder.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(excaliburItem));
		empty.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
				new ItemStack(net.minecraft.world.item.Items.STONE_SLAB));
		holder.getPersistentData().putInt("mgdp_calibur_paired", 0);

		caliburEffects(holder, holder.blockPosition());
	}

	private void caliburEffects(AbstractGolemEntity<?, ?> golem, BlockPos pos) {
		var bolt = new net.minecraft.world.entity.LightningBolt(
				net.minecraft.world.entity.EntityType.LIGHTNING_BOLT, golem.level());
		bolt.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
		bolt.setVisualOnly(true);
		golem.level().addFreshEntity(bolt);

		if (golem.level() instanceof net.minecraft.server.level.ServerLevel sl) {
			sl.sendParticles(net.minecraft.core.particles.ParticleTypes.WAX_OFF,
					pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
					15, 0.5, 0.5, 0.5, 0.1);
		}
		golem.level().playSound(null, pos, net.minecraft.sounds.SoundEvents.TOTEM_USE,
				net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
	}
}
