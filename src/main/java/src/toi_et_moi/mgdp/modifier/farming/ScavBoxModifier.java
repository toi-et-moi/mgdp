package src.toi_et_moi.mgdp.modifier.farming;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.entity.mode.GolemModes;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.PacketDistributor;
import src.toi_et_moi.mgdp.Config;
import src.toi_et_moi.mgdp.init.MGDPModifiers;
import src.toi_et_moi.mgdp.network.MGDPNetwork;
import src.toi_et_moi.mgdp.network.ScavStatusPacket;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 拾荒箱升级（灵感来自逃离塔科夫 scav 宝箱）
 * <p>
 * 傀儡每累计一段空闲时间（无战斗目标，战斗时间不计入）就会拾荒一次：
 * 1. 从自动发现池（所有命名空间 chests/* 战利品表，即当前安装的全部
 *    原版+模组结构箱子表）随机滚 2+等级 张表；
 * 2. 对每个已安装的联动模组，额外滚一张 mgdp 专属战利品表
 *    （包含该模组不会出现在箱子里的特殊物品）；
 * 3. 若同时装了收获作物/挖矿升级，额外滚一张物资表
 *    （农作物/木材/树苗/矿石等）。
 * 产出时：周围 1 格有容器则直接放入，否则掉落物直接出现在傀儡位置
 * （有拾取升级会自动捡走）。
 * <p>
 * 空闲累计存于傀儡 persistent data（卸载重载不丢）。
 * 自动发现池按服务器缓存，数据包重载后自然重建。
 */
public class ScavBoxModifier extends GolemModifier {

	private static final Logger LOGGER = LogUtils.getLogger();

	private static final String TAG_IDLE = "mgdp_scav_idle";

	/** 联动模组 -> 专属战利品表（表内物品 ID 均来自 MGDP 自身配方/代码验证） */
	private static final String[][] MOD_TABLES = {
			{"goety", "mgdp:scav/goety"},
			{"goety_revelation", "mgdp:scav/goety_revelation"},
			{"l2complements", "mgdp:scav/l2complements"},
			{"l2hostility", "mgdp:scav/l2hostility"},
			{"cataclysm", "mgdp:scav/cataclysm"},
			{"create", "mgdp:scav/create"},
			{"smc", "mgdp:scav/smc"},
			{"twilightforest", "mgdp:scav/twilight_forest"},
			{"irons_spellbooks", "mgdp:scav/irons_spellbooks"},
			{"golemdungeons", "mgdp:scav/golem_dungeons"},
			{"curseofpandora", "mgdp:scav/curse_of_pandora"}
	};

	private static final ResourceLocation FARM_TABLE = new ResourceLocation("mgdp", "scav/farm");
	private static final ResourceLocation MINE_TABLE = new ResourceLocation("mgdp", "scav/mine");
	private static final ResourceLocation LUMBER_TABLE = new ResourceLocation("mgdp", "scav/lumber");
	private static final ResourceLocation ARCH_TABLE = new ResourceLocation("mgdp", "scav/archaeology");
	private static final ResourceLocation SWIM_TABLE = new ResourceLocation("mgdp", "scav/swim");

	/** 原版原木 -> 同种树苗（成对抽出，森林砍伐带回家种回去） */
	private static final Map<String, String> LOG_SAPLING_PAIRS = Map.of(
			"minecraft:oak_log", "minecraft:oak_sapling",
			"minecraft:spruce_log", "minecraft:spruce_sapling",
			"minecraft:birch_log", "minecraft:birch_sapling",
			"minecraft:jungle_log", "minecraft:jungle_sapling",
			"minecraft:acacia_log", "minecraft:acacia_sapling",
			"minecraft:dark_oak_log", "minecraft:dark_oak_sapling",
			"minecraft:cherry_log", "minecraft:cherry_sapling",
			"minecraft:mangrove_log", "minecraft:mangrove_propagule");

	/** 自动发现池缓存（按服务器实例 + 配置版本重建，配置热重载后自动刷新） */
	private static Object poolServer;
	private static Set<ResourceLocation> chestPool;
	private static int poolConfigVersion = -1;

	public ScavBoxModifier() {
		super(StatFilterType.MASS, 3);
	}

	@Override
	public List<MutableComponent> getDetail(int v) {
		return List.of(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GREEN));
	}

	private static final int STATUS_INTERVAL = 20; // 头顶状态推送间隔：1 秒

	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;
		if (level <= 0) return;
		if (!(golem.level() instanceof ServerLevel sl)) return;

		// 计时条件：必须处于停留行为模式且无战斗目标；状态经数据包推送到头顶渲染
		if (golem.getTarget() != null) {
			if (Config.scavShowCountdown && golem.tickCount % STATUS_INTERVAL == 0) {
				sendStatus(golem, ScavStatusPacket.STATE_COMBAT, 0);
			}
			return;
		}
		if (golem.getMode() != GolemModes.STAND) {
			if (Config.scavShowCountdown && golem.tickCount % STATUS_INTERVAL == 0) {
				sendStatus(golem, ScavStatusPacket.STATE_NO_MODE, 0);
			}
			return;
		}

		CompoundTag data = golem.getPersistentData();
		long idle = data.getLong(TAG_IDLE) + 1;
		long cooldown = cooldownTicks(level);
		if (idle < cooldown) {
			data.putLong(TAG_IDLE, idle);
			if (Config.scavShowCountdown && golem.tickCount % STATUS_INTERVAL == 0) {
				sendStatus(golem, ScavStatusPacket.STATE_COUNTING, cooldown - idle);
			}
			return;
		}
		data.putLong(TAG_IDLE, 0);
		sendStatus(golem, ScavStatusPacket.STATE_NONE, 0);

		int count = scavenge(golem, sl, level);
		if (count > 0) {
			if (golem.getOwner() instanceof ServerPlayer player) {
				player.displayClientMessage(
						Component.translatable("mgdp.scav.found", golem.getDisplayName(), count), true);
			}
			golem.level().playSound(null, golem.blockPosition(),
					SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.6F, 1.0F);
		}
	}

	/**
	 * 把拾荒状态推送给主人客户端（头顶公告板渲染，不占用 customName）。
	 */
	private void sendStatus(AbstractGolemEntity<?, ?> golem, byte state, long remainTicks) {
		if (!(golem.getOwner() instanceof ServerPlayer player)) return;
		int seconds = (int) Math.max(0, remainTicks / 20);
		MGDPNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
				new ScavStatusPacket(golem.getId(), state, seconds));
	}

	private long cooldownTicks(int level) {
		int minutes = Config.scavCooldownBaseMinutes - (level - 1) * Config.scavCooldownPerLevel;
		return Math.max(1, minutes) * 60L * 20L;
	}

	private int scavenge(AbstractGolemEntity<?, ?> golem, ServerLevel sl, int level) {
		List<ItemStack> haul = new ArrayList<>();
		Vec3 origin = golem.position();
		// 飞行/火箭飞行傀儡：赶路效率极高 -> 所有抽取次数翻倍；且飞行本身就是游泳升级版，可触发海产品表
		boolean fly = golem.getModifiers().containsKey(MGDPModifiers.FLIGHT.get())
				|| golem.getModifiers().containsKey(MGDPModifiers.ROCKET_FLIGHT.get());
		int mult = fly ? 2 : 1;

		// 1. 自动发现池：随机滚 (2+等级)*mult 张结构箱子表
		Set<ResourceLocation> pool = getChestPool(sl);
		int rolls = (2 + level) * mult;
		for (int i = 0; i < rolls && !pool.isEmpty(); i++) {
			haul.addAll(rollTable(sl, pick(pool, sl.random.nextInt(pool.size())), origin));
		}
		// 2. 联动模组专属表：装了哪个模组就按配置概率滚哪张（未配置的恒触发）
		for (String[] entry : MOD_TABLES) {
			if (ModList.get().isLoaded(entry[0])) {
				double chance = Config.scavModTableChances == null ? 1.0
						: Config.scavModTableChances.getOrDefault(entry[0], 1.0);
				if (sl.random.nextDouble() < chance) {
					rollTableTimes(haul, sl, new ResourceLocation(entry[1]), origin, mult);
				}
			}
		}
		// 3. 收获/挖矿/伐木/考古/游泳联动：各自滚精选表 + 标签抽取（飞行翻倍）
		boolean farmer = golem.getModifiers().containsKey(MGDPModifiers.HARVEST_CROP.get());
		boolean miner = golem.getModifiers().containsKey(MGDPModifiers.MINER.get());
		boolean lumberjack = golem.getModifiers().containsKey(MGDPModifiers.LUMBERJACK.get());
		boolean brush = golem.getModifiers().containsKey(MGDPModifiers.BRUSH.get());
		boolean swim = golem.getModifiers().containsKey(dev.xkmc.modulargolems.init.registrate.GolemModifiers.SWIM.get());
		if (farmer) {
			rollTableTimes(haul, sl, FARM_TABLE, origin, mult);
			haul.addAll(tagRolls(sl, 2 * mult, 1, 3, Tags.Items.SEEDS, Tags.Items.CROPS));
		}
		if (miner) {
			rollTableTimes(haul, sl, MINE_TABLE, origin, mult);
			haul.addAll(tagRolls(sl, 2 * mult, 1, 3, Tags.Items.ORES));
		}
		if (lumberjack) {
			rollTableTimes(haul, sl, LUMBER_TABLE, origin, mult);
			// 同种原木必带同种树苗（成对抽出）；无对应关系的原木/菌柄天然不配对，无需额外过滤
			pairLogsWithSaplings(haul, sl);
			// 标签抽取：原木 12~64（与 lumber.json 表一致）；树苗由表 + 原木配对兜底
			haul.addAll(tagRolls(sl, 2 * mult, 12, 64, ItemTags.LOGS));
		}
		if (brush) {
			rollTableTimes(haul, sl, ARCH_TABLE, origin, mult);
			// 陶片/纹饰锻造模板/音乐唱片都有统一标签，模组新增自动进池
			haul.addAll(tagRolls(sl, 2 * mult, 1, 3,
					ItemTags.DECORATED_POT_SHERDS, ItemTags.TRIM_TEMPLATES, ItemTags.MUSIC_DISCS));
		}
		if (swim || fly) {
			rollTableTimes(haul, sl, SWIM_TABLE, origin, mult);
		}
		// 产出：周围 1 格有容器则直接放入；塞不下的剩余物品退回掉落物形式
		// （插入过程异常也不吞物品，一律按掉落处理）
		Container container = findContainer(golem);
		int count = 0;
		for (ItemStack stack : haul) {
			count += stack.getCount();
			if (container != null) {
				try {
					stack = insertItem(container, stack);
				} catch (Exception ignored) {
					// 容器异常：剩余物品退回掉落物，绝不丢失
				}
			}
			if (!stack.isEmpty()) {
				golem.spawnAtLocation(stack);
			}
		}
		LOGGER.info("[ScavBox] {} scavenged: pool={} haul={} item(s), container={}",
				golem.getDisplayName().getString(), pool.size(), count, container != null);
		return count;
	}

	/**
	 * 原木 -> 同种树苗成对：滚到的原木每种补 2~6 棵同种树苗。
	 */
	private void pairLogsWithSaplings(List<ItemStack> haul, ServerLevel sl) {
		List<ItemStack> extras = new ArrayList<>();
		for (ItemStack stack : haul) {
			if (stack.isEmpty()) continue;
			var key = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
			if (key == null) continue;
			String sapling = LOG_SAPLING_PAIRS.get(key.toString());
			if (sapling != null) {
				Item item = net.minecraftforge.registries.ForgeRegistries.ITEMS
						.getValue(net.minecraft.resources.ResourceLocation.tryParse(sapling));
				if (item != null) {
					extras.add(new ItemStack(item, 2 + sl.random.nextInt(5)));
				}
			}
		}
		haul.addAll(extras);
	}

	/**
	 * 同一张表滚 times 次（飞行翻倍用）。
	 */
	private void rollTableTimes(List<ItemStack> haul, ServerLevel sl, ResourceLocation id, Vec3 origin, int times) {
		for (int i = 0; i < times; i++) {
			haul.addAll(rollTable(sl, id, origin));
		}
	}

	private List<ItemStack> rollTable(ServerLevel sl, ResourceLocation id, Vec3 origin) {
		try {
			LootTable table = sl.getServer().getLootData().getLootTable(id);
			// CHEST 参数集强制要求 origin（掉落源位置）
			LootParams params = new LootParams.Builder(sl)
					.withParameter(LootContextParams.ORIGIN, origin)
					.create(LootContextParamSets.CHEST);
			List<ItemStack> items = new ArrayList<>(table.getRandomItems(params));
			if (!items.isEmpty()) {
				LOGGER.info("[ScavBox] rolled {} -> {} item(s)", id, items.size());
			}
			return items;
		} catch (Exception e) {
			LOGGER.error("[ScavBox] failed to roll loot table {}", id, e);
			return new ArrayList<>();
		}
	}

	/**
	 * 从物品标签随机抽取：任意模组打了对应标签的物品自动进池，零维护。
	 * 数量区间按调用点自定义（minCount~maxCount，含两端）。
	 */
	@SafeVarargs
	private List<ItemStack> tagRolls(ServerLevel sl, int count, int minCount, int maxCount, TagKey<Item>... tags) {
		List<Item> pool = new ArrayList<>();
		for (TagKey<Item> tag : tags) {
			for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
				pool.add(holder.value());
			}
		}
		List<ItemStack> out = new ArrayList<>();
		int span = Math.max(1, maxCount - minCount + 1);
		for (int i = 0; i < count && !pool.isEmpty(); i++) {
			// 随机数量 minCount~maxCount
			out.add(new ItemStack(pool.get(sl.random.nextInt(pool.size())), minCount + sl.random.nextInt(span)));
		}
		return out;
	}

	/**
	 * 查找傀儡周围 1 格（3x3x3）内的第一个容器。
	 */
	private Container findContainer(AbstractGolemEntity<?, ?> golem) {
		BlockPos pos = golem.blockPosition();
		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = -1; dy <= 1; dy++) {
				for (int dz = -1; dz <= 1; dz++) {
					BlockEntity be = golem.level().getBlockEntity(pos.offset(dx, dy, dz));
					if (be instanceof Container container) return container;
				}
			}
		}
		return null;
	}

	/**
	 * 把物品堆叠塞进容器：先合并进同种堆叠，再填空格；装不下的原样返回。
	 */
	private ItemStack insertItem(Container container, ItemStack stack) {
		for (int i = 0; i < container.getContainerSize() && !stack.isEmpty(); i++) {
			ItemStack slot = container.getItem(i);
			if (!slot.isEmpty() && ItemStack.isSameItemSameTags(slot, stack)
					&& slot.getCount() < slot.getMaxStackSize()) {
				int move = Math.min(slot.getMaxStackSize() - slot.getCount(), stack.getCount());
				slot.grow(move);
				stack.shrink(move);
				container.setChanged();
			}
		}
		for (int i = 0; i < container.getContainerSize() && !stack.isEmpty(); i++) {
			if (container.getItem(i).isEmpty()) {
				int move = Math.min(stack.getMaxStackSize(), stack.getCount());
				container.setItem(i, stack.split(move));
				container.setChanged();
			}
		}
		return stack;
	}

	private ResourceLocation pick(Set<ResourceLocation> pool, int index) {
		for (ResourceLocation id : pool) {
			if (index-- == 0) return id;
		}
		return pool.iterator().next();
	}

	private Set<ResourceLocation> getChestPool(ServerLevel sl) {
		Object server = sl.getServer();
		if (chestPool == null || poolServer != server || poolConfigVersion != Config.scavConfigVersion) {
			chestPool = new HashSet<>();
			// 枚举全部数据包中的 loot_tables/chests/*.json（原版 + 所有模组自动覆盖）
			// 注意：listResources 返回的键是带 "loot_tables/" 前缀的完整相对路径
			sl.getServer().getResourceManager()
					.listResources("loot_tables", s -> s.getPath().endsWith(".json"))
					.keySet().forEach(file -> {
						String path = file.getPath();
						String rel = path.startsWith("loot_tables/") ? path.substring("loot_tables/".length()) : path;
						if (rel.startsWith("chests/") && rel.endsWith(".json")
								&& !file.getNamespace().equals("mgdp")) {
							chestPool.add(new ResourceLocation(file.getNamespace(),
									rel.substring(0, rel.length() - 5)));
						}
					});
			LOGGER.info("[ScavBox] chest loot pool rebuilt: {} tables", chestPool.size());
			if (Config.scavExcludeTables != null) {
				for (String exclude : Config.scavExcludeTables) {
					chestPool.remove(new ResourceLocation(exclude));
				}
			}
			poolServer = server;
			poolConfigVersion = Config.scavConfigVersion;
		}
		return chestPool;
	}
}
