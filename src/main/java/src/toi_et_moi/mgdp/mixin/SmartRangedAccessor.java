package src.toi_et_moi.mgdp.mixin;

import dev.xkmc.mob_weapon_api.example.goal.SmartRangedAttackGoal;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SmartRangedAttackGoal.class)
public interface SmartRangedAccessor {

	@Accessor(value = "mob", remap = false)
	Mob getMob();
}
