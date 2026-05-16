package src.toi_et_moi.mgdp.compat;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class CreateCompat {

	private static final int SCAN_INTERVAL = 4;
	private static final int BASE_RANGE = 2;

	private static final ResourceLocation HAND_CRANK_ID = new ResourceLocation("create", "hand_crank");
	private static final ResourceLocation PUSH_MODIFIER_ID = new ResourceLocation("modulargolems", "push");
	private static final ResourceLocation FORCE_MODIFIER_ID = new ResourceLocation("modulargolems", "mechanical_force");
	private static final ResourceLocation MOBILITY_MODIFIER_ID = new ResourceLocation("modulargolems", "mechanical_mobility");
	private static final ResourceLocation FORCE_EFFECT_ID = new ResourceLocation("modulargolems", "mechanical_force");
	private static final ResourceLocation MOBILITY_EFFECT_ID = new ResourceLocation("modulargolems", "mechanical_mobility");

	public static void tryDriveHandCrank(AbstractGolemEntity<?, ?> golem) {
		if (golem.tickCount % SCAN_INTERVAL != 0) return;

		int extraRange = getModifierLevel(golem, MOBILITY_MODIFIER_ID)
				+ getEffectLevel(golem, MOBILITY_EFFECT_ID);
		int range = BASE_RANGE + extraRange;

		int forceModLevel = getModifierLevel(golem, FORCE_MODIFIER_ID);
		int forceEffLevel = getEffectLevel(golem, FORCE_EFFECT_ID);
		float stressMultiplier = 1f + forceModLevel + forceEffLevel;

		// Mechanical Arm (push) upgrade triples stress output
		for (GolemModifier mod : golem.getModifiers().keySet()) {
			if (mod.getRegistryName().equals(PUSH_MODIFIER_ID)) {
				stressMultiplier *= 3;
				break;
			}
		}

		BlockPos center = golem.blockPosition();
		for (int dx = -range; dx <= range; dx++) {
			for (int dy = -1; dy <= 2; dy++) {
				for (int dz = -range; dz <= range; dz++) {
					BlockPos pos = center.offset(dx, dy, dz);
					BlockState state = golem.level().getBlockState(pos);
					if (ForgeRegistries.BLOCKS.getKey(state.getBlock()).equals(HAND_CRANK_ID)) {
						drive(golem, golem.level().getBlockEntity(pos), stressMultiplier);
					}
				}
			}
		}
	}

	private static void drive(AbstractGolemEntity<?, ?> golem, BlockEntity be, float stressMultiplier) {
		if (be == null) return;
		try {
			Field backwardsField = findField(be.getClass(), "backwards");
			boolean backwards = backwardsField.getBoolean(be);

			Method turn = be.getClass().getMethod("turn", boolean.class);
			turn.invoke(be, backwards);

			// Override stress capacity: 256 SU per 1 base attack
			// (player has 1 base attack → hand crank provides 256 SU)
			// KineticBlockEntity.capacity is per-RPM stress:
			//   capacity = attack * 256 / 32 = attack * 8
			float attack = (float) golem.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
			float capacity = attack * 8f * stressMultiplier;

			Field capacityField = findField(be.getClass(), "capacity");
			capacityField.setFloat(be, capacity);

			Method notify = findMethod(be.getClass(), "notifyStressCapacityChange");
			if (notify != null) {
				notify.setAccessible(true);
				notify.invoke(be, capacity);
			}
		} catch (Exception ignored) {
		}
	}

	private static int getModifierLevel(AbstractGolemEntity<?, ?> golem, ResourceLocation id) {
		for (var entry : golem.getModifiers().entrySet()) {
			if (entry.getKey().getRegistryName().equals(id)) {
				return entry.getValue();
			}
		}
		return 0;
	}

	private static int getEffectLevel(AbstractGolemEntity<?, ?> golem, ResourceLocation id) {
		MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(id);
		if (effect == null) return 0;
		MobEffectInstance instance = golem.getEffect(effect);
		return instance != null ? instance.getAmplifier() + 1 : 0;
	}

	private static Field findField(Class<?> clazz, String name) throws NoSuchFieldException {
		Class<?> current = clazz;
		while (current != null && current != Object.class) {
			try {
				Field field = current.getDeclaredField(name);
				field.setAccessible(true);
				return field;
			} catch (NoSuchFieldException e) {
				current = current.getSuperclass();
			}
		}
		throw new NoSuchFieldException(name);
	}

	private static Method findMethod(Class<?> clazz, String name) {
		Class<?> current = clazz;
		while (current != null && current != Object.class) {
			for (Method m : current.getDeclaredMethods()) {
				if (m.getName().equals(name)) {
					return m;
				}
			}
			current = current.getSuperclass();
		}
		return null;
	}
}
