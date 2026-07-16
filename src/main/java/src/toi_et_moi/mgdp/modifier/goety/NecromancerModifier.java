package src.toi_et_moi.mgdp.modifier.goety;

import net.minecraft.core.registries.BuiltInRegistries;
import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

public class NecromancerModifier extends GolemModifier {

	private static final String TAG_LAST = "mgdp_summon_tick";

	public NecromancerModifier() {
		super(StatFilterType.HEALTH, 5);
	}

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		if (golem.getTarget() == null) return;
		if (!(golem.level() instanceof ServerLevel serverLevel)) return;

		int last = golem.getPersistentData().getInt(TAG_LAST);
		if (last > golem.tickCount) last = 0;
		if (golem.tickCount - last < 20 * 5) return;

		int spawnCount = level <= 2 ? 2 : level <= 4 ? 4 : 6;

		for (int i = 0; i < spawnCount; i++) {
			Mob minion = createMinion(serverLevel, golem, level);
			if (minion != null) {
				BlockPos pos = golem.blockPosition().offset(
						golem.getRandom().nextInt(-3, 4), 0, golem.getRandom().nextInt(-3, 4));
				minion.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
				serverLevel.addFreshEntity(minion);
				try {
					net.minecraft.world.entity.LivingEntity owner = golem.getOwner();
					Object target = owner != null ? owner : golem;
					Class.forName("com.Polarice3.Goety.api.entities.IOwned")
							.getMethod("setTrueOwner", net.minecraft.world.entity.LivingEntity.class)
							.invoke(minion, target);
				} catch (Exception ignored) {}
				minion.setTarget(golem.getTarget());
				minion.getPersistentData().putBoolean("mgdp_necromancer_minion", true);
				try {
					Class.forName("com.Polarice3.Goety.api.entities.IOwned")
							.getMethod("setLimitedLife", int.class)
							.invoke(minion, 3000);
				} catch (Exception ignored) {}

			}
		}

		golem.getPersistentData().putInt(TAG_LAST, golem.tickCount);
	}


	private static Mob createMinion(ServerLevel level, AbstractGolemEntity<?, ?> golem, int lv) {
		BlockPos pos = golem.blockPosition();
		Biome biome = level.getBiome(pos).value();
		boolean nether = level.dimension() == net.minecraft.world.level.Level.NETHER;
			boolean thunder = level.isThundering() && level.canSeeSky(pos);
			boolean ocean = (level.getBiome(pos).is(net.minecraft.tags.BiomeTags.IS_OCEAN) || level.getBiome(pos).is(net.minecraft.tags.BiomeTags.IS_RIVER))
					|| golem.isInWater();
			boolean jungle = level.getBiome(pos).is(net.minecraft.tags.BiomeTags.IS_JUNGLE);
		boolean skele = golem.getRandom().nextBoolean();
		boolean crossbow = false;

		String id;
		if (nether) id = skele ? "goety:wither_skeleton_servant" : "goety:zpiglin_servant";
		else if (thunder) id = skele ? "goety:rattled_servant" : "goety:frayed_servant";
		else if (ocean) {
			id = skele ? "goety:sunken_skeleton_servant" : "goety:drowned_servant";
			crossbow = skele;
		} else if (jungle) id = skele ? "goety:mossy_skeleton_servant" : "goety:jungle_zombie_servant";
		else if (biome.coldEnoughToSnow(pos)) id = skele ? "goety:stray_servant" : "goety:frozen_zombie_servant";
		else if (biome.getBaseTemperature() > 1.0f) id = skele ? "goety:skeleton_servant" : "goety:husk_servant";
		else id = skele ? "goety:skeleton_servant" : "goety:zombie_servant";

		EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(id));
		if (type == null) return null;

		Mob mob = (Mob) type.create(level);
		if (mob == null) return null;

		mob.setCanPickUpLoot(false);
		equip(mob, lv, skele, nether, crossbow);
		return mob;
	}

	private static void equip(Mob mob, int lv, boolean skele, boolean nether, boolean crossbow) {
		switch (lv) {
			case 1 -> {
				mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
				mob.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.LEATHER_CHESTPLATE));
				mob.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.LEATHER_LEGGINGS));
				mob.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.LEATHER_BOOTS));
				if (crossbow) {
					mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
				} else {
					mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(skele ? Items.BOW : Items.WOODEN_AXE));
				}
				mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, -1, 0, false, false));
				mob.addEffect(new MobEffectInstance(ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("goety", "buff")), -1, 0, false, false));
			}
			case 2 -> {
				mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
				mob.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
				mob.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.CHAINMAIL_LEGGINGS));
				mob.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.CHAINMAIL_BOOTS));
				if (skele) {
					ItemStack bow = crossbow ? new ItemStack(Items.CROSSBOW) : new ItemStack(Items.BOW);
					bow.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", crossbow ? "quick_charge" : "power")), 2);
					mob.setItemSlot(EquipmentSlot.MAINHAND, bow);
				} else {
					mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_AXE));
				}
				mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, -1, 0, false, false));
				mob.addEffect(new MobEffectInstance(ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("goety", "buff")), -1, 1, false, false));
			}
			case 3 -> {
				ItemStack h3 = new ItemStack(Items.IRON_HELMET);
				h3.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "protection")), 3);
				ItemStack c3 = new ItemStack(Items.IRON_CHESTPLATE);
				c3.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "protection")), 3);
				ItemStack l3 = new ItemStack(Items.IRON_LEGGINGS);
				l3.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "protection")), 3);
				ItemStack b3 = new ItemStack(Items.IRON_BOOTS);
				b3.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "protection")), 3);
				mob.setItemSlot(EquipmentSlot.HEAD, h3); mob.setItemSlot(EquipmentSlot.CHEST, c3);
				mob.setItemSlot(EquipmentSlot.LEGS, l3); mob.setItemSlot(EquipmentSlot.FEET, b3);
				if (skele) {
					ItemStack bow = crossbow ? new ItemStack(Items.CROSSBOW) : new ItemStack(Items.BOW);
					bow.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", crossbow ? "quick_charge" : "power")), 3);
					if (!crossbow) {
						bow.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "punch")), 1);
					}
					mob.setItemSlot(EquipmentSlot.MAINHAND, bow);
				} else {
					ItemStack axe = new ItemStack(Items.IRON_AXE);
					axe.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "sharpness")), 2);
					mob.setItemSlot(EquipmentSlot.MAINHAND, axe);
				}
				mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, -1, 1, false, false));
				mob.addEffect(new MobEffectInstance(ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("goety", "buff")), -1, 2, false, false));
			}
			case 4 -> {
				ItemStack h4 = new ItemStack(Items.DIAMOND_HELMET);
				h4.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "protection")), 4);
				h4.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "unbreaking")), 3);
				ItemStack c4 = new ItemStack(Items.DIAMOND_CHESTPLATE);
				c4.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "protection")), 4);
				c4.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "unbreaking")), 3);
				c4.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "thorns")), 2);
				ItemStack l4 = new ItemStack(Items.DIAMOND_LEGGINGS);
				l4.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "protection")), 4);
				l4.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "unbreaking")), 3);
				ItemStack b4 = new ItemStack(Items.DIAMOND_BOOTS);
				b4.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "protection")), 4);
				b4.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "unbreaking")), 3);
				b4.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "thorns")), 2);
				mob.setItemSlot(EquipmentSlot.HEAD, h4); mob.setItemSlot(EquipmentSlot.CHEST, c4);
				mob.setItemSlot(EquipmentSlot.LEGS, l4); mob.setItemSlot(EquipmentSlot.FEET, b4);
				if (skele) {
					ItemStack bow = crossbow ? new ItemStack(Items.CROSSBOW) : new ItemStack(Items.BOW);
					bow.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", crossbow ? "quick_charge" : "power")), crossbow ? 3 : 5);
					if (!crossbow) {
						bow.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "punch")), 2);
						bow.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "flame")), 1);
					} else {
						bow.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "piercing")), 4);
					}
					mob.setItemSlot(EquipmentSlot.MAINHAND, bow);
				} else {
					ItemStack axe = new ItemStack(Items.DIAMOND_AXE);
					axe.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "sharpness")), 5);
					axe.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "unbreaking")), 3);
					mob.setItemSlot(EquipmentSlot.MAINHAND, axe);
				}
				mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, -1, 2, false, false));
				mob.addEffect(new MobEffectInstance(ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("goety", "buff")), -1, 3, false, false));
				mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, -1, 0, false, false));
			}
			case 5 -> {
				ItemStack h5 = new ItemStack(Items.NETHERITE_HELMET);
				h5.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "protection")), 5);
				h5.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "unbreaking")), 4);
				h5.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "thorns")), 3);
				ItemStack c5 = new ItemStack(Items.NETHERITE_CHESTPLATE);
				c5.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "protection")), 5);
				c5.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "unbreaking")), 4);
				c5.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "thorns")), 3);
				ItemStack l5 = new ItemStack(Items.NETHERITE_LEGGINGS);
				l5.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "protection")), 5);
				l5.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "unbreaking")), 4);
				ItemStack b5 = new ItemStack(Items.NETHERITE_BOOTS);
				b5.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "protection")), 5);
				b5.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "unbreaking")), 4);
				b5.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "thorns")), 3);
				mob.setItemSlot(EquipmentSlot.HEAD, h5); mob.setItemSlot(EquipmentSlot.CHEST, c5);
				mob.setItemSlot(EquipmentSlot.LEGS, l5); mob.setItemSlot(EquipmentSlot.FEET, b5);
				if (skele) {
					ItemStack bow = crossbow ? new ItemStack(Items.CROSSBOW) : new ItemStack(Items.BOW);
					bow.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", crossbow ? "quick_charge" : "power")), crossbow ? 4 : 6);
					if (!crossbow) {
						bow.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "punch")), 3);
						bow.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "flame")), 2);
					} else {
						bow.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "piercing")), 5);
					}
					mob.setItemSlot(EquipmentSlot.MAINHAND, bow);
				} else {
					ItemStack axe = new ItemStack(Items.NETHERITE_AXE);
					axe.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "sharpness")), 6);
					axe.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "unbreaking")), 4);
					axe.enchant(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("minecraft", "sweeping")), 3);
					mob.setItemSlot(EquipmentSlot.MAINHAND, axe);
				}
				mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, -1, 3, false, false));
				mob.addEffect(new MobEffectInstance(ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("goety", "buff")), -1, 4, false, false));
				mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, -1, 1, false, false));
				mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, -1, 0, false, false));
			}
		}
	}
}
