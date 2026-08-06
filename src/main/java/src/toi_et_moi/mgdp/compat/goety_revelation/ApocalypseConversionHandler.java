package src.toi_et_moi.mgdp.compat.goety_revelation;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import src.toi_et_moi.mgdp.Mgdp;
import src.toi_et_moi.mgdp.init.MGDPItems;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

/**
 * Special acquisitions for the final apostle titles:
 * - Apocalypse: a golem holding raw/cooked cod that receives the goety:doom effect
 *   converts the cod into the apocalypse upgrade item.
 * - Genesis: a golem holding every other apostle title that carries the ascension halo
 *   (goety_revelation:ascension_halo) converts it into the genesis upgrade item after
 *   a 10-second channelled ritual (altar sounds).
 */
@Mod.EventBusSubscriber(modid = Mgdp.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ApocalypseConversionHandler {

	private static final int GENESIS_CONVERT_TICKS = 200;
	private static final String TAG_GENESIS_CONVERT = "mgdp_genesis_convert";

	private static Item ASCENSION_HALO;

	@SubscribeEvent
	public static void onDoomApplied(MobEffectEvent.Added event) {
		if (event.getEntity().level().isClientSide()) return;
		if (!(event.getEntity() instanceof AbstractGolemEntity<?, ?> golem)) return;

		MobEffect doom = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("goety", "doom"));
		if (doom == null || event.getEffectInstance().getEffect() != doom) return;

		ItemStack hand = golem.getMainHandItem();
		if (!hand.is(Items.COD) && !hand.is(Items.COOKED_COD)) return;

		golem.removeEffect(doom);
		golem.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(MGDPItems.THE_APOCALYPSE.get()));
		playSound(golem, "doom");
	}

	@SubscribeEvent
	public static void onGolemTick(LivingEvent.LivingTickEvent event) {
		if (!(event.getEntity() instanceof AbstractGolemEntity<?, ?> golem)) return;
		if (golem.level().isClientSide) return;
		if (golem.tickCount % 40 != 0) return;

		var data = golem.getPersistentData();
		int convertStart = data.getInt(TAG_GENESIS_CONVERT);
		boolean converting = convertStart > 0;

		var mods = golem.getModifiers();
		boolean all = mods.containsKey(MGDPModifiers.THE_PYRE_LORD.get())
				&& mods.containsKey(MGDPModifiers.THE_WITCH_KING.get())
				&& mods.containsKey(MGDPModifiers.THE_CRUEL.get())
				&& mods.containsKey(MGDPModifiers.THE_GREAT_SHADOW.get())
				&& mods.containsKey(MGDPModifiers.THE_DEFILER.get())
				&& mods.containsKey(MGDPModifiers.THE_DARK.get())
				&& mods.containsKey(MGDPModifiers.THE_GLORIOUS.get());

		if (ASCENSION_HALO == null) {
			ASCENSION_HALO = ForgeRegistries.ITEMS.getValue(new ResourceLocation("goety_revelation", "ascension_halo"));
		}
		boolean hasHalo = ASCENSION_HALO != null && golem.getMainHandItem().is(ASCENSION_HALO);

		if (converting) {
			if (!all || !hasHalo) {
				// Ritual interrupted
				data.remove(TAG_GENESIS_CONVERT);
				return;
			}
			if (golem.tickCount - convertStart >= GENESIS_CONVERT_TICKS) {
				// Ritual complete
				data.remove(TAG_GENESIS_CONVERT);
				golem.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(MGDPItems.THE_GENESIS.get()));
				playSound(golem, "altar_finish");
			} else {
				playSound(golem, "altar_loop");
			}
		} else if (all && hasHalo) {
			// Ritual starts
			data.putInt(TAG_GENESIS_CONVERT, golem.tickCount);
			playSound(golem, "altar_start");
		}
	}

	private static void playSound(AbstractGolemEntity<?, ?> golem, String name) {
		SoundEvent snd = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("goety", name));
		if (snd != null) {
			golem.playSound(snd, 2.0F, 1.0F);
		}
	}
}
