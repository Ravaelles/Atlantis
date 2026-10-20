package atlantis.battle.strategy;

import atlantis.battle.kite.CombatTuning;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.actions.move.MoveAway;
import atlantis.units.special.SleepUntilFrameGate;

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

    /** Hold this far from melee: just outside their reach, inside ours. */
    public static final double STANDOFF_BELOW_RANGE_TILES = 0.4;

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
        return unit.distTo(threat) < holdDistance(unit);
    }

    public static double holdDistance(AUnit unit) {
        return unit.groundWeaponRange() - STANDOFF_BELOW_RANGE_TILES;
    }

    /** Step exactly the gap to the hold point, capped at one full step; null when already there. */
    public static APosition yieldStep(AUnit unit, AUnit threat) {
        double gap = holdDistance(unit) - unit.distTo(threat);
        if (gap <= 0) return null;
        double stepLen = Math.min(CombatTuning.current().getKiteStepTiles(), gap);
        if (stepLen <= 0) return null;
        return MoveAway.bestDestination(unit, threat, stepLen);
    }

    /** Shared cadence with every other step: re-issues at most every 10 frames, rides between. */
    public static boolean yieldNow(int now, Integer lastStep) {
        return lastStep == null || now - lastStep >= CombatTuning.current().getKiteRestepsEveryFrames();
    }
}
