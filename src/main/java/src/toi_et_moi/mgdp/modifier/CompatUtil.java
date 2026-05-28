package src.toi_et_moi.mgdp.modifier;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Field;

public class CompatUtil {

	private static MobEffect trueInvisibility;

	public static MobEffect getTrueInvisibility() {
		if (trueInvisibility != null) return trueInvisibility;
		if (!ModList.get().isLoaded("irons_spellbooks")) {
			trueInvisibility = MobEffects.INVISIBILITY;
			return trueInvisibility;
		}
		try {
			Class<?> clazz = Class.forName("io.redspace.ironsspellbooks.registries.MobEffectRegistry");
			Field field = clazz.getDeclaredField("TRUE_INVISIBILITY");
			Object registryObject = field.get(null);
			Object effect = registryObject.getClass().getMethod("get").invoke(registryObject);
			trueInvisibility = (MobEffect) effect;
		} catch (Exception e) {
			trueInvisibility = MobEffects.INVISIBILITY;
		}
		return trueInvisibility;
	}
}
