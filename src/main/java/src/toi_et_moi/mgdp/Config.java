package src.toi_et_moi.mgdp;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = Mgdp.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // Swap upgrade cooldown in seconds
    private static final ForgeConfigSpec.IntValue SWAP_COOLDOWN = BUILDER
            .comment("Cooldown for the Swap upgrade in seconds (default: 10)")
            .defineInRange("swapCooldown", 10, 0, 300);

    // Liquid Clear range per level (blocks)
    private static final ForgeConfigSpec.IntValue LIQUID_CLEAR_RANGE = BUILDER
            .comment("Liquid Clear range per level in blocks (default: 8)")
            .defineInRange("liquidClearRangePerLevel", 8, 1, 128);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static int swapCooldown;
    public static int liquidClearRangePerLevel;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        swapCooldown = SWAP_COOLDOWN.get();
        liquidClearRangePerLevel = LIQUID_CLEAR_RANGE.get();
    }
}
