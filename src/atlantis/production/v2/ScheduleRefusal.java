package atlantis.production.v2;

import atlantis.game.A;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

import java.util.HashMap;
import java.util.Map;

/**
 * Names WHY a wanted building or unit never makes the plan (NEXT #202
 * family): the scheduler drops goals silently - no placement tile, no
 * affordable frame, no free producer - so a demand that fires every
 * frame for 18k frames without a single dispatch (measured live 130749:
 * Cybernetics Core goal from f6k, first plan at f24k) reads as a
 * production stall instead of a siting or bank problem.
 *
 * <p>Transition-gated like the attack decision: the same item failing for
 * the same reason rows once, not every frame. The twin answers "which
 * door is closed" for the next live game; it never changes what gets
 * planned.</p>
 */
public final class ScheduleRefusal {

    private static final Map<String, String> lastRefusalByItem = new HashMap<>();

    private ScheduleRefusal() {
    }

    public static void note(Producible item, String reason) {
        if (item == null || reason == null) return;

        String id = item.id();
        if (reason.equals(lastRefusalByItem.get(id))) return;
        lastRefusalByItem.put(id, reason);

        EventLog.key(LogKey.SCHEDULE_REFUSED)
            .param("item", id)
            .param("reason", reason)
            .log(A.now(), A.minSec());
    }

    /**
     * Test seam: static refusal memory never crosses tests (or games -
     * see {@code OnGameStarted}, same as the attack decision's latch).
     */
    public static void clearForTests() {
        lastRefusalByItem.clear();
    }
}
