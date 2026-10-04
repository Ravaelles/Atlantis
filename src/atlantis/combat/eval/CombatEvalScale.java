package atlantis.combat.eval;

/**
 * The scale of {@link atlantis.units.AUnit#eval()}, and the properties the number
 * must keep no matter which tweaks ran before it.
 *
 * <p>eval() is an unbounded ratio: {@code enemyScore / (ourScore + 0.001)}. 1.0 is
 * an even fight, lower means we are stronger, higher means the enemy is. There is
 * no ceiling and no floor by nature - but the additive tweaks that run after the
 * ratio ({@code ProtossJfapTweaksConsiderChokesEtc}, up to -0.4 in total) used to
 * push it straight through zero, and a ratio that changes sign is not a strength
 * comparison any more.</p>
 *
 * <p>Measured 2026-10-04 on the stub world, a lone Wraith against two discovered
 * Photon Cannons: as Terran the raw ratio is 0.0066, as Protoss it comes out at
 * <b>-0.3789</b> - the raw ratio minus -0.1 for enemy buildings near and -0.3 for
 * two anti-air combat buildings. A guard of the form {@code eval() < 0.5} ("we are
 * much stronger") is satisfied by -0.3789, for a fight the wraith loses.</p>
 *
 * <p>Flooring does not make such a guard stricter - 0.01 is still below 0.5 - and it
 * does not make the evaluator right. What it removes is the sign break: a value
 * that means nothing can no longer be handed to a comparison, and the worst answer
 * the evaluator can give is a defined one ("as bad as it gets") rather than a
 * negative ratio that reads like an advantage. Whether the thresholds around it are
 * the right thresholds for an unbounded ratio is B-1, and it stays open.</p>
 */
public class CombatEvalScale {

    /**
     * The lowest value eval() is allowed to take. Small, not zero: 0.0 is what a
     * "nothing to fight" shortcut can produce, and a real fight is never exactly
     * free.
     */
    public static final double FLOOR = 0.01;

    /**
     * The tweaked value, guaranteed positive.
     *
     * <p>Applied after every tweak, on both sides, because that is the only place
     * where the sign can still be broken.</p>
     */
    public static double signSafe(double eval) {
        return Math.max(FLOOR, eval);
    }
}