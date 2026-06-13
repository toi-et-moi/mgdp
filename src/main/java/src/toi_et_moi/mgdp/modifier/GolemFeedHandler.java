package src.toi_et_moi.mgdp.modifier;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import src.toi_et_moi.mgdp.Mgdp;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

@Mod.EventBusSubscriber(modid = Mgdp.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GolemFeedHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onInteractGolem(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof AbstractGolemEntity<?, ?> golem)) return;
        Player player = event.getEntity();
        if (!golem.canModify(player)) return;
        ItemStack stack = event.getItemStack();
        if (!stack.is(Items.MILK_BUCKET) && golem.getHealth() >= golem.getMaxHealth()) return;

        int type = tryConsume(golem, stack);
        if (type != 0) {
            if (!player.getAbilities().instabuild) {
                ItemStack container = getContainer(stack);
                stack.shrink(1);
                if (!container.isEmpty()) {
                    if (!player.getInventory().add(container)) {
                        player.drop(container, false);
                    }
                }
            }
            playConsumeSound(golem, type);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onGolemTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof AbstractGolemEntity<?, ?> golem)) return;
        if (golem.level().isClientSide()) return;
        if (golem.tickCount % 40 != 0) return;
        boolean hasMilk = false;
        for (InteractionHand h : InteractionHand.values()) {
            if (golem.getItemInHand(h).is(Items.MILK_BUCKET)) hasMilk = true;
        }
        if (!hasMilk && golem.getHealth() >= golem.getMaxHealth()) return;

        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = golem.getItemInHand(hand);
            if (stack.isEmpty()) continue;
            int type = tryConsume(golem, stack);
            if (type == 0) continue;
            playConsumeSound(golem, type);
            ItemStack container = getContainer(stack);
            stack.shrink(1);
            if (!container.isEmpty()) {
                golem.setItemInHand(hand, container);
            }
            return;
        }

        if (ModList.get().isLoaded("l2backpack")) {
            tryConsumeFromBackpack(golem);
        }
    }

    @SuppressWarnings("unchecked")
    private static void tryConsumeFromBackpack(AbstractGolemEntity<?, ?> golem) {
        try {
            Class<?> wcClass = Class.forName("dev.xkmc.l2backpack.content.remote.worldchest.WorldChestItem");

            for (EquipmentSlot slot : List.of(EquipmentSlot.CHEST, EquipmentSlot.OFFHAND)) {
                ItemStack stack = golem.getItemBySlot(slot);
                if (stack.isEmpty() || !wcClass.isInstance(stack.getItem())) continue;
                if (consumeFromWorldChest(golem, wcClass, stack)) return;
            }

            if (ModList.get().isLoaded("curios")) {
                try {
                    Class<?> curiosApi = Class.forName("top.theillusivec4.curios.api.CuriosApi");
                    Method getCurios = curiosApi.getMethod("getCuriosInventory", net.minecraft.world.entity.LivingEntity.class);
                    Object lazyOpt = getCurios.invoke(null, golem);
                    Method resolve = lazyOpt.getClass().getMethod("resolve");
                    Object opt = resolve.invoke(lazyOpt);
                    if (opt == null) return;
                    Method optIsPresent = opt.getClass().getMethod("isPresent");
                    if (!(boolean) optIsPresent.invoke(opt)) return;
                    Object handler = opt.getClass().getMethod("get").invoke(opt);
                    Method getEquipped = handler.getClass().getMethod("getEquippedCurios");
                    Object equipped = getEquipped.invoke(handler);
                    Method getSlots = equipped.getClass().getMethod("getSlots");
                    Method getStackInSlot = equipped.getClass().getMethod("getStackInSlot", int.class);
                    int slots = (int) getSlots.invoke(equipped);
                    for (int i = 0; i < slots; i++) {
                        ItemStack stack = (ItemStack) getStackInSlot.invoke(equipped, i);
                        if (stack.isEmpty() || !wcClass.isInstance(stack.getItem())) continue;
                        if (consumeFromWorldChest(golem, wcClass, stack)) return;
                    }
                } catch (Exception e2) {
                    Mgdp.LOGGER.error("MGDP feed from curios failed: " + e2.getMessage());
                }
            }
        } catch (Exception e) {
            Mgdp.LOGGER.error("MGDP feed from backpack failed: " + e.getMessage());
        }
    }

    private static boolean consumeFromWorldChest(AbstractGolemEntity<?, ?> golem, Class<?> wcClass, ItemStack stack) throws Exception {
        Method getContainer = wcClass.getMethod("getContainer", ItemStack.class, ServerLevel.class);
        Object opt = getContainer.invoke(stack.getItem(), stack, (ServerLevel) golem.level());

        Class<?> optClass = opt.getClass();
        if ((boolean) optClass.getMethod("isEmpty").invoke(opt)) return false;

        Object storage = optClass.getMethod("get").invoke(opt);
        Field f = storage.getClass().getField("container");
        var inv = (net.minecraft.world.SimpleContainer) f.get(storage);

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack invStack = inv.getItem(i);
            if (invStack.isEmpty()) continue;
            int type = tryConsume(golem, invStack);
            if (type == 0) continue;
            playConsumeSound(golem, type);
            ItemStack containerItem = getContainer(invStack);
            invStack.shrink(1);
            if (!containerItem.isEmpty()) {
                golem.spawnAtLocation(containerItem);
            }
            inv.setChanged();
            return true;
        }
        return false;
    }

    /** 0=none, 1=food, 2=potion, 3=milk */
    private static int tryConsume(AbstractGolemEntity<?, ?> golem, ItemStack stack) {
        if (stack.getItem() instanceof PotionItem) {
            List<MobEffectInstance> effects = PotionUtils.getMobEffects(stack);
            if (effects.isEmpty()) return 0;
            for (MobEffectInstance effect : effects) {
                golem.addEffect(new MobEffectInstance(effect));
            }
            return 2;
        }

        if (stack.is(Items.MILK_BUCKET)) {
            boolean hasDebuff = golem.getActiveEffects().stream()
                    .anyMatch(e -> !e.getEffect().isBeneficial());
            if (!hasDebuff) return 0;
            golem.removeAllEffects();
            return 3;
        }

        if (!stack.isEdible()) return 0;
        FoodProperties food = stack.getFoodProperties(golem);
        if (food == null) return 0;

        int healAmount = (int) (food.getNutrition() + food.getNutrition() * food.getSaturationModifier() * 2.0f);
        golem.heal(Math.max(1, healAmount));

        if (food.getEffects() != null) {
            for (var pair : food.getEffects()) {
                if (pair == null || pair.getFirst() == null) continue;
                golem.addEffect(new MobEffectInstance(pair.getFirst()));
            }
        }
        return 1;
    }

    private static void playConsumeSound(AbstractGolemEntity<?, ?> golem, int type) {
        var sound = switch (type) {
            case 2 -> SoundEvents.GENERIC_DRINK;
            case 3 -> SoundEvents.GENERIC_DRINK;
            default -> SoundEvents.GENERIC_EAT;
        };
        golem.level().playSound(null, golem.blockPosition(), sound, SoundSource.NEUTRAL, 1.0F, 1.0F);
    }

    private static ItemStack getContainer(ItemStack stack) {
        if (stack.getItem().hasCraftingRemainingItem()) {
            return new ItemStack(stack.getItem().getCraftingRemainingItem());
        }
        return ItemStack.EMPTY;
    }
}
