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

    private static final Map<Integer, Integer> lastStepFrame = new HashMap<>();
    private static final Map<Integer, Integer> lastMovedFrame = new HashMap<>();
    private static final Map<Integer, APosition> lastMovedPos = new HashMap<>();
    private static final Map<Integer, Integer> lastRepositionFrame = new HashMap<>();
    private static final Map<Integer, APosition> lastContactPos = new HashMap<>();
    private static final Map<Integer, Integer> lastContactFrame = new HashMap<>();
    private static final Map<Integer, Integer> lastGhostFrame = new HashMap<>();

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

    static void snapshotContact(AUnit unit, APosition position) {
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
}
