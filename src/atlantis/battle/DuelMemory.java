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
    public static final int PRE_CALIBRATION_SETTLE_FRAMES = 6;

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

    /**
     * Battle-hook hp snapshot for the damage gate (NEXT #183).
     * UnitStateManager appends hp() BEFORE BattleCommander runs each frame
     * (and skips frames with an order in flight), so comparing against
     * that history reads false for a steady holder by construction:
     * the stored value already equals current hp. This snapshot runs
     * inside the battle hook instead, after every decision: damage taken
     * since the previous battle frame latches here, independent of
     * manager cadence or order dedup.
     */
    private static final Map<Integer, Integer> lastBattleHp = new HashMap<>();
    private static final Map<Integer, Boolean> battleDamage = new HashMap<>();
    private static final Map<Integer, Integer> lastBattleDamageFrame = new HashMap<>();

    /**
     * Snapshots current hp and latches whether it dropped since the
     * previous battle frame. Called once per unit per frame from the
     * battle hook, before any decision reads the latch below.
     */
    public static void noteBattleFrame(AUnit unit) {
        try {
            if (unit == null || !unit.isAlive()) return;

            int now = unit.hp();
            Integer prev = lastBattleHp.get(unit.id());
            boolean dropped = prev != null && now < prev;
            battleDamage.put(unit.id(), dropped);
            if (dropped) lastBattleDamageFrame.put(unit.id(), A.now());
            lastBattleHp.put(unit.id(), now);
        } catch (Exception ignored) {
        }
    }

    /**
     * Window version of the damage latch (NEXT #183): true when hp
     * dropped within the last {@code framesAgo} battle frames. Replaces
     * the manager-history {@code lastUnderAttackLessThanAgo} for
     * battle-observed units, for which that history reads fresh by
     * construction and can never fire.
     */
    public static boolean damagedLessThanAgo(AUnit unit, int framesAgo) {
        if (unit == null) return false;

        try {
            Integer last = lastBattleDamageFrame.get(unit.id());

            return last != null && A.now() - last <= framesAgo;
        } catch (Exception ignored) {
            return false;
        }
    }

    /** True when hp dropped between the last two battle-hook snapshots. */
    public static boolean tookDamageSinceLastBattleFrame(AUnit unit) {
        if (unit == null) return false;

        try {
            return battleDamage.getOrDefault(unit.id(), false);
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Finished shooting (owner): one of our observed projectiles is 0-2
     * frames old, so the damage is committed and the attacker is free.
     * The window mirrors the old attack-state determinator (bullet age
     * 1-2 meant FINISHED), extended to the firing frame itself: the
     * bullet entity exists, so the shot is launched whatever the
     * animation still shows. Age 3+ is a previous cycle. No bullet,
     * no latch - fail-closed, the swing hold and the settle below keep
     * today's behavior.
     */
    public static boolean finishedShooting(AUnit unit) {
        if (unit == null || !unit.isAlive()) return false;

        try {
            int age = unit.lastBulletAge();

            return age >= 0 && age <= 2;
        } catch (Exception ignored) {
            return false;
        }
    }

    private DuelMemory() {
    }

    public static void clear() {
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
        lastBattleHp.clear();
        battleDamage.clear();
        lastBattleDamageFrame.clear();
    }

    /** Another step rode on this unit this frame - the re-arm must not cancel it. */
    public static void markRepositioned(AUnit unit) {
        lastRepositionFrame.put(unit.id(), A.now());
    }

    /** Frames since a reposition rode, or a large number when never. */
    public static int repositionedAgo(AUnit unit) {
        return A.now() - lastRepositionFrame.getOrDefault(unit.id(), -1000000);
    }

    /** Last engaged point while visible, or null when this unit never fought. */
    public static APosition lastContact(AUnit unit) {
        return lastContactPos.get(unit.id());
    }

    /** Frame of the last visible contact snapshot, or null when never fought. */
    public static Integer lastContactFrame(AUnit unit) {
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

    public static Integer lastStepFrame(AUnit unit) {
        return lastStepFrame.get(unit.id());
    }

    public static void stepped(AUnit unit, int now) {
        lastStepFrame.put(unit.id(), now);
        lastRepositionFrame.put(unit.id(), now);
    }

    public static Integer lastGhostFrame(AUnit unit) {
        return lastGhostFrame.get(unit.id());
    }

    public static void noteGhost(AUnit unit) {
        lastGhostFrame.put(unit.id(), A.now());
    }

    public static Integer lastMovedFrame(AUnit unit) {
        return lastMovedFrame.get(unit.id());
    }

    public static APosition lastMovedPos(AUnit unit) {
        return lastMovedPos.get(unit.id());
    }

    public static void noteMoved(AUnit unit, int now, APosition position) {
        lastMovedFrame.put(unit.id(), now);
        lastMovedPos.put(unit.id(), position);
    }

    public static void forgetMotion(AUnit unit) {
        lastStepFrame.remove(unit.id());
        lastMovedFrame.remove(unit.id());
        lastMovedPos.remove(unit.id());
    }

    /**
     * Post-swing calibration (PurpleWave AttackDelay): the attack
     * animation just opened - remember when, so the settle window can
     * be measured instead of guessed.
     */
    public static void openAttackAnim(AUnit unit, int now) {
        attackAnimOpenFrame.put(unit.id(), now);
        lastAttackStartFrame.put(unit.id(), now);
    }

    public static boolean attackAnimOpen(AUnit unit) {
        return attackAnimOpenFrame.containsKey(unit.id());
    }

    /**
     * The animation just closed: the settle window is the observed
     * duration plus PurpleWave's empirical +2. Stored per unit, so a
     * dragoon's long swing and a zealot's short one calibrate apart.
     */
    public static void closeAttackAnim(AUnit unit, int now) {
        Integer open = attackAnimOpenFrame.remove(unit.id());
        if (open != null && now - open > 0) {
            attackSettleFrames.put(unit.id(), now - open + 2);
        }
    }

    public static Integer lastAttackStart(AUnit unit) {
        return lastAttackStartFrame.get(unit.id());
    }

    /** Observed settle window, or the pre-calibration default. */
    public static int attackSettle(AUnit unit) {
        return attackSettleFrames.getOrDefault(unit.id(), PRE_CALIBRATION_SETTLE_FRAMES);
    }
}
