package src.toi_et_moi.mgdp.modifier.special;

import dev.xkmc.modulargolems.content.core.StatFilterType;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.entity.mode.GolemModes;
import dev.xkmc.modulargolems.content.modifier.base.GolemModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import src.toi_et_moi.mgdp.Config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;

public class TimeAxisModifier extends GolemModifier {

	private static final Field TICKERS_IN_LEVEL;
	private static final Field WRAPPER_TICKER;
	private static final Field BOUND_TICKER;

	static {
		try {
			// Try both deobf (tickersInLevel) and SRG (f_156362_) field names
			Field tickersField = null;
			try {
				tickersField = LevelChunk.class.getDeclaredField("tickersInLevel");
			} catch (NoSuchFieldException e) {
				tickersField = LevelChunk.class.getDeclaredField("f_156362_");
			}
			TICKERS_IN_LEVEL = tickersField;
			TICKERS_IN_LEVEL.setAccessible(true);

			Class<?> wrapperClass = Class.forName("net.minecraft.world.level.chunk.LevelChunk$RebindableTickingBlockEntityWrapper");
			Field wrapperTickerField = null;
			try {
				wrapperTickerField = wrapperClass.getDeclaredField("ticker");
			} catch (NoSuchFieldException e) {
				wrapperTickerField = wrapperClass.getDeclaredField("f_156444_");
			}
			WRAPPER_TICKER = wrapperTickerField;
			WRAPPER_TICKER.setAccessible(true);

			Class<?> boundClass = Class.forName("net.minecraft.world.level.chunk.LevelChunk$BoundTickingBlockEntity");
			Field boundTickerField = null;
			try {
				boundTickerField = boundClass.getDeclaredField("ticker");
			} catch (NoSuchFieldException e) {
				boundTickerField = boundClass.getDeclaredField("f_156429_");
			}
			BOUND_TICKER = boundTickerField;
			BOUND_TICKER.setAccessible(true);
		} catch (Exception e) {
			throw new RuntimeException("Failed to initialize TimeAxisModifier reflection", e);
		}
	}

	public TimeAxisModifier() {
		super(StatFilterType.HEALTH, 1);
	}

	// === 日冕: 无视负面药水效果（无时间限制） ===
	@Override
	public void onAiStep(AbstractGolemEntity<?, ?> golem, int level) {
		if (golem.level().isClientSide()) return;

		// Remove negative effects every 10 ticks
		if (golem.tickCount % 10 == 0) {
			var effects = new ArrayList<>(golem.getActiveEffects());
			for (var effect : effects) {
				if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
					golem.removeEffect(effect.getEffect());
				}
			}
		}

		// === 光芒: 无视光照限制，持续恢复 ===
		golem.heal(0.1f);

		// === 时轴专属: 停留模式时加速周围方块（每帧，4 格半径） ===
		if (golem.getMode() == GolemModes.STAND && golem.level() instanceof ServerLevel serverLevel) {
			accelerateBlocks(serverLevel, golem);
		}
	}

	@SuppressWarnings("unchecked")
	private void accelerateBlocks(ServerLevel level, AbstractGolemEntity<?, ?> golem) {
		int speed = Config.timeAxisSpeed;
		if (speed <= 0) return;

		int r = 4;
		BlockPos golemPos = golem.blockPosition();

		// 加速随机刻（作物生长、雪融化等）
		BlockPos.betweenClosedStream(
				golemPos.getX() - r, golemPos.getY() - r, golemPos.getZ() - r,
				golemPos.getX() + r, golemPos.getY() + r, golemPos.getZ() + r
		).forEach(pos -> {
			BlockState state = level.getBlockState(pos);
			if (state.isRandomlyTicking()) {
				for (int i = 0; i < speed; i++) {
					state.randomTick(level, pos, level.random);
				}
			}
		});

		// 加速所有 ticking 方块实体（全模组兼容）
		int chunkMinX = (golemPos.getX() - r) >> 4;
		int chunkMaxX = (golemPos.getX() + r) >> 4;
		int chunkMinZ = (golemPos.getZ() - r) >> 4;
		int chunkMaxZ = (golemPos.getZ() + r) >> 4;

		try {
			for (int cx = chunkMinX; cx <= chunkMaxX; cx++) {
				for (int cz = chunkMinZ; cz <= chunkMaxZ; cz++) {
					LevelChunk chunk = level.getChunk(cx, cz);
					Map<BlockPos, Object> tickers = (Map<BlockPos, Object>) TICKERS_IN_LEVEL.get(chunk);
					if (tickers == null) continue;

					for (Map.Entry<BlockPos, Object> entry : tickers.entrySet()) {
						BlockPos pos = entry.getKey();
						if (Math.abs(pos.getX() - golemPos.getX()) > r) continue;
						if (Math.abs(pos.getY() - golemPos.getY()) > r) continue;
						if (Math.abs(pos.getZ() - golemPos.getZ()) > r) continue;

						Object wrapper = entry.getValue();
						if (wrapper == null) continue;
						Object bound = WRAPPER_TICKER.get(wrapper);
						if (bound == null) continue;
						BlockEntityTicker<BlockEntity> ticker = (BlockEntityTicker<BlockEntity>) BOUND_TICKER.get(bound);
						if (ticker == null) continue;

						BlockState state = level.getBlockState(pos);
						BlockEntity be = level.getBlockEntity(pos);
						if (be != null && !be.isRemoved()) {
							for (int i = 0; i < speed; i++) {
								ticker.tick(level, pos, state, be);
							}
						}
					}
				}
			}
		} catch (Exception ignored) {
		}
	}

	// === 光芒: 光照无关的增伤（始终生效） ===
	@Override
	public void onHurtTarget(AbstractGolemEntity<?, ?> golem, LivingHurtEvent event, int level) {
		if (golem.level().isClientSide()) return;
		event.setAmount(event.getAmount() * 1.25f);
	}

	// === 月影: 刷怪阻止由 MoonShadowHandler 统一处理 ===
}
