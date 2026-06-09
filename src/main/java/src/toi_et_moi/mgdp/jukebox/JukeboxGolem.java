package src.toi_et_moi.mgdp.jukebox;

import net.minecraft.world.item.ItemStack;

/**
 * Interface for AbstractGolemEntity mixin to access jukebox data.
 */
public interface JukeboxGolem {

    boolean mgdp$isPlaying();

    void mgdp$setPlaying(boolean playing);

    ItemStack mgdp$getDisc();

    void mgdp$setDisc(ItemStack stack);

    int mgdp$getTick();

    void mgdp$setTick(int tick);

}
