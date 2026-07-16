package src.toi_et_moi.mgdp.modifier.special;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import src.toi_et_moi.mgdp.Mgdp;
import src.toi_et_moi.mgdp.init.MGDPModifiers;

@Mod.EventBusSubscriber(modid = Mgdp.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PenguinModifier extends GolemModifier {

    public PenguinModifier() {
        super(StatFilterType.MASS, 1);
    }

    @Override
    public java.util.List<net.minecraft.network.chat.MutableComponent> getDetail(int v) {
        return java.util.List.of(net.minecraft.network.chat.Component.translatable(getDescriptionId() + ".desc")
                .withStyle(net.minecraft.ChatFormatting.GREEN));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof AbstractGolemEntity<?, ?> golem
                && !event.getEntity().level().isClientSide
                && golem.getModifiers().containsKey(MGDPModifiers.PENGUIN.get())) {
            LivingEntity target = event.getEntity();
            var level = target.level();
            var type = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(
                    new ResourceLocation("twilightforest", "penguin"));
            if (type != null) {
                var penguin = type.create(level);
                if (penguin != null) {
                    penguin.setPos(target.getX(), target.getY(), target.getZ());
                    level.addFreshEntity(penguin);
                }
            }
        }
    }
}
