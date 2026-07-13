package src.toi_et_moi.mgdp.modifier.combat;

import dev.xkmc.mob_weapon_api.api.projectile.ProjectileWeaponUser;
import dev.xkmc.mob_weapon_api.example.behavior.ThrowableBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class PerkinSpearThrowBehavior extends ThrowableBehavior {

    @Override
    protected Projectile getProjectile(ProjectileWeaponUser user, ItemStack stack, LivingEntity target, int charge) {
        LivingEntity shooter = user.user();
        if (!net.minecraftforge.fml.ModList.get().isLoaded("smc")) return null;

        try {
            var type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("smc", "thrown_spear"));
            if (type == null) return null;

            Class<?> cls = Class.forName("com.starmeow.smc.entities.projectiles.ThrownSpear");
            var ctor = cls.getConstructor(net.minecraft.world.level.Level.class,
                    LivingEntity.class, ItemStack.class);
            Entity spear = (Entity) ctor.newInstance(shooter.level(), shooter, stack);
            if (spear instanceof Projectile p) return p;
        } catch (Exception e) {
            src.toi_et_moi.mgdp.Mgdp.LOGGER.warn("PerkinSpearThrow: failed", e);
        }
        return null;
    }
}
