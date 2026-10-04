package atlantis.combat.eval;

/**
 * The scale of {@link atlantis.units.AUnit#eval()}, and the properties the number
 * must keep no matter which tweaks ran before it.
 *
 * <h2>Which way is up</h2>
 * Higher is better, for both sides' own readings. eval() is
 * {@code enemyScore / (ourScore + 0.001)} over cost-like negative side scores, and
 * a side's score is what it <b>lost</b> in the simulated window - so the ratio reads
 * "how much did the enemy lose, divided by how much did we lose". Measured on the
 * stub world: three Marines next to one Zergling, a fight we win comfortably,
 * score 1.667; one Marine next to one Zealot, a fight we lose, score 0.130. For an
 * <b>enemy</b> unit the same formula runs from that unit's side, so its number runs
 * the other way - high means the enemy is doing well, which is bad for us. When
 * nothing is in reach the pair is {@code {9874, -9874}} and eval() returns 9874.0:
 * the same "infinitely better" reading, reached by a shortcut.
 *
 * <h2>Why the floor exists</h2>
 * The Protoss tweaks are additive and reach -0.7 in total, so they could take a
 * ratio through zero, and a number that changes sign is not a strength comparison
 * any more. Measured 2026-10-04 on the same stub world, a lone Wraith against two
 * discovered Photon Cannons: the raw ratio is 0.0211 - the cannons lost 16, the
 * Wraith lost 760, so "we are losing 47 to 1", which is exactly right - and as
 * Protoss it comes out at <b>-0.3789</b> after -0.1 for enemy buildings near, -0.3
 * for two anti-air combat buildings and the choke/cohesion terms.
 *
 * <p>No threshold can read that. {@code eval() >= 1.2} (12 call sites) would say
 * "not a good fight" for a number that is not a fight at all, and {@code eval() <=
 * 2.5} would say "no better than even". Flooring does not make any guard stricter -
 * 0.01 is still below 0.3, the smallest number production compares against - it
 * restores the one reading the ratio can honestly take, which for this fight is
 * "as bad as it gets". Whether the thresholds around it are the right thresholds
 * for an unbounded ratio is B-1, and it stays open.
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