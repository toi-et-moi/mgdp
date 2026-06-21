package src.toi_et_moi.mgdp.modifier.combat;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DisarmModifier extends GolemModifier {

	public DisarmModifier() {
		super(StatFilterType.ATTACK, 5);
	}

	@Override
	public void onHurtTarget(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		if (golem.level().isClientSide()) return;

		LivingEntity target = event.getEntity();

		for (int i = 0; i < level; i++) {
			stripRandomItem(golem, target);
		}
	}

	private void stripRandomItem(AbstractGolemEntity<?, ?> golem, LivingEntity target) {
		List<EquipmentSlot> filledSlots = new ArrayList<>();
		for (var slot : EquipmentSlot.values()) {
			if (!target.getItemBySlot(slot).isEmpty()) {
				filledSlots.add(slot);
			}
		}

		List<CuriosEntry> curiosEntries = new ArrayList<>();
		if (ModList.get().isLoaded("curios") && target.isAlive()) {
			scanCurios(target, curiosEntries);
		}

		int total = filledSlots.size() + curiosEntries.size();
		if (total == 0) return;

		RandomSource random = target.getRandom();
		int pick = random.nextInt(total);

		ItemStack dropped;
		if (pick < filledSlots.size()) {
			EquipmentSlot slot = filledSlots.get(pick);
			dropped = target.getItemBySlot(slot).copy();
			target.setItemSlot(slot, ItemStack.EMPTY);
		} else {
			CuriosEntry entry = curiosEntries.get(pick - filledSlots.size());
			dropped = entry.stack.copy();
			removeCuriosItem(target, entry);
		}

		ItemEntity drop = new ItemEntity(golem.level(),
				golem.getX(), golem.getY() + golem.getBbHeight() * 0.5, golem.getZ(),
				dropped);
		drop.setPickUpDelay(10);
		golem.level().addFreshEntity(drop);
	}

	@SuppressWarnings("unchecked")
	private void scanCurios(LivingEntity target, List<CuriosEntry> result) {
		try {
			Class<?> apiClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");
			Method getHelper = apiClass.getMethod("getCuriosHelper");
			Object helper = getHelper.invoke(null);

			Method getHandler = helper.getClass().getMethod("getCuriosHandler", LivingEntity.class);
			Object opt = getHandler.invoke(helper, target);
			Method orElse = opt.getClass().getMethod("orElse", Object.class);
			Object handler = orElse.invoke(opt, (Object) null);
			if (handler == null) return;

			Method getCurios = handler.getClass().getMethod("getCurios");
			Map<String, Object> curiosMap = (Map<String, Object>) getCurios.invoke(handler);

			for (Map.Entry<String, Object> entry : curiosMap.entrySet()) {
				String identifier = entry.getKey();
				Object stacksHandler = entry.getValue();

				Method getStacks = stacksHandler.getClass().getMethod("getStacks");
				Object stacks = getStacks.invoke(stacksHandler);
				Method getStackInSlot = stacks.getClass().getMethod("getStackInSlot", int.class);
				Method getSlotsCount = stacks.getClass().getMethod("getSlots");

				int slots = (int) getSlotsCount.invoke(stacks);
				for (int s = 0; s < slots; s++) {
					ItemStack stack = (ItemStack) getStackInSlot.invoke(stacks, s);
					if (!stack.isEmpty()) {
						result.add(new CuriosEntry(identifier, s, stack));
					}
				}
			}
		} catch (Exception ignored) {
		}
	}

	private void removeCuriosItem(LivingEntity target, CuriosEntry entry) {
		try {
			Class<?> apiClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");
			Method getHelper = apiClass.getMethod("getCuriosHelper");
			Object helper = getHelper.invoke(null);

			Method getHandler = helper.getClass().getMethod("getCuriosHandler", LivingEntity.class);
			Object opt = getHandler.invoke(helper, target);
			Method orElse = opt.getClass().getMethod("orElse", Object.class);
			Object handler = orElse.invoke(opt, (Object) null);
			if (handler == null) return;

			Method getStacksHandler = handler.getClass().getMethod("getStacksHandler", String.class);
			Object optHandler = getStacksHandler.invoke(handler, entry.identifier);
			Method handlerOrElse = optHandler.getClass().getMethod("orElse", Object.class);
			Object stacksHandler = handlerOrElse.invoke(optHandler, (Object) null);
			if (stacksHandler == null) return;

		Method getStacks = stacksHandler.getClass().getMethod("getStacks");
			Object stacks = getStacks.invoke(stacksHandler);
			Method setStackInSlot = stacks.getClass().getMethod("setStackInSlot", int.class, ItemStack.class);
			setStackInSlot.invoke(stacks, entry.slotIndex, ItemStack.EMPTY);
		} catch (Exception ignored) {
		}
	}

	private record CuriosEntry(String identifier, int slotIndex, ItemStack stack) {
	}
}
