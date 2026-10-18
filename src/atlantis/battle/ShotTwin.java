package atlantis.battle;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

/**
 * Names every attack acquisition and every animation hold (owner
 * 2026-10-19, Wine 1Drag_v_1Zeal: the Dragoon stopped shooting at the
 * end and no row said whether the animation guard latched or the
 * order never issued). Both rows throttle to one per unit per 30
 * frames on separate clocks, so a hold never suppresses the shot
 * that follows it.
 */
public class ShotTwin {

    private static final java.util.Map<Integer, Integer> lastFiredByUnit = new java.util.HashMap<>();
    private static final java.util.Map<Integer, Integer> lastHoldByUnit = new java.util.HashMap<>();

    private ShotTwin() {
    }

    public static void fired(AUnit unit, AUnit target) {
        int now = A.now();
        Integer last = lastFiredByUnit.get(unit.id());
        if (last != null && now - last < 30) return;
        lastFiredByUnit.put(unit.id(), now);
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
        lastFiredByUnit.clear();
        lastHoldByUnit.clear();
    }
}
