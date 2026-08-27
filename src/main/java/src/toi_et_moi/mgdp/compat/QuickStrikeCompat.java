package src.toi_et_moi.mgdp.compat;

import dev.xkmc.mob_weapon_api.registry.WeaponRegistry;
import dev.xkmc.mob_weapon_api.registry.WeaponStatus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import src.toi_et_moi.mgdp.Mgdp;

import java.util.Optional;
import java.util.Set;

/**
 * 把 QuickStrike 的弓/弩兼容从 mixin 迁移到 mob_weapon_api 官方行为注册：
 * - 通用弓（普通 BowItem）→ QuickStrikeBowBehavior（秒拉弓 + 满力 + 无限弹药）
 * - 通用弩（CrossbowItem）→ QuickStrikeCrossbowBehavior（秒装填 + 高速连发弹幕）
 * 专属弓（MetalGolemBowItem / L2Archery / SMC / Revelation）在 predicate 里排除，
 * 继续由各自已注册的行为处理，避免被通用行为抢占。
 */
@Mod.EventBusSubscriber(modid = Mgdp.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class QuickStrikeCompat {

	private static final Set<String> EXCLUSIVE_BOW_IDS = Set.of(
			"smc:rainbow_bow", "smc:frostium_bow", "smc:perfrostite_bow",
			"goety_revelation:bow_of_revelation");

	@SubscribeEvent
	public static void onCommonSetup(FMLCommonSetupEvent event) {
		WeaponRegistry.BOW.register(new ResourceLocation(Mgdp.MODID, "quick_strike_bow"),
				QuickStrikeCompat::isGenericBow,
				(user, stack) -> new QuickStrikeBowBehavior(), 5);
		WeaponRegistry.CROSSBOW.register(new ResourceLocation(Mgdp.MODID, "quick_strike_crossbow"),
				stack -> WeaponStatus.RANGED.of(stack.getItem() instanceof CrossbowItem),
				(user, stack) -> new QuickStrikeCrossbowBehavior(), 5);
	}

	private static Optional<WeaponStatus> isGenericBow(ItemStack stack) {
		if (!(stack.getItem() instanceof BowItem)) return WeaponStatus.RANGED.of(false);
		String cls = stack.getItem().getClass().getName();
		// MetalGolemBowItem（modulargolems 专属机械弓）走 golem_bow 行为，不能抢
		if (cls.equals("dev.xkmc.modulargolems.content.item.ranged.MetalGolemBowItem"))
			return WeaponStatus.RANGED.of(false);
		// L2Archery 弓走 L2BowBehavior，保留莱特兰弓艺完整逻辑
		if (cls.startsWith("dev.xkmc.l2archery.")) return WeaponStatus.RANGED.of(false);
		// SMC / Revelation 专属弓由各自行为处理
		ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
		if (id != null && EXCLUSIVE_BOW_IDS.contains(id.toString())) return WeaponStatus.RANGED.of(false);
		return WeaponStatus.RANGED.of(true);
	}
}
