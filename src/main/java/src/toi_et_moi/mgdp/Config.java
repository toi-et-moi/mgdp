package src.toi_et_moi.mgdp;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = Mgdp.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

    // --- Common Config ---

    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.IntValue SWAP_COOLDOWN = COMMON_BUILDER
            .comment("Cooldown for the Swap upgrade in seconds (default: 10)")
            .defineInRange("swapCooldown", 10, 0, 300);

    private static final ForgeConfigSpec.IntValue LIQUID_CLEAR_RANGE = COMMON_BUILDER
            .comment("Liquid Clear range per level in blocks (default: 8)")
            .defineInRange("liquidClearRangePerLevel", 8, 1, 128);

    private static final ForgeConfigSpec.BooleanValue DESTRUCTION_MODE = COMMON_BUILDER
            .comment("Explosive upgrades (Trident Festival, Self Destruct) destroy terrain (default: false)")
            .define("destructionMode", false);

    private static final ForgeConfigSpec.IntValue TIME_AXIS_SPEED = COMMON_BUILDER
            .comment("Time Axis block acceleration multiplier (default: 8, 0 = disabled)")
            .defineInRange("timeAxisSpeed", 8, 0, 256);

    static final ForgeConfigSpec COMMON_SPEC = COMMON_BUILDER.build();

    public static int swapCooldown;
    public static int liquidClearRangePerLevel;
    public static boolean destructionMode;
    public static int timeAxisSpeed;

    // --- Client Config ---

    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue HEALTH_WARNING = CLIENT_BUILDER
            .comment("Show low-HP golem warning on screen (default: true)")
            .define("golemHealthWarning", true);

    static final ForgeConfigSpec CLIENT_SPEC = CLIENT_BUILDER.build();

    public static boolean golemHealthWarning;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == COMMON_SPEC) {
            swapCooldown = SWAP_COOLDOWN.get();
            liquidClearRangePerLevel = LIQUID_CLEAR_RANGE.get();
            destructionMode = DESTRUCTION_MODE.get();
            timeAxisSpeed = TIME_AXIS_SPEED.get();
        } else if (event.getConfig().getSpec() == CLIENT_SPEC) {
            golemHealthWarning = HEALTH_WARNING.get();
        }
    }
}
