package src.toi_et_moi.mgdp.compat.goety_revelation;

import dev.xkmc.modulargolems.compat.materials.goety.revelation.GRCompatRegistry;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import src.toi_et_moi.mgdp.Mgdp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 天启饥荒（modulargolems goety_revelation 使徒头衔：The Profane / the_profane）在攻击时删除目标正面效果。
 * 女巫之王（MGDP the_witch_king）需要同时拥有天启饥荒时，才能窃取这些被删除的效果。
 *
 * <p>利用事件优先级捕获 modulargolems 在 HIGH 优先级删掉的效果：
 * HIGHEST 记录攻击前目标正面效果快照，LOWEST 对比出被删的效果并缓存，
 * 供 onHurtTarget（伤害结算）中的女巫之王读取。
 */
@Mod.EventBusSubscriber(modid = Mgdp.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ProfaneStealHandler {

	private static final Map<UUID, List<MobEffectInstance>> BEFORE = new HashMap<>();
	private static final Map<UUID, List<MobEffectInstance>> REMOVED = new HashMap<>();

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onAttackPre(LivingAttackEvent event) {
		if (!(event.getSource().getEntity() instanceof AbstractGolemEntity<?, ?> golem)) return;
		if (!hasProfane(golem)) return;
		LivingEntity target = event.getEntity();
		List<MobEffectInstance> snapshot = new ArrayList<>();
		for (MobEffectInstance inst : target.getActiveEffects()) {
			if (inst.getEffect().isBeneficial()) snapshot.add(inst);
		}
		BEFORE.put(target.getUUID(), snapshot);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onAttackPost(LivingAttackEvent event) {
		if (!(event.getSource().getEntity() instanceof AbstractGolemEntity<?, ?> golem)) return;
		if (!hasProfane(golem)) return;
		LivingEntity target = event.getEntity();
		List<MobEffectInstance> snapshot = BEFORE.remove(target.getUUID());
		if (snapshot == null) return;
		List<MobEffectInstance> removed = new ArrayList<>();
		for (MobEffectInstance inst : snapshot) {
			if (!target.hasEffect(inst.getEffect())) {
				removed.add(inst);
			}
		}
		if (!removed.isEmpty()) {
			REMOVED.put(target.getUUID(), removed);
		}
	}

	/** 取出并清空某目标被天启饥荒删除的正面效果。 */
	public static List<MobEffectInstance> drain(LivingEntity target) {
		List<MobEffectInstance> list = REMOVED.remove(target.getUUID());
		return list == null ? List.of() : list;
	}

	/** 判断傀儡是否装有天启饥荒（The Profane）。对象引用优先，注册名匹配兜底（覆盖不同版本命名空间）。 */
	public static boolean hasProfane(AbstractGolemEntity<?, ?> golem) {
		if (ModList.get().isLoaded("goety_revelation")
				&& golem.getModifiers().containsKey(GRCompatRegistry.CURSE.get())) {
			return true;
		}
		for (var entry : golem.getModifiers().entrySet()) {
			ResourceLocation name = entry.getKey().getRegistryName();
			if (PROFANE_MG.equals(name) || PROFANE_GR.equals(name)) return true;
		}
		return false;
	}

	private static final ResourceLocation PROFANE_MG = new ResourceLocation("modulargolems", "the_profane");
	private static final ResourceLocation PROFANE_GR = new ResourceLocation("goety_revelation", "the_profane");
}
