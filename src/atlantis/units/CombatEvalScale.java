package atlantis.units;

/**
 * The scale of {@link AUnit#eval()}, and the properties the number must keep no
 * matter which tweaks ran before it and whatever doctrine reads it afterwards.
 *
 * <p>This lives in {@code atlantis.units} rather than next to the evaluator in
 * {@code atlantis.combat.eval} for a boundary reason: {@code AUnit.eval()} has to
 * apply the our-side hedge below, and {@code atlantis.units} may not depend on
 * {@code atlantis.combat} (REVIEW §16 Stage E). The evaluator depends on units all
 * day; the other way round is the frozen edge. A pure-arithmetic helper on both
 * sides of that boundary belongs on the side that is allowed to be reached.</p>
 *
 * <h2>Which way is up</h2>
 * Higher is better, for both sides' own readings. eval() is
 * {@code enemyScore / (ourScore + 0.001)} over cost-like negative side scores, and
 * a side's score is what it <b>lost</b> in the simulated window - so the ratio reads
 * "how much did the enemy lose, divided by how much did we lose". Measured on the
 * stub world: three Marines next to one Zergling, a fight we win comfortably,
 * score 1.667; one Marine next to one Zealot, a fight we lose, score 0.130. As a
 * rule of thumb: 0.5 means about half as strong as the enemy, 2.0 about twice as
 * strong. For an
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
     * What we take off our own reading before anything compares it to a threshold.
     *
     * <p>The doctrine this encodes (the owner's, 2026-10-04): a fight that reads
     * 1.01 is one we would probably win, which makes it exactly the fight we should
     * not walk into. So "even" has to mean "we are clearly ahead" - the raw 1.0 reads
     * 0.7, and only a raw 1.3 reads as 1.0.</p>
     *
     * <p>Subtracting (rather than scaling) is what makes the hedge bite hardest where
     * the decisions are hard: at 1.0 it takes 30% off the margin, at 5.0 it takes 6%.
     * Every one of the 236 call sites compares against an unchanged threshold, so
     * every {@code eval >= x} guard fires later and every {@code eval <= x} guard
     * stops firing later - all of them stricter, none of them relaxed.</p>
     *
     * <p>It applies to <b>our</b> readings only. An enemy unit's number already runs
     * the other way, and the failure mode B-18's owner describes from the game is
     * understating the enemy - so shifting that number down would make it worse.</p>
     */
    public static final double OUR_SIDE_HEDGE = 0.3;

    /**
     * The number production compares against thresholds: our own reading, hedged.
     */
    /**
     * What {@link AUnit#eval()} returns when the simulation has nothing to simulate:
     * no enemy within reach that has a weapon and is not immobilized. The evaluator
     * returns the pair {@code {9874, -9874}} for that case, so the relative reading is
     * the same "infinitely better" number a real fight reaches by winning every
     * exchange - which is why it is a *named* value and not a literal, and why
     * {@link AUnit#hasEnemyForEval()} exists next to it. {@link AUnit#eval()} then takes
     * {@link #OUR_SIDE_HEDGE} off it like any other reading, so a unit of ours answers
     * {@code 9873.7} and an enemy unit 9874.0.
     *
     * <p>Every {@code eval() >= x} guard reads this as "we are strong, proceed". That is
     * harmless in most places (if there is no enemy in reach there is usually nothing to
     * walk towards) and wrong where the guard is about a <i>chosen</i> target that may be
     * out of reach - chasing is the case B-1's audit step 2 is about, and
     * {@code ProtossMissionDefendAllowsToAttack.allowsToAttackEnemyUnit} is its first
     * measured row.</p>
     */
    public static final double NO_ENEMY_IN_REACH = 9874.0;

    public static double hedgedForOurSide(double eval) {
        return signSafe(eval - OUR_SIDE_HEDGE);
    }

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