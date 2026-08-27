package src.toi_et_moi.mgdp.mixin;

import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.Wadjet_Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;

/**
 * Brute-force fix for "modular golems can never lock Wadjet" across
 * Cataclysm 3.16 and 3.31.
 *
 * <h2>The two different Wadjet bugs (different SRG numbers)</h2>
 *
 * <p>Wadjet_Entity is shipped as a slim jar in both 3.16 and 3.31,
 * and in both versions the override is compiled under the SRG name
 * {@code m_142066_}. The actual method being overridden differs
 * between the two versions — they recompiled the class with a
 * different mixin-point distribution — so the SRG name
 * {@code m_142066_} resolves to:</p>
 *
 * <ul>
 *   <li><b>3.16</b>: {@code canBeSeenAsEnemy} — the override is
 *       {@code !isSleep() && super.m_142066_()}. {@code super}
 *       is {@link net.minecraft.world.entity.LivingEntity}'s
 *       vanilla canBeSeenAsEnemy (true if the boss is alive).
 *       With the default spawn leaving {@code isSleep()=true}
 *       (and no auto-trigger to wake the boss up), this short-
 *       circuits to false forever — {@code AbstractGolemEntity
 *       .setTargetRaw} gates on
 *       {@code target.canBeSeenAsEnemy()}, so golems can never
 *       lock a 3.16 Wadjet.</li>
 *   <li><b>3.31</b>: {@code isSleep} — the override is
 *       {@code isAwaken() && super.m_142066_()}. The
 *       {@code isAwaken() &&} uses logical AND with the wrong
 *       polarity: at default spawn {@code isAwaken()=false}
 *       short-circuits to false, so the boss has no spawn-time
 *       damage immunity at all (a design regression vs. 3.16).
 *       After waking up, {@code isAwaken()=true} and the AND
 *       resolves to the super call, returning true — that is
 *       when canBeSeenAsEnemy short-circuits to false and
 *       golems stop being able to lock the boss.</li>
 * </ul>
 *
 * <h2>Fix</h2>
 *
 * <p>Replace the slim-jar override with a passthrough to
 * {@code super.m_142066_()} (vanilla canBeSeenAsEnemy), which is
 * always true on a live boss. This works for both versions even
 * though {@code m_142066_} resolves to different methods in
 * each — in both cases {@code super.m_142066_()} ends up calling
 * vanilla {@link net.minecraft.world.entity.LivingEntity
 * #canBeSeenAsEnemy()} (true when alive), so the value is
 * correct.</p>
 *
 * <p>Resulting behavior on 3.16: golems can now lock the boss
 * at default spawn, can damage it through the normal
 * {@code canBlockDamageSource} path. (The 3.16 spawn-time
 * damage-immunity design is intentionally sacrificed — that
 * path was gated on {@code isSleep()}, which was permanently
 * true on 3.16 anyway.)</p>
 *
 * <p>Resulting behavior on 3.31: {@code isSleep()} now returns
 * {@code super.m_142066_()=true} at default spawn — restoring
 * the intended spawn-time damage immunity that 3.31's
 * inverted-logic bug had broken. After the boss wakes up, the
 * boss becomes hittable and golems can lock it.</p>
 *
 * <h2>Notes on names and visibility</h2>
 *
 * <p>Cataclysm ships as a slim jar in 3.16 and 3.31, so the
 * method is named {@code m_142066_} in the runtime class — we
 * override it under that SRG name. {@code remap = false} on
 * both {@link Mixin} and {@link Overwrite} tells the mixin
 * processor to skip the srg remap step (which would otherwise
 * fail for a third-party mod class not present in the srg
 * table).</p>
 *
 * <p>This mixin is marked {@link Pseudo} so players without
 * Cataclysm installed are not affected; the mixin silently
 * no-ops when the target class is absent.</p>
 *
 * @author mgdp
 */
@Pseudo
@Mixin(value = Wadjet_Entity.class, remap = false)
public abstract class WadjetIsSleepMixin {

    /**
     * @author mgdp
     * @reason See class javadoc. Replace the slim-jar override
     *         (either {@code !isSleep() && super.m_142066_()}
     *         on 3.16 or {@code isAwaken() && super.m_142066_()}
     *         on 3.31) with a passthrough to vanilla
     *         canBeSeenAsEnemy so the boss is always a valid
     *         target for modular golems.
     */
    @Overwrite(remap = false)
    public boolean m_142066_() {
        return true;
    }
}
