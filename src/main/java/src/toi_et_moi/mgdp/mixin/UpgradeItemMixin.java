package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.modulargolems.content.core.GolemType;
import dev.xkmc.modulargolems.content.item.upgrade.UpgradeItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = UpgradeItem.class)
public abstract class UpgradeItemMixin {

	@Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true)
	private void mgdp$nullCheckTooltip(ItemStack stack, Level level, List<Component> list, TooltipFlag flag, CallbackInfo ci) {
		for (var ins : ((UpgradeItem) (Object) this).get()) {
			if (ins.mod() == null) {
				ci.cancel();
				return;
			}
		}
	}

	@Inject(method = "fitsOn", at = @At("HEAD"), cancellable = true, remap = false)
	private void mgdp$nullCheckFitsOn(GolemType<?, ?> type, CallbackInfoReturnable<Boolean> cir) {
		for (var ins : ((UpgradeItem) (Object) this).get()) {
			if (ins.mod() == null) {
				cir.setReturnValue(false);
				return;
			}
		}
	}
}
