package src.toi_et_moi.mgdp.init;

import dev.xkmc.modulargolems.content.client.armor.GolemModelPaths;
import dev.xkmc.modulargolems.content.entity.metalgolem.MetalGolemEntity;
import dev.xkmc.modulargolems.content.item.ranged.CannonPoseUtil;
import dev.xkmc.modulargolems.content.item.ranged.IShoulderCannonAnimated;
import dev.xkmc.modulargolems.content.item.ranged.ShouldWeaponItem;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import src.toi_et_moi.mgdp.Mgdp;
import src.toi_et_moi.mgdp.entity.MourningBeamEntity;

public class BlackMourningItem extends ShouldWeaponItem implements IShoulderCannonAnimated {

	public BlackMourningItem(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable Level level, java.util.List<Component> list, TooltipFlag flag) {
		super.appendHoverText(stack, level, list, flag);
		list.add(Component.translatable("item.mgdp.black_mourning.desc").withStyle(ChatFormatting.LIGHT_PURPLE));
	}

	@Override
	public void onTick(MetalGolemEntity e, ItemStack stack, InteractionHand hand) {
		if (e.tickCount % 60 == (hand == InteractionHand.MAIN_HAND ? 20 : 50) &&
				!e.level().isClientSide() && e.getTarget() != null && e.getTarget().isAlive()) {
			if (CannonPoseUtil.BEACON.isOutOfRange(e, hand)) return;

			var laser = new MourningBeamEntity(Mgdp.MOURNING_BEAM.get(), e.level(), e, 10, hand == InteractionHand.MAIN_HAND);
			e.level().addFreshEntity(laser);
			if (!e.isSilent())
				e.level().playSound(null, e.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.NEUTRAL, 2, 0.5f);
		}
	}

	@Override
	public ResourceLocation getModelForHand(InteractionHand hand) {
		return hand == InteractionHand.MAIN_HAND
				? GolemModelPaths.BEACON_RIGHT
				: GolemModelPaths.BEACON_LEFT;
	}
}
