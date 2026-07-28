package src.toi_et_moi.mgdp.client;

import dev.xkmc.modulargolems.events.event.GolemInfoEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import src.toi_et_moi.mgdp.Mgdp;
import src.toi_et_moi.mgdp.init.IConquerorData;
import src.toi_et_moi.mgdp.init.IFlipData;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

@Mod.EventBusSubscriber(modid = Mgdp.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ShieldBlockOverlayHandler {

	@SubscribeEvent
	public static void onGolemInfo(GolemInfoEvent event) {
		var golem = event.getGolem();

		// 盾牌防御
		if (golem.getModifiers().containsKey(MGDPModifiers.SHIELD_BLOCK.get())
				&& golem instanceof IFlipData sbData) {
			int shields = sbData.mgdp$getSbShields();
			int hp = sbData.mgdp$getSbHp();
			int maxHp = Math.max(10, (int) (golem.getMaxHealth() * 0.05f));
			event.addLine(Component.translatable("modifier.modulargolems.shield_block.info", shields, hp, maxHp)
					.withStyle(ChatFormatting.GRAY));
		}

		// 充能护盾
		if (golem.getModifiers().containsKey(MGDPModifiers.CHARGED_SHIELD.get())
				&& golem instanceof IFlipData csData) {
			int totalShields = golem.getModifiers().get(MGDPModifiers.CHARGED_SHIELD.get()) * 5;
			int shields = csData.mgdp$getCsShields();
			event.addLine(Component.translatable("modifier.modulargolems.charged_shield.info", shields, totalShields)
					.withStyle(ChatFormatting.AQUA));
		}

		// 征服者星级
		if (golem.getModifiers().containsKey(MGDPModifiers.CONQUEROR.get())
				&& golem instanceof IConquerorData cvData) {
			int star = cvData.mgdp$getVetStar();
			if (star > 0) {
				event.addLine(Component.translatable("modifier.modulargolems.conqueror.info", star)
						.withStyle(ChatFormatting.GOLD));
			}
		}
	}
}
