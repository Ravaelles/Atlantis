package atlantis.battle.strategy;

import atlantis.battle.escape.EscapeDestination;
import atlantis.battle.escape.EscapeRays;
import atlantis.battle.kite.CombatTuning;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.move.MoveAway;
import atlantis.units.special.SleepUntilFrameGate;

import java.util.List;

/**
 * Duel kiting (NEXT #169): while the weapon cools, step off every melee
 * threat to weapon range minus a step - unless it shows its back. Dragoon
 * scope like the margin rule; Zealots want contact.
 *
 * <p>Why a separate rule next to the margin: a healthy Dragoon holds to
 * ~1 tile and shoots through the cooldown, which is correct DPS racing
 * on open ground but donates hits in a duel (Wine 1v1: WIN with 10 hits
 * taken, target under 2). When the shot cannot fire now, the ground is
 * free - yield it.
 */
public final class DuelKite {

    /** Duel scale: baseless fights at or below this headcount kite, bigger ones hold the line. */
    public static final int MAX_DUEL_COMBATANTS = 4;

    /** Live-measured dragoon windup (DuelMemory: damage lands ~cd 22 on a 30f cooldown). The sim runs 6 (dead WINDUP keys, #186 slice 3). */
    private static final int WINDUP_FRAMES = 8;

    /** Measured full-speed braking glide (#186 slice 3 pins: 6.0px over 7 ticks). Conservative below full speed. */
    private static final double BRAKING_PX = 6.0;

    /** Hold margin for order-issue slop: the same 2f cover as the imminent-shot gate above (measured re-issue refusal at 1-2f). */
    private static final int HOLD_MARGIN_FRAMES = 2;

    /**
     * A/B wiring switch (NEXT #186): when true, {@link #shouldYield}
     * answers the pre-wiring formula (in hold = yield) and skips the
     * hold-early race. Test scope only - the sim batch flips it per
     * arm, live always runs false. Volatile: the batch reuses the JVM
     * across arms, and a stale true would silently run every later
     * arm unwired.
     */
    public static volatile boolean holdEarlyDisabledForAB = false;

    private DuelKite() {
    }

    public static boolean applies(AUnit unit) {
        return unit != null && unit.isDragoon() && BattleSetup.isDuel();
    }

    /**
     * Step off while cooling, unless the threat flees (then chase, never
     * yield ground to a runner) or the shot is imminent (never trade DPS
     * for ground).
     */
    public static boolean shouldYield(AUnit unit, AUnit threat) {
        if (unit == null || threat == null || !unit.isDragoon()) return false;
        if (!BattleSetup.isDuel()) return false;
        if (unit.enemyWeaponRangeAgainstThisUnit(threat) > 1) return false;
        if (unit.cooldown() <= SleepUntilFrameGate.latencyFrames() + 2) return false;
        if (threat.isMoving() && !unit.isOtherFacingThisUnit(threat)) return false;

        if (unit.distTo(threat) >= holdDistance(unit)) return false;

        // A/B switch (#186): pre-wiring formula, exactly as before slice 5.
        if (holdEarlyDisabledForAB) return unit.distTo(threat) < holdDistance(unit);

        // Hold-early (#186): a committed chaser faces us, so closing runs
        // at its top speed head-on; at an angle the true closing is lower
        // and this assumption only ever yields earlier (conservative).
        // Foe reach is edge-to-edge pixels (tile truncation rounds melee
        // bodies to zero, which would hold into trades).
        double closingPx = threat.maxSpeed();
        double effectiveRangePx = ProtossAttackHoldToShoot.effectiveRange(
            unit.enemyWeaponRangeAgainstThisUnit(threat) * 32 + unit.type().bodyRadiusPx(),
            closingPx,
            WINDUP_FRAMES,
            BRAKING_PX);

        return !ProtossAttackHoldToShoot.shouldHoldEarly(
            unit.distTo(threat) * 32,
            effectiveRangePx,
            unit.cooldown(),
            SleepUntilFrameGate.latencyFrames(),
            closingPx,
            HOLD_MARGIN_FRAMES);
    }

    public static double holdDistance(AUnit unit) {
        return unit.groundWeaponRange() - CombatTuning.current().getKiteStandoffInsetTiles();
    }

    /** Step toward the hold point: escape-scored first, MoveAway when nothing scores. */
    public static APosition yieldStep(AUnit unit, AUnit threat) {
        double gap = holdDistance(unit) - unit.distTo(threat);
        if (gap <= 0) return null;

        double stepLen = stepLength(unit, gap);
        if (stepLen <= 0) return null;

        int step = Math.max(1, (int) Math.round(stepLen));
        APosition dest = atlantis.battle.escape.EscapeScoredStep.pickScored(unit, threat, step);
        if (dest != null) return dest;

        return fallbackStep(unit, threat, stepLen);
    }

    /** Same decision with caller-supplied context: the unit-testable seam. */
    public static APosition yieldStep(
        AUnit unit, AUnit threat, EscapeRays.Walkability walk,
        List<? extends HasPosition> blockers, List<? extends AUnit> foes
    ) {
        double gap = holdDistance(unit) - unit.distTo(threat);
        if (gap <= 0) return null;

        double stepLen = stepLength(unit, gap);
        if (stepLen <= 0) return null;

        int step = Math.max(1, (int) Math.round(stepLen));
        APosition dest = EscapeDestination.pickEscape(unit, threat, step, walk, blockers, foes);
        if (dest != null) return dest;

        APosition fallback = MoveAway.bestDestination(unit, threat, stepLen, true);
        if (fallback != null && EscapeDestination.violatesBuildingClearance(fallback, blockers)) {
            return null;
        }

        return fallback;
    }

    /**
     * Step length (owner, Steamhammer loss): swamped Dragoons break
     * contact first, margin second. Three or more melee inside 2.5
     * tiles means the pack closes faster than 2-tile steps open -
     * take the full gap (up to the long 5) instead of donating the
     * DPS race one capped step at a time. Open ground keeps the
     * measured 2.0 cap (#152 grid: longer steps buy nothing there).
     */
    private static double stepLength(AUnit unit, double gap) {
        if (isSwamped(unit)) return Math.min(MoveAway.LONG_STEP_TILES, gap);

        return Math.min(CombatTuning.current().getKiteStepTiles(), gap);
    }

    /** Swamped: 3+ melee enemies inside 2.5 tiles of this Dragoon. */
    public static boolean isSwamped(AUnit unit) {
        if (unit == null || !unit.isDragoon()) return false;

        try {
            return atlantis.units.AUnitNearCounts.meleeEnemiesNearCount(unit, 2.5) >= 3;
        } catch (Exception | Error ignored) {
            return false;
        }
    }

    private static APosition fallbackStep(AUnit unit, AUnit threat, double stepLen) {
        java.util.List<HasPosition> blockers;
        try {
            blockers = new java.util.ArrayList<>(atlantis.battle.escape.EscapeScoredStep.blockers(unit));
        } catch (Exception | Error ignored) {
            blockers = new java.util.ArrayList<>();
        }

        APosition fallback = MoveAway.bestDestination(unit, threat, stepLen, true);
        if (fallback != null && EscapeDestination.violatesBuildingClearance(fallback, blockers)) {
            return null;
        }

        return fallback;
    }


    /** Shared cadence with every other step: re-issues at most every 10 frames, rides between. */
    public static boolean yieldNow(int now, Integer lastStep) {
        return lastStep == null || now - lastStep >= CombatTuning.current().getKiteRestepsEveryFrames();
    }
}
