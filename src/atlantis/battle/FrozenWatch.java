package atlantis.battle;

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

    /** How often one unit may log its frozen state (the painter has no throttle). */
    private static final int LOG_EVERY_FRAMES = 150;

    private static final Map<Integer, APosition> lastPos = new HashMap<>();
    private static final Map<Integer, Integer> stillFrames = new HashMap<>();
    private static final Map<Integer, Integer> lastLogFrame = new HashMap<>();

    private FrozenWatch() {
    }

    public static void clearForTests() {
        lastPos.clear();
        stillFrames.clear();
        lastLogFrame.clear();
    }

    /** Per-frame sample, next to the other per-unit twins in the battle hook. */
    public static void note(AUnit unit) {
        if (unit == null || !unit.isAlive() || unit.position() == null) return;
        try {
            APosition last = lastPos.get(unit.id());
            if (last != null && unit.distTo(last) < STILL_TILES) {
                stillFrames.put(unit.id(), stillFrames.getOrDefault(unit.id(), 0) + 1);
            }
            else {
                stillFrames.put(unit.id(), 0);
            }
            lastPos.put(unit.id(), unit.position());
            if (isFrozen(unit)) maybeLog(unit);
        } catch (Exception ignored) {
        }
    }

    public static boolean isFrozen(AUnit unit) {
        if (unit == null || !unit.isAlive()) return false;
        try {
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
}
