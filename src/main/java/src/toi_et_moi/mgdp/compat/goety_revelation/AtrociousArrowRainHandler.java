package src.toi_et_moi.mgdp.compat.goety_revelation;

import dev.xkmc.modulargolems.compat.materials.goety.revelation.GRCompatRegistry;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import src.toi_et_moi.mgdp.Mgdp;

/**
 * 十恶不赦（modulargolems goety_revelation 使徒头衔：the_atrocious）扩展：
 * 周期在目标头顶召唤箭雨；傀儡手持天启长弓（goety_revelation:bow_of_revelation）时改为死亡箭雨。
 */
@Mod.EventBusSubscriber(modid = Mgdp.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AtrociousArrowRainHandler {

	private static final ResourceLocation ATROCIOUS_MG = new ResourceLocation("modulargolems", "the_atrocious");
	private static final ResourceLocation ATROCIOUS_GR = new ResourceLocation("goety_revelation", "the_atrocious");
	private static final ResourceLocation REVELATION_BOW = new ResourceLocation("goety_revelation", "bow_of_revelation");
	private static final ResourceLocation GENESIS_MG = new ResourceLocation("modulargolems", "the_genesis");
	private static final ResourceLocation GENESIS_MDP = new ResourceLocation("mgdp", "the_genesis");

	private static final int INTERVAL = 100;

	@SubscribeEvent
	public static void onGolemTick(LivingEvent.LivingTickEvent event) {
		if (!(event.getEntity() instanceof AbstractGolemEntity<?, ?> golem)) return;
		if (golem.level().isClientSide) return;
		int lv = getAtrociousLevel(golem);
		if (lv <= 0) return;
		if (golem.tickCount % INTERVAL != 0) return;
		LivingEntity target = golem.getTarget();
		if (target == null || !target.isAlive() || !golem.canAttack(target)) return;
		summonArrowRain(golem, target, lv);
	}

	private static void summonArrowRain(AbstractGolemEntity<?, ?> golem, LivingEntity target, int lv) {
		Level level = golem.level();
		if (level.isClientSide) return;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(target.getX(), target.getY() + 1.5D, target.getZ());
		int maxHeight = 15;
		int i = 0;
		while (pos.getY() < level.getMaxBuildHeight() && level.isEmptyBlock(pos) && i < maxHeight) {
			pos.move(0, 1, 0);
			i++;
		}
		boolean death = hasGenesis(golem) || holdsRevelationBow(golem);
		int arrows = death ? 18 + lv * 6 : 12 + lv * 6;
		float baseDamage = Math.max(10.0F, (float) golem.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.5F);
		String entityId = death ? "death_arrow" : "rain_arrow";
		EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation("goety", entityId));
		if (type == null) return;
		try {
			String className = death ? "com.Polarice3.Goety.common.entities.projectiles.DeathArrow"
					: "com.Polarice3.Goety.common.entities.projectiles.RainArrow";
			var ctor = Class.forName(className).getConstructor(EntityType.class, Level.class);
			for (int j = 0; j < arrows; j++) {
				AbstractArrow arrow = (AbstractArrow) ctor.newInstance(type, level);
				arrow.setBaseDamage(baseDamage);
				arrow.setEnchantmentEffectsFromEntity(golem, baseDamage);
				Vec3 spawn = pos.getCenter().add(
						level.random.nextFloat() * 8.0F - 4.0F,
						level.random.nextFloat() * 2.0F - 1.0F,
						level.random.nextFloat() * 8.0F - 4.0F);
				arrow.setPos(spawn);
				arrow.setOwner(golem);
				Vec3 vec = target.position().add(0, target.getBbHeight() * 0.5D, 0).subtract(spawn);
				arrow.shoot(vec.x, vec.y, vec.z, 1.5F + level.random.nextFloat(), 10.0F);
				level.addFreshEntity(arrow);
			}
		} catch (Exception e) {
			Mgdp.LOGGER.warn("Atrocious arrow rain failed", e);
		}
	}

	private static boolean holdsRevelationBow(AbstractGolemEntity<?, ?> golem) {
		if (!ModList.get().isLoaded("goety_revelation")) return false;
		Item bow = ForgeRegistries.ITEMS.getValue(REVELATION_BOW);
		if (bow == null) return false;
		for (InteractionHand hand : InteractionHand.values()) {
			if (golem.getItemInHand(hand).is(bow)) return true;
		}
		return false;
	}

	private static boolean hasGenesis(AbstractGolemEntity<?, ?> golem) {
		if (golem.getModifiers().containsKey(src.toi_et_moi.mgdp.init.MGDPModifiers.THE_GENESIS.get())) {
			return true;
		}
		for (var entry : golem.getModifiers().entrySet()) {
			ResourceLocation name = entry.getKey().getRegistryName();
			if (GENESIS_MG.equals(name) || GENESIS_MDP.equals(name)) return true;
		}
		return false;
	}

	private static int getAtrociousLevel(AbstractGolemEntity<?, ?> golem) {
		if (ModList.get().isLoaded("goety_revelation")) {
			Integer lv = golem.getModifiers().get(GRCompatRegistry.FAST.get());
			if (lv != null && lv > 0) return lv;
		}
		for (var entry : golem.getModifiers().entrySet()) {
			ResourceLocation name = entry.getKey().getRegistryName();
			if (ATROCIOUS_MG.equals(name) || ATROCIOUS_GR.equals(name)) return entry.getValue();
		}
		return 0;
	}

}