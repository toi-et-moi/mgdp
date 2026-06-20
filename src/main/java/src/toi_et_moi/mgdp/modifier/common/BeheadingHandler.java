package src.toi_et_moi.mgdp.modifier.common;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import src.toi_et_moi.mgdp.Mgdp;

@Mod.EventBusSubscriber(modid = Mgdp.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BeheadingHandler {

	private static final ResourceLocation SLICING_AXE_ID = new ResourceLocation("modulargolems", "golem_slicing_axe");

	@SubscribeEvent
	public static void onDeath(LivingDeathEvent event) {
		if (event.getEntity().level().isClientSide) return;
		if (!(event.getSource().getEntity() instanceof AbstractGolemEntity<?, ?> golem)) return;

		ItemStack weapon = golem.getMainHandItem();
		if (weapon.isEmpty()) return;
		if (!weapon.is(ForgeRegistries.ITEMS.getValue(SLICING_AXE_ID))) return;

		LivingEntity dead = event.getEntity();
		ItemStack head = getHead(dead);
		if (!head.isEmpty()) {
			dead.spawnAtLocation(head);
		}
	}


	private static ItemStack getModHead(String id) {
		var rl = new ResourceLocation(id);
		var trophy = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(new ResourceLocation(id + "_trophy"));
		if (trophy != Items.AIR) return new ItemStack(trophy);
		var head = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(new ResourceLocation(id + "_head"));
		if (head != Items.AIR) return new ItemStack(head);
		var skull = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(new ResourceLocation(id + "_skull"));
		if (skull != Items.AIR) return new ItemStack(skull);
		return ItemStack.EMPTY;
	}

	private static ItemStack getHead(LivingEntity entity) {
		String mid = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString();
		if (entity.hasCustomName()) {
			ItemStack head = new ItemStack(Items.PLAYER_HEAD);
			head.getOrCreateTag().putString("SkullOwner", entity.getCustomName().getString());
			return head;
		}
		if (entity instanceof WitherSkeleton) {
			ItemStack mod = getModHead(mid);
			return mod.isEmpty() ? new ItemStack(Items.WITHER_SKELETON_SKULL) : mod;
		}
		if (entity instanceof Creeper) {
			ItemStack mod = getModHead(mid);
			return mod.isEmpty() ? new ItemStack(Items.CREEPER_HEAD) : mod;
		}
		if (entity instanceof Zombie) {
			ItemStack mod = getModHead(mid);
			return mod.isEmpty() ? new ItemStack(Items.ZOMBIE_HEAD) : mod;
		}
		if (entity instanceof Skeleton) {
			ItemStack mod = getModHead(mid);
			return mod.isEmpty() ? new ItemStack(Items.SKELETON_SKULL) : mod;
		}
		if (entity instanceof EnderDragon) {
			ItemStack mod = getModHead(mid);
			return mod.isEmpty() ? new ItemStack(Items.DRAGON_HEAD) : mod;
		}
		if (entity instanceof Player) {
			ItemStack playerHead = new ItemStack(Items.PLAYER_HEAD);
			playerHead.getOrCreateTag().putString("SkullOwner", entity.getName().getString());
			return playerHead;
		}
		ItemStack mod = getModHead(mid);
		return mod.isEmpty() ? ItemStack.EMPTY : mod;
	}
}
