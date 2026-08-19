package src.toi_et_moi.mgdp;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = Mgdp.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

    // --- Common Config / 通用配置 ---

    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.IntValue SWAP_COOLDOWN = COMMON_BUILDER
            .comment("Cooldown for the Swap upgrade in seconds (default: 10)", "换位升级的冷却时间（秒）（默认：10）")
            .defineInRange("swapCooldown", 10, 0, 300);

    private static final ForgeConfigSpec.IntValue LIQUID_CLEAR_RANGE = COMMON_BUILDER
            .comment("Liquid Clear range per level in blocks (default: 8)", "清液升级每级范围（格）（默认：8）")
            .defineInRange("liquidClearRangePerLevel", 8, 1, 128);

    private static final ForgeConfigSpec.BooleanValue DESTRUCTION_MODE = COMMON_BUILDER
            .comment("Explosive upgrades (Trident Festival, Self Destruct) destroy terrain (default: false)", "爆炸类升级（三叉戟狂欢节、自爆）是否破坏地形（默认：false）")
            .define("destructionMode", false);

    private static final ForgeConfigSpec.IntValue TIME_AXIS_SPEED = COMMON_BUILDER
            .comment("Time Axis block acceleration multiplier (default: 8, 0 = disabled)", "时光升级方块加速倍率（默认：8，0 = 禁用）")
            .defineInRange("timeAxisSpeed", 8, 0, 256);

    private static final ForgeConfigSpec.BooleanValue MOB_AUTO_AGGRO = COMMON_BUILDER
            .comment("Mobs target by golems automatically target the golem back (default: true)", "被傀儡锁定的怪物是否自动还击傀儡（默认：true）")
            .define("mobAutoAggro", true);

    private static final ForgeConfigSpec.IntValue DOG_FALL_DISTANCE = COMMON_BUILDER
            .comment("Minimum fall distance for dog golem to rescue player (default: 5)", "跟随犬形傀儡救援主人的最小坠落距离（格）（默认：5）")
            .defineInRange("dogFallDistance", 5, 1, 50);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> MINER_EXTRA_BLOCKS = COMMON_BUILDER
            .comment("Additional mineable blocks for the Miner upgrade (registry names, e.g. \"minecraft:ancient_debris\")", "挖矿升级额外可开采方块（注册名，如 \"minecraft:ancient_debris\"）")
            .defineList("minerExtraBlocks", List.of(), e -> e instanceof String);

    private static final ForgeConfigSpec.IntValue SCAV_COOLDOWN_BASE = COMMON_BUILDER
            .comment("Idle time required for the Scav Box in minutes (default: 20, reduced per level)", "拾荒箱所需累计空闲时间（分钟）（默认：20，逐级递减）")
            .defineInRange("scavCooldownBaseMinutes", 20, 1, 240);

    private static final ForgeConfigSpec.IntValue SCAV_COOLDOWN_PER_LEVEL = COMMON_BUILDER
            .comment("Idle time reduced per Scav Box level in minutes (default: 5)", "拾荒箱每级减少的空闲时间（分钟）（默认：5）")
            .defineInRange("scavCooldownPerLevel", 5, 0, 120);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> SCAV_EXCLUDE_TABLES = COMMON_BUILDER
            .comment("Loot tables excluded from the Scav Box (registry names, e.g. \"minecraft:chests/spawn_bonus_chest\")", "拾荒箱排除的战利品表（注册名，如 \"minecraft:chests/spawn_bonus_chest\"）")
            .defineList("scavExcludeTables", List.of(), e -> e instanceof String);

    private static final ForgeConfigSpec.BooleanValue SCAV_SHOW_COUNTDOWN = COMMON_BUILDER
            .comment("Show the Scav Box countdown above the golem (default: true)", "是否在傀儡头顶显示拾荒箱收集倒计时（默认：true）")
            .define("scavShowCountdown", true);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> SCAV_MOD_TABLE_CHANCES = COMMON_BUILDER
            .comment("Trigger chance for linked mod-specific loot tables (format: \"modid:chance\", e.g. \"cataclysm:0.15\"; mods not listed always use 1.0)", "联动模组专属表的触发概率（格式：\"模组id:概率\"，如 \"cataclysm:0.15\"；未列出的模组恒为 1.0）")
            .defineList("scavModTableChances", List.of(
                    "create:0.6",
                    "twilightforest:0.5",
                    "goety:0.25",
                    "irons_spellbooks:0.25",
                    "l2hostility:0.2",
                    "goety_revelation:0.05",
                    "cataclysm:0.15",
                    "golemdungeons:0.15",
                    "curseofpandora:0.15",
                    "smc:0.1",
                    "l2complements:0.05"
            ), e -> e instanceof String);

    static final ForgeConfigSpec COMMON_SPEC = COMMON_BUILDER.build();

    public static int swapCooldown;
    public static int liquidClearRangePerLevel;
    public static boolean destructionMode;
    public static int timeAxisSpeed;
    public static boolean mobAutoAggro;
    public static int dogFallDistance;
    public static Set<String> minerExtraBlocks;
    public static int scavCooldownBaseMinutes;
    public static int scavCooldownPerLevel;
    public static Set<String> scavExcludeTables;
    public static boolean scavShowCountdown;
    public static Map<String, Double> scavModTableChances; // Linked loot table chance: mod id -> chance (0-1) / 联动表触发概率：模组id -> 概率(0-1)
    public static int scavConfigVersion; // Incremented on each config (re)load so caches can detect hot reloads / 每次配置(重)加载 +1,供依赖缓存的机制感知热重载

    // --- Client Config / 客户端配置 ---

    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue HEALTH_WARNING = CLIENT_BUILDER
            .comment("Show low-HP golem warning on screen (default: true)", "是否在屏幕上显示低血量傀儡警告（默认：true）")
            .define("golemHealthWarning", true);

    private static final ForgeConfigSpec.DoubleValue JUKEBOX_VOLUME = CLIENT_BUILDER
            .comment("Jukebox music volume (default: 4.0, range: 0.0 - 16.0)", "网络音乐机音量（默认：4.0，范围：0.0 - 16.0）")
            .defineInRange("jukeboxVolume", 4.0, 0.0, 16.0);

    static final ForgeConfigSpec CLIENT_SPEC = CLIENT_BUILDER.build();

    public static boolean golemHealthWarning;
    public static double jukeboxVolume;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == COMMON_SPEC) {
            swapCooldown = SWAP_COOLDOWN.get();
            liquidClearRangePerLevel = LIQUID_CLEAR_RANGE.get();
            destructionMode = DESTRUCTION_MODE.get();
            timeAxisSpeed = TIME_AXIS_SPEED.get();
            mobAutoAggro = MOB_AUTO_AGGRO.get();
            dogFallDistance = DOG_FALL_DISTANCE.get();
            minerExtraBlocks = new HashSet<>(MINER_EXTRA_BLOCKS.get());
            scavCooldownBaseMinutes = SCAV_COOLDOWN_BASE.get();
            scavCooldownPerLevel = SCAV_COOLDOWN_PER_LEVEL.get();
            scavExcludeTables = new HashSet<>(SCAV_EXCLUDE_TABLES.get());
            scavShowCountdown = SCAV_SHOW_COUNTDOWN.get();
            scavModTableChances = new HashMap<>();
            for (String s : SCAV_MOD_TABLE_CHANCES.get()) {
                int i = s.indexOf(':');
                if (i > 0) {
                    try {
                        scavModTableChances.put(s.substring(0, i), Double.parseDouble(s.substring(i + 1)));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            scavConfigVersion++;
        } else if (event.getConfig().getSpec() == CLIENT_SPEC) {
            golemHealthWarning = HEALTH_WARNING.get();
            jukeboxVolume = JUKEBOX_VOLUME.get();
        }
    }
}
