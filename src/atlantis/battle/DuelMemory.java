package atlantis.battle;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;

import java.util.HashMap;
import java.util.Map;

/**
 * Per-unit combat memory for the {@link FightTarget} frame pipeline
 * (__SOLID.md item 22): step cadence, motion watch, reposition rides,
 * visible-contact snapshots and the ghost-log throttle.
 *
 * <p>Static keyed by unit id, exactly like the pipeline that reads it -
 * this is a file-level split, not a lifecycle change: the pipeline keeps
 * deciding, this only remembers. {@link FightTarget} forwards its four
 * memory methods here, so no caller changes. Cleared with the world in
 * tests and on every new game.</p>
 */
public final class DuelMemory {

    /** Settle window before any swing was ever observed (covers a zealot swing + margin). */
    static final int PRE_CALIBRATION_SETTLE_FRAMES = 6;

    private static final Map<Integer, Integer> lastStepFrame = new HashMap<>();
    private static final Map<Integer, Integer> lastMovedFrame = new HashMap<>();
    private static final Map<Integer, APosition> lastMovedPos = new HashMap<>();
    private static final Map<Integer, Integer> lastRepositionFrame = new HashMap<>();
    private static final Map<Integer, APosition> lastContactPos = new HashMap<>();
    private static final Map<Integer, Integer> lastContactFrame = new HashMap<>();
    private static final Map<Integer, Integer> lastGhostFrame = new HashMap<>();
    private static final Map<Integer, Integer> attackAnimOpenFrame = new HashMap<>();
    private static final Map<Integer, Integer> lastAttackStartFrame = new HashMap<>();
    private static final Map<Integer, Integer> attackSettleFrames = new HashMap<>();

    private DuelMemory() {
    }

    static void clear() {
        lastStepFrame.clear();
        lastMovedFrame.clear();
        lastMovedPos.clear();
        lastRepositionFrame.clear();
        lastContactPos.clear();
        lastContactFrame.clear();
        lastGhostFrame.clear();
        attackAnimOpenFrame.clear();
        lastAttackStartFrame.clear();
        attackSettleFrames.clear();
    }

    /** Another step rode on this unit this frame - the re-arm must not cancel it. */
    static void markRepositioned(AUnit unit) {
        lastRepositionFrame.put(unit.id(), A.now());
    }

    /** Frames since a reposition rode, or a large number when never. */
    static int repositionedAgo(AUnit unit) {
        return A.now() - lastRepositionFrame.getOrDefault(unit.id(), -1000000);
    }

    /** Last engaged point while visible, or null when this unit never fought. */
    static APosition lastContact(AUnit unit) {
        return lastContactPos.get(unit.id());
    }

    /** Frame of the last visible contact snapshot, or null when never fought. */
    static Integer lastContactFrame(AUnit unit) {
        return lastContactFrame.get(unit.id());
    }

    /**
     * Contact memory is sight, not engagement: a visible target snapshots
     * here even when the engagement is refused below (imminent shot,
     * formation hold) - otherwise refusing to chase would also blind the
     * ghost march that closes the distance instead. Public: the
     * acquisition chain lives in another package.
     */
    public static void snapshotContact(AUnit unit, APosition position) {
        lastContactPos.put(unit.id(), position);
        lastContactFrame.put(unit.id(), A.now());
    }

    static Integer lastStepFrame(AUnit unit) {
        return lastStepFrame.get(unit.id());
    }

    static void stepped(AUnit unit, int now) {
        lastStepFrame.put(unit.id(), now);
        lastRepositionFrame.put(unit.id(), now);
    }

    static Integer lastGhostFrame(AUnit unit) {
        return lastGhostFrame.get(unit.id());
    }

    static void noteGhost(AUnit unit) {
        lastGhostFrame.put(unit.id(), A.now());
    }

    static Integer lastMovedFrame(AUnit unit) {
        return lastMovedFrame.get(unit.id());
    }

    static APosition lastMovedPos(AUnit unit) {
        return lastMovedPos.get(unit.id());
    }

    static void noteMoved(AUnit unit, int now, APosition position) {
        lastMovedFrame.put(unit.id(), now);
        lastMovedPos.put(unit.id(), position);
    }

    static void forgetMotion(AUnit unit) {
        lastStepFrame.remove(unit.id());
        lastMovedFrame.remove(unit.id());
        lastMovedPos.remove(unit.id());
    }

    /**
     * Post-swing calibration (PurpleWave AttackDelay): the attack
     * animation just opened - remember when, so the settle window can
     * be measured instead of guessed.
     */
    static void openAttackAnim(AUnit unit, int now) {
        attackAnimOpenFrame.put(unit.id(), now);
        lastAttackStartFrame.put(unit.id(), now);
    }

    static boolean attackAnimOpen(AUnit unit) {
        return attackAnimOpenFrame.containsKey(unit.id());
    }

    /**
     * The animation just closed: the settle window is the observed
     * duration plus PurpleWave's empirical +2. Stored per unit, so a
     * dragoon's long swing and a zealot's short one calibrate apart.
     */
    static void closeAttackAnim(AUnit unit, int now) {
        Integer open = attackAnimOpenFrame.remove(unit.id());
        if (open != null && now - open > 0) {
            attackSettleFrames.put(unit.id(), now - open + 2);
        }
    }

    static Integer lastAttackStart(AUnit unit) {
        return lastAttackStartFrame.get(unit.id());
    }

    /** Observed settle window, or the pre-calibration default. */
    static int attackSettle(AUnit unit) {
        return attackSettleFrames.getOrDefault(unit.id(), PRE_CALIBRATION_SETTLE_FRAMES);
    }
}
