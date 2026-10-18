package atlantis.battle;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

/**
 * Names every attack acquisition and every animation hold (owner
 * 2026-10-19, Wine 1Drag_v_1Zeal: the Dragoon stopped shooting at the
 * end and no row said whether the animation guard latched or the
 * order never issued). Holds throttle to one per unit per 30 frames;
 * acquisitions always row (exact cadence is the point - a throttled
 * shot count cannot tell a 30-frame cycle from a 60-frame one).
 */
public class ShotTwin {

    private static final java.util.Map<Integer, Integer> lastHoldByUnit = new java.util.HashMap<>();

    private ShotTwin() {
    }

    public static void fired(AUnit unit, AUnit target) {
        row(unit, target, "shot-fired");
    }

    public static void holding(AUnit unit, AUnit target) {
        int now = A.now();
        Integer last = lastHoldByUnit.get(unit.id());
        if (last != null && now - last < 30) return;
        lastHoldByUnit.put(unit.id(), now);
        row(unit, target, "holding-shot");
    }

    private static void row(AUnit unit, AUnit target, String result) {
        EventLog.key(LogKey.SHOT)
            .param("unit", unit.id())
            .param("dist", (int) (unit.distTo(target) * 10))
            .param("cooldown", unit.cooldown())
            .param("result", result)
            .log(A.now(), A.minSec());
    }

    public static void clearForTests() {
        lastHoldByUnit.clear();
    }
}
