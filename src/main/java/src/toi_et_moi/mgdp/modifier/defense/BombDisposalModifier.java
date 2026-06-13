package src.toi_et_moi.mgdp.modifier.defense;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import src.toi_et_moi.mgdp.Mgdp;

import java.util.List;

@Mod.EventBusSubscriber(modid = Mgdp.MODID)
public class BombDisposalModifier extends GolemModifier {

	private static final int TNT_SCAN_INTERVAL = 10;
	private static final int TNT_RANGE = 35;

	public BombDisposalModifier() {
		super(dev.xkmc.modulargolems.content.core.StatFilterType.HEALTH, 1);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onExplosion(ExplosionEvent.Start event) {
		Explosion explosion = event.getExplosion();
		Level explosionLevel = event.getLevel();
		if (explosionLevel.isClientSide()) return;
		Vec3 center = explosion.getPosition();
		// Check if any golem with this modifier is nearby
		for (AbstractGolemEntity<?, ?> golem : explosionLevel.getEntitiesOfClass(AbstractGolemEntity.class,
				net.minecraft.world.phys.AABB.ofSize(center, 70, 70, 70))) {
			if (golem.getModifiers().containsKey(src.toi_et_moi.mgdp.init.MGDPModifiers.BOMB_DISPOSAL.get())) {
				event.setCanceled(true);
				return;
			}
		}
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		if (golem.tickCount % TNT_SCAN_INTERVAL != 0) return;

		for (Entity entity : golem.level().getEntitiesOfClass(Entity.class,
				golem.getBoundingBox().inflate(TNT_RANGE))) {
			if (entity == golem) continue;
			if (entity instanceof ItemEntity) continue;
			if (entity instanceof AbstractGolemEntity) continue;
			if (entity instanceof PrimedTnt) {
				// vanilla TNT, handled below
			} else {
				String sn = entity.getType().toShortString().toLowerCase();
				if (!sn.contains("tnt") && !sn.contains("primed") && !sn.contains("boom") && !sn.contains("explosive") && !sn.contains("nuke") && !sn.contains("bomb") && !sn.contains("fuse") && !sn.contains("creeper") && !sn.contains("blast") && !sn.contains("trap") && !sn.contains("meteor") && !sn.contains("mine") && !sn.contains("missile") && !sn.contains("hole") && !sn.contains("skull") && !sn.contains("crystal") && !sn.contains("rocket")) {
					try {
						entity.getClass().getDeclaredField("fuse");
					} catch (NoSuchFieldException e) {
						continue;
					}
				}
			}
			entity.discard();
			ItemStack drop = tryGetItem(entity);
			if (drop.isEmpty()) drop = new ItemStack(Items.TNT, 1);
			ItemEntity item = new ItemEntity(golem.level(), entity.getX(), entity.getY(), entity.getZ(), drop);
			golem.level().addFreshEntity(item);
		}
	}


	private static ItemStack tryGetItem(Entity entity) {
		net.minecraft.resources.ResourceLocation key = net.minecraft.world.entity.EntityType.getKey(entity.getType());
		if (key == null) return ItemStack.EMPTY;
		// Try original name first (some mods use same ID for entity and item)
		net.minecraft.resources.ResourceLocation itemKey = new net.minecraft.resources.ResourceLocation(key.getNamespace(), key.getPath().toLowerCase());
		var item = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(itemKey);
		if (item != null && item != net.minecraft.world.item.Items.AIR) {
			return new ItemStack(item);
		}
		// Try stripped name (remove primed_/fuse_ prefix)
		String stripped = key.getPath().toLowerCase()
				.replace("primed_", "").replace("primed", "").replace("fuse_", "");
		if (!stripped.equals(key.getPath().toLowerCase())) {
			net.minecraft.resources.ResourceLocation strippedKey = new net.minecraft.resources.ResourceLocation(key.getNamespace(), stripped);
			var strippedItem = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(strippedKey);
			if (strippedItem != null && strippedItem != net.minecraft.world.item.Items.AIR) {
				return new ItemStack(strippedItem);
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public List<MutableComponent> getDetail(int v) {
		return List.of(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GREEN));
	}
}
