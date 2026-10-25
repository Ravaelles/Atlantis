package atlantis.combat;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;

import java.util.HashMap;
import java.util.Map;

/**
 * Frozen-unit detection (owner 2026-10-26): a unit ordered to move or
 * attack that stops displacing is a stuck engine order, not a decision -
 * the classic StarCraft freeze (a dragoon held a move mission standing
 * still until Hold Position was pressed on it by hand). Still means no
 * position change for {@link #FROZEN_FRAMES} frames with a move/attack
 * order riding and no cooldown running. Units trading blows are
 * excluded: an attacker whose target is in weapon range stands still
 * on purpose.
 *
 * <p>Passive diagnostics: {@code note} rides the per-frame battle hook,
 * the painter draws the purple rings and a throttled twin row names the
 * unit for headless logs. Nothing here issues orders.</p>
 */
public final class FrozenWatch {

    /** No displacement this long with an order riding: frozen, not holding. */
    public static final int FROZEN_FRAMES = 40;

    /** Position jitter below this still counts as standing still. */
    public static final double STILL_TILES = 0.3;

    /**
     * Displacement a unit must bank before it can read frozen: the
     * opening march issues orders before the engine spins movement up,
     * and flagging the whole army at frame 40 drowns the signal (live:
     * all 9 units flagged at f40 while still deploying). A unit that
     * moved and then stops with an order riding is the stuck shape.
     */
    public static final double DEPLOYED_TILES = 3.0;

    /** How often one unit may log its frozen state (the painter has no throttle). */
    private static final int LOG_EVERY_FRAMES = 150;

    private static final Map<Integer, APosition> lastPos = new HashMap<>();
    private static final Map<Integer, Integer> stillFrames = new HashMap<>();
    private static final Map<Integer, Integer> lastLogFrame = new HashMap<>();
    private static final Map<Integer, Double> deployedTiles = new HashMap<>();
    private static final Map<Integer, Integer> frozenStreak = new HashMap<>();
    private static final Map<Integer, Integer> lastErrorFrame = new HashMap<>();

    private FrozenWatch() {
    }

    public static void clearForTests() {
        lastPos.clear();
        stillFrames.clear();
        lastLogFrame.clear();
        deployedTiles.clear();
        frozenStreak.clear();
        lastErrorFrame.clear();
    }

    /** Per-frame sample, next to the other per-unit twins in the battle hook. */
    public static void note(AUnit unit) {
        if (unit == null || !unit.isAlive() || unit.position() == null) return;

        try {
            APosition last = lastPos.get(unit.id());
            if (last != null) {
                double step = unit.distTo(last);
                if (step < STILL_TILES) {
                    stillFrames.put(unit.id(), stillFrames.getOrDefault(unit.id(), 0) + 1);
                }
                else {
                    stillFrames.put(unit.id(), 0);
                    deployedTiles.put(unit.id(),
                        deployedTiles.getOrDefault(unit.id(), 0.0) + step);
                }
            }
            lastPos.put(unit.id(), unit.position());
            boolean frozen = frozenNow(unit);
            if (frozen) {
                int streak = frozenStreak.getOrDefault(unit.id(), 0) + 1;
                frozenStreak.put(unit.id(), streak);
                // Second consecutive freeze: error to the file (owner
                // 2026-10-26) - unit, last order and its age, still
                // frames. The twin row keeps flowing; the error fires
                // once per freeze episode and again after a reset.
                if (streak == 2) logFrozenError(unit);
                maybeLog(unit);
            }
            else {
                frozenStreak.put(unit.id(), 0);
            }
        } catch (Exception ignored) {
        }
    }

    public static boolean isFrozen(AUnit unit) {
        try {
            return frozenNow(unit);
        } catch (Exception ignored) {
            return false;
        }
    }

    /** Consecutive frozen notes; the error fires when this reaches 2. */
    public static int frozenStreak(AUnit unit) {
        if (unit == null) return 0;

        return frozenStreak.getOrDefault(unit.id(), 0);
    }

    /** Frame of the last frozen error for this unit, or null when never filed. */
    public static Integer frozenErrorFrame(AUnit unit) {
        if (unit == null) return null;

        return lastErrorFrame.get(unit.id());
    }

    private static boolean frozenNow(AUnit unit) {
        try {
            if (deployedTiles.getOrDefault(unit.id(), 0.0) < DEPLOYED_TILES) return false;
            if (stillFrames.getOrDefault(unit.id(), 0) < FROZEN_FRAMES) return false;
            if (!unit.isMoving() && !unit.isAttacking()) return false;
            if (unit.cooldown() > 0) return false;

            return !trading(unit);
        } catch (Exception ignored) {
            return false;
        }
    }

    /** Standing still with a live target inside weapon range: fighting, not frozen. */
    private static boolean trading(AUnit unit) {
        try {
            AUnit target = unit.target();
            if (target == null || !target.isAlive() || target.position() == null) return false;

            return unit.hasWeaponRangeToAttack(target, 0);
        } catch (Exception ignored) {
            return false;
        }
    }

    private static void maybeLog(AUnit unit) {
        try {
            int now = A.now();
            Integer last = lastLogFrame.get(unit.id());
            if (last != null && now - last < LOG_EVERY_FRAMES) return;

            lastLogFrame.put(unit.id(), now);
            atlantis.util.log.EventLog.key(atlantis.util.log.LogKey.FROZEN)
                .param("unit", unit.id())
                .param("type", unit.type() == null ? "?" : unit.type().name())
                .param("still", stillFrames.getOrDefault(unit.id(), 0))
                .param("order", unit.isMoving() ? "move" : "attack")
                .log(now, A.minSec());
        } catch (Exception ignored) {
        }
    }

    /**
     * Second consecutive freeze: error to the file (owner 2026-10-26) -
     * clock, unit, last order and its age, still frames. One line per
     * freeze episode; the organization hunt for the block starts here.
     */
    private static void logFrozenError(AUnit unit) {
        try {
            int now = A.now();
            lastErrorFrame.put(unit.id(), now);
            String order;
            try {
                order = unit.action() == null ? unit.lastCommandName() : unit.action().name();
            } catch (Exception ignored) {
                order = "?";
            }
            int ago;
            try {
                ago = unit.lastCommandIssuedAgo();
            } catch (Exception ignored) {
                ago = -1;
            }
            atlantis.util.AConsole.errPrintln(
                A.minSec() + " " + unit.typeWithUnitId()
                    + " FROZEN " + order + " " + ago + "f ago, still "
                    + stillFrames.getOrDefault(unit.id(), 0) + "f");
        } catch (Exception ignored) {
        }
    }
}
