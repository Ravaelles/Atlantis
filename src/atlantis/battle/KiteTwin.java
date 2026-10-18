package atlantis.battle;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

/**
 * Names every kiting step and every step that had nowhere to go (owner
 * 2026-10-19, Wine 1Drag_v_1Zeal follow-up: kite-move-kite, then a stop,
 * then trading at point blank - without rows the stop is guesswork).
 * The decision itself stays in {@link BattleCommander#shouldKite}; this
 * only records what happened, throttled so a long kite does not flood
 * the debug stream (one row per unit per 30 frames, stuck steps always).
 */
public class KiteTwin {

    private static final java.util.Map<Integer, Integer> lastStepFrameByUnit = new java.util.HashMap<>();

    private KiteTwin() {
    }

    public static void steppedAside(AUnit dragoon, AUnit threat) {
        int now = A.now();
        Integer last = lastStepFrameByUnit.get(dragoon.id());
        if (last != null && now - last < 30) return;
        lastStepFrameByUnit.put(dragoon.id(), now);
        row(dragoon, threat, "stepped-aside");
    }

    public static void nowhereToStep(AUnit dragoon, AUnit threat) {
        lastStepFrameByUnit.remove(dragoon.id());
        row(dragoon, threat, "stuck-no-step");
    }

    private static void row(AUnit dragoon, AUnit threat, String result) {
        EventLog.key(LogKey.KITE)
            .param("unit", dragoon.id())
            .param("dist", (int) (dragoon.distTo(threat) * 10))
            .param("cooldown", dragoon.cooldown())
            .param("result", result)
            .log(A.now(), A.minSec());
    }

    public static void clearForTests() {
        lastStepFrameByUnit.clear();
    }
}
