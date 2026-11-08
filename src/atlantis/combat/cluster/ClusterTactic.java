package atlantis.combat.cluster;

import atlantis.battle.cluster.Alpha;
import atlantis.battle.kite.CombatTuning;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;

/**
 * Cluster tactic arbitration (extracted from BattleCommander): one
 * gate decides FIGHT or FLEE per frame - breach, UMS opening and home
 * ground force FIGHT, otherwise the evaluator decides with a retreat
 * latch. Per-unit behavior never reads the latch directly; it only
 * receives the decided tactic.
 */
public final class ClusterTactic {

    private static int fleeStartedAt = -1;

    private ClusterTactic() {
    }

    /**
     * Per-game reset next to the other static resets.
     */
    public static void reset() {
        fleeStartedAt = -1;
    }

    public static FightOrFlight.Tactic decide() {
        // Strong army never retreats (owner 2026-10-29): at double the
        // enemy strength the cluster stands and fights. FLEE at 200% sat
        // a finished army in the base, oscillating with the retreat
        // instead of marching out.
        if (strongArmyStands()) {
            fleeStartedAt = -1;

            return FightOrFlight.Tactic.FIGHT;
        }

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

        // Multi-base hold (owner 2026-11-08): past two bases the cluster
        // never trades the natural for a retreat home - with the ball
        // closer to the main than to the natural by ground, FLEE walks
        // away from the base under attack instead of toward the fight.
        if (multiBaseHoldAgainstRetreat()) {
            fleeStartedAt = -1;

            return FightOrFlight.Tactic.FIGHT;
        }

        // Natural stand (owner 2026-11-08): 15+ combat units within 20
        // ground tiles of the natural at 90%+ relative strength stand
        // and fight - only FIGHT is allowed. The weak-early-Zerg veto
        // below stays for genuinely outmatched openers; this owns the
        // held-natural shape, so it runs before it.
        if (naturalStandWithStrength()) {
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

        // No early attacking vs Zerg without Dragoons while weak (owner
        // 2026-10-29): an early trade without ranged units donates the
        // game. Force the retreat instead of letting the evaluator trade.
        if (weakEarlyVsZergWithoutDragoons()) {
            if (fleeStartedAt < 0) fleeStartedAt = A.now();

            return FightOrFlight.Tactic.FLEE;
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
        AUnit main = HomeGround.resolve().main();
        if (main == null) return false;

        HasPosition center = Alpha.get().center();
        if (center == null) return false;

        return center.distTo(main) < 2;
    }

    /**
     * Past two bases, closer to the main than to the natural by ground:
     * FLEE is not an option there - the ball is already homeward of the
     * natural, so retreating abandons the base under attack instead of
     * joining its fight. Public as a seam, pinned without a game.
     */
    public static final int MULTI_BASE_HOLD_BASES = 2;

    public static boolean multiBaseHoldAgainstRetreat() {
        try {
            if (atlantis.units.select.Select.ourBasesWithUnfinished().count()
                <= MULTI_BASE_HOLD_BASES) return false;

            HasPosition center = Alpha.get().center();
            AUnit main = HomeGround.resolve().main();
            AUnit natural = atlantis.map.base.Bases.natural();
            if (center == null || main == null || main.position() == null
                || natural == null || natural.position() == null) return false;

            return center.groundDist(main.position()) < center.groundDist(natural.position());
        } catch (Exception | Error ignored) {
            return false;
        }
    }

    /**
     * 15+ combat units within 20 ground tiles of the natural at 90%+
     * relative strength: FLEE is not an option there, only FIGHT.
     * Public as a seam, pinned without a game.
     */
    public static final int NATURAL_STAND_UNITS = 15;
    public static final double NATURAL_STAND_DIST = 20;
    public static final int NATURAL_STAND_STRENGTH = 90;

    public static boolean naturalStandWithStrength() {
        try {
            if (atlantis.units.select.Count.ourCombatUnits() <= NATURAL_STAND_UNITS) return false;
            if (atlantis.information.generic.Army.strength() < NATURAL_STAND_STRENGTH) return false;

            HasPosition center = Alpha.get().center();
            AUnit natural = atlantis.map.base.Bases.natural();
            if (center == null || natural == null || natural.position() == null) return false;

            return center.groundDist(natural.position()) <= NATURAL_STAND_DIST;
        } catch (Exception | Error ignored) {
            return false;
        }
    }

    /**
     * No early attacking vs Zerg without Dragoons while weak (owner
     * 2026-10-29): Zerg x early game x zero standing Dragoons x hedged
     * strength under 155 forces the retreat. Public as a seam, pinned
     * without a game.
     */
    /**
     * At or above this relative strength the cluster never flees (owner
     * 2026-10-29): double the enemy means stand and fight. Public as a
     * seam, pinned without a game.
     */
    public static final int STRONG_ARMY_NEVER_FLEE = 200;

    public static boolean strongArmyStands() {
        try {
            return atlantis.information.generic.Army.strength() >= STRONG_ARMY_NEVER_FLEE;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static final int WEAK_EARLY_ZERG_STRENGTH = 155;

    public static boolean weakEarlyVsZergWithoutDragoons() {
        try {
            if (!atlantis.game.player.Enemy.zerg()) return false;
            if (!atlantis.information.strategy.GamePhase.isEarlyGame()) return false;
            if (atlantis.units.select.Count.dragoons() > 0) return false;

            return atlantis.information.generic.ArmyStrength.ourArmyRelativeStrength()
                < WEAK_EARLY_ZERG_STRENGTH;
        } catch (Exception ignored) {
            return false;
        }
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
            AUnit foe = atlantis.combat.targeting.Engagement.nearestEngageableEnemy(unit, 15);
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
