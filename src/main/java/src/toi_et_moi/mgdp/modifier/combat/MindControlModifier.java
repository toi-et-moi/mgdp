package src.toi_et_moi.mgdp.modifier.combat;

import net.minecraft.core.registries.BuiltInRegistries;
import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import src.toi_et_moi.mgdp.Mgdp;

public class MindControlModifier extends GolemModifier {

	private static final String TAG_CD = "mgdp_ctrl_cd";

	public MindControlModifier() {
		super(StatFilterType.ATTACK, 2);
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		if (!(golem.getTarget() instanceof Mob target)) return;
		if (Player.class.isInstance(target)) return;
		if (target instanceof AbstractGolemEntity) return;
		if (level < 2 && isBoss(target)) return;
		if (!(golem.level() instanceof ServerLevel serverLevel)) return;

		var data = golem.getPersistentData();
		int lastCd = data.getInt(TAG_CD);
		if (lastCd > golem.tickCount) lastCd = 0;
		if (lastCd > 0 && golem.tickCount - lastCd < 100) return;
		if (golem.distanceToSqr(target) > 100) return;

		data.putInt(TAG_CD, golem.tickCount + 100);

		LivingEntity owner = golem.getOwner() != null ? golem.getOwner() : golem;

		// 1. Try converting to a servant variant (entity replacement, highest priority)
		if (ModList.get().isLoaded("goety")) {
			if (tryConvertServant(target, owner, serverLevel)) return;
		}

		// 2. Try IOwned.setTrueOwner directly (for Goety mobs that already implement it)
		if (ModList.get().isLoaded("goety")) {
			try {
				Class.forName("com.Polarice3.Goety.api.entities.IOwned")
						.getMethod("setTrueOwner", LivingEntity.class)
						.invoke(target, owner);
				return;
			} catch (Exception ignored) {
			}
		}

		// 3. Tame vanilla animals
		if (target instanceof TamableAnimal tamable) {
			if (owner instanceof Player player) {
				tamable.tame(player);
			}
			return;
		}
		if (target instanceof net.minecraft.world.entity.animal.horse.AbstractHorse horse) {
			if (owner != null) {
				horse.setTamed(true);
				horse.setOwnerUUID(owner.getUUID());
			}
		}
	}

	private static boolean tryConvertServant(Mob target, LivingEntity owner, ServerLevel level) {
		ResourceLocation regId = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());
		if (regId == null) return false;

		String path = regId.getPath();
		String ns = regId.getNamespace();

		EntityType<?> type = lookupServant(ns, path, true);
		if (type == null && path.startsWith("hostile_")) {
			String base = path.substring(8);
			type = lookupServant(ns, base, true);
			if (type == null) type = lookupServant(ns, base, false);
		}
		if (type == null) type = lookupServant("goety", path, false);
		if (type == null) return false;

		Mob servant = (Mob) type.create(level);
		if (servant == null) return false;

		servant.setPos(target.getX(), target.getY(), target.getZ());
		servant.setYRot(target.getYRot());
		servant.setXRot(target.getXRot());
		servant.setTarget(target.getTarget());
		for (var slot : net.minecraft.world.entity.EquipmentSlot.values()) {
			servant.setItemSlot(slot, target.getItemBySlot(slot).copy());
		}
		servant.setHealth(servant.getMaxHealth() * target.getHealth() / target.getMaxHealth());
		for (var effect : target.getActiveEffects()) {
			servant.addEffect(new net.minecraft.world.effect.MobEffectInstance(effect));
		}

		try {
			Class.forName("com.Polarice3.Goety.api.entities.IOwned")
					.getMethod("setTrueOwner", LivingEntity.class)
					.invoke(servant, owner);
		} catch (Exception e) {
			servant.discard();
			return false;
		}

		try {
			Mob converted = target.convertTo((EntityType<? extends Mob>) servant.getType(), true);
			if (converted != null) {
				try {
					Class.forName("com.Polarice3.Goety.api.entities.IOwned")
							.getMethod("setTrueOwner", LivingEntity.class)
							.invoke(converted, owner);
				} catch (Exception ignored) {
				}
				converted.setTarget(target.getTarget());
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private static EntityType<?> lookupServant(String namespace, String baseName, boolean suffix) {
		String id = suffix ? baseName + "_servant" : baseName;
		// Fast path: check the original namespace first (minecraft:zombie -> minecraft:zombie_servant = skip, goety:necromancer -> goety:necromancer_servant = hit)
		EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.containsKey(new ResourceLocation(namespace, id)) ? BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(namespace, id)) : null;
		if (type != null) return type;
		// Priority: same ns > exact goety: > goety addon > goety_ > others
		EntityType<?> fallbackAddon = null;
		EntityType<?> fallbackGoety = null;
		EntityType<?> fallbackAny = null;
		for (var entry : ForgeRegistries.ENTITY_TYPES.getEntries()) {
			if (entry.getKey().location().getPath().equals(id)) {
				String ns = entry.getKey().location().getNamespace();
				if (ns.equals(namespace)) continue;
				if (ns.equals("goety")) {
					type = entry.getValue();
					break;
				} else if (ns.contains("goety") || ns.contains("Goety")) {
					if (ns.startsWith("goety_") || ns.startsWith("Goety_")) {
						if (fallbackGoety == null) fallbackGoety = entry.getValue();
					} else {
						if (fallbackAddon == null) fallbackAddon = entry.getValue();
					}
				}
				if (fallbackAny == null) fallbackAny = entry.getValue();
			}
		}
		if (type == null) type = fallbackAddon;
		if (type == null) type = fallbackGoety;
		if (type == null) type = fallbackAny;
		return type;
	}

	private static boolean isBoss(Mob mob) {
		return mob.getType().is(net.minecraft.tags.TagKey.create(
				net.minecraft.core.registries.Registries.ENTITY_TYPE,
				new ResourceLocation("forge", "bosses")));
	}
}