package atlantis.combat.twins;

import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

import java.util.HashMap;
import java.util.Map;

/**
 * Names formation-order gate decisions (NEXT #210): which unit, which
 * destination, issued or absorbed. Per-unit throttled - transitions
 * always row, steady states heartbeat every 150 frames - so a defending
 * line's calm reads as absorbs, churn as issues.
 */
public final class FormationOrderTwin {

    private static final int HEARTBEAT_FRAMES = 150;
    private static final Map<Integer, String> lastRow = new HashMap<>();
    private static final Map<Integer, Integer> lastFrame = new HashMap<>();

    private FormationOrderTwin() {
    }

    public static void issued(AUnit unit, HasPosition dest) {
        row(unit, dest, "issued");
    }

    public static void absorbed(AUnit unit, HasPosition dest) {
        row(unit, dest, "absorbed");
    }

    private static void row(AUnit unit, HasPosition dest, String result) {
        try {
            if (unit == null || dest == null || dest.position() == null) return;

            int now = A.now();
            String key = unit.id() + "@" + dest.tx() + "," + dest.ty() + ":" + result;
            String prev = lastRow.get(unit.id());
            Integer at = lastFrame.get(unit.id());
            if (key.equals(prev) && at != null && now - at < HEARTBEAT_FRAMES) return;

            lastRow.put(unit.id(), key);
            lastFrame.put(unit.id(), now);
            EventLog.key(LogKey.FORMATION_ORDER)
                .param("unit", unit.id())
                .param("result", result)
                .param("tx", dest.tx())
                .param("ty", dest.ty())
                .log(now, A.minSec());
        } catch (Exception ignored) {
        }
    }

    public static void clearForTests() {
        lastRow.clear();
        lastFrame.clear();
    }
}
