package atlantis.battle.cluster;

import atlantis.battle.kite.CombatTuning;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

/**
 * Cluster tactic arbitration (extracted from BattleCommander): one
 * gate decides FIGHT or FLEE per frame - breach, UMS opening and home
 * ground force FIGHT, otherwise the evaluator decides with a retreat
 * latch. Per-unit behavior never reads the latch directly; it only
 * receives the decided tactic.
 */
public final class ClusterTactic {

    private ClusterTactic() {
    }

    private static int fleeStartedAt = -1;

    /** Per-game reset next to the other static resets. */
    public static void reset() {
        fleeStartedAt = -1;
    }

    public static FightOrFlight.Tactic decide() {
        AUnit leader = Alpha.get().leader();
        AUnit assessor = nearestToFight(leader);
        double eval = assessor != null ? assessor.eval() : 0;

        // Breach rule (owner): raiders in the base mean fight - always. Two
        // workers chewing probes is not an evaluation the cluster can walk
        // away from, however lopsided the numbers read (the evaluator is
        // not even asked). The latch resets with it: a retreat ordered
        // before the breach does not drag the cluster home. The only
        // retreat on a breach is per-unit, below in act().
        // Same for a UMS opening: with no base anywhere, an early retreat
        // just feeds - stand the first 15 seconds and let the fight happen.
        if (ClusterBreach.breachForcesFight() || earlyUmsStandGround()) {
            fleeStartedAt = -1;

            return FightOrFlight.Tactic.FIGHT;
        }

        // Home ground holds: under two tiles from the main base the
        // cluster stands instead of fleeing (owner: retreating into the
        // mineral line donates the base). Individual kiting inside act()
        // still applies - this gates only the cluster tactic.
        if (holdsHomeGround()) {
            fleeStartedAt = -1;

            return FightOrFlight.Tactic.FIGHT;
        }

        // Stardust rule (owner 2026-10-23): never run through a blocking
        // army. If armed enemies sit between the cluster and home, the
        // cluster fights through instead - the latch does not apply,
        // dying beats oscillating.
        if (eval < FightOrFlight.FIGHT_EVAL && BlockingArmy.blocksWayHome()) {
            fleeStartedAt = -1;

            return FightOrFlight.Tactic.FIGHT;
        }

        FightOrFlight.Tactic tactic = FightOrFlight.decide(eval, A.now(), fleeStartedAt);
        if (tactic == FightOrFlight.Tactic.FLEE && fleeStartedAt < 0) {
            fleeStartedAt = A.now();
        }
        if (tactic == FightOrFlight.Tactic.FIGHT) {
            fleeStartedAt = -1;
        }
        return tactic;
    }
    /**
     * The UMS opening stand-ground (owner): on custom maps there is no
     * base to run to, so a retreat in the first 15 seconds is never the
     * answer - the unit would just feed. Public as a seam: the rule pins
     * in a stub state without running the frame.
     */
    public static boolean earlyUmsStandGround() {
        return atlantis.game.A.isUms() && atlantis.game.A.now() < CombatTuning.current().getUmsNoRetreatFrames();
    }
    /**
     * True when the cluster sits on the main base (under 2 tiles): FLEE
     * is not an option there. Public as a seam, pinned without a game.
     */
    public static boolean holdsHomeGround() {
        AUnit main = Select.main();
        if (main == null) return false;

        HasPosition center = Alpha.get().center();
        if (center == null) return false;

        return center.distTo(main) < 2;
    }
    /**
     * Who the gate listens to: the unit closest to an enemy within 15
     * tiles, else the leader. A leader at the back reads a different fight
     * than the vanguard taking contact (owner 2026-10-19: one cluster, one
     * area assessment - the gate answers for where the fight is, not for
     * where the leader stands). Public as a seam, pinned without a game.
     */
    public static AUnit nearestToFight(AUnit leader) {
        AUnit best = leader;
        double bestDist = Double.MAX_VALUE;
        for (AUnit unit : Alpha.get().selection().list()) {
            if (unit == null || !unit.isAlive()) continue;
            AUnit foe = atlantis.battle.targeting.ASquadTargeting.nearestEngageableEnemy(unit, 15);
            if (foe == null) continue;
            double dist = unit.distTo(foe);
            if (dist < bestDist) {
                best = unit;
                bestDist = dist;
            }
        }
        return best;
    }
}
