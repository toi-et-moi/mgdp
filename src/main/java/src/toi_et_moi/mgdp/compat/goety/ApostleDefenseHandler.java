package src.toi_et_moi.mgdp.compat.goety;

import dev.xkmc.modulargolems.compat.materials.goety.GoetyCompatRegistry;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import src.toi_et_moi.mgdp.Mgdp;

/**
 * 使徒晋升（modulargolems goety compat：apostle）扩展：
 * 傀儡获得使徒同款限伤（单次伤害上限 20），并在下界获得 50% 伤害抗性。
 */
@Mod.EventBusSubscriber(modid = Mgdp.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ApostleDefenseHandler {

	private static final ResourceLocation APOSTLE_MG = new ResourceLocation("modulargolems", "apostle");
	private static final ResourceLocation APOSTLE_GOETY = new ResourceLocation("goety", "apostle");

	private static final float APOSTLE_DAMAGE_CAP = 20.0F;
	private static final float NETHER_DAMAGE_REDUCTION = 0.5F;

	@SubscribeEvent
	public static void onGolemDamaged(LivingDamageEvent event) {
		if (!(event.getEntity() instanceof AbstractGolemEntity<?, ?> golem)) return;
		if (!hasApostle(golem)) return;
		float amount = event.getAmount();
		if (golem.level().dimension() == Level.NETHER) {
			amount *= NETHER_DAMAGE_REDUCTION;
		}
		if (!event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			amount = Math.min(amount, APOSTLE_DAMAGE_CAP);
		}
		event.setAmount(amount);
	}

	private static boolean hasApostle(AbstractGolemEntity<?, ?> golem) {
		if (ModList.get().isLoaded("goety")) {
			Integer lv = golem.getModifiers().get(GoetyCompatRegistry.APOSTLE.get());
			if (lv != null && lv > 0) return true;
		}
		for (var entry : golem.getModifiers().entrySet()) {
			ResourceLocation name = entry.getKey().getRegistryName();
			if (APOSTLE_MG.equals(name) || APOSTLE_GOETY.equals(name)) return true;
		}
		return false;
	}

}