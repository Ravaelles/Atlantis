package atlantis.production.scheduling;
import atlantis.production.producibles.Producible;

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
 * the same reason rows once, not every frame - plus a heartbeat for
 * persisting storms, so a minute-long drought reads with a duration
 * (measured live: BO Gateway refused "no-placement" from 2:04, one row,
 * then silence until the 3:19 mint). The twin answers "which door is
 * closed" for the next live game; it never changes what gets planned.</p>
 */
public final class ScheduleRefusal {

    /**
     * Same-reason storms re-twin on this cadence (25 game-seconds).
     */
    public static final int HEARTBEAT_FRAMES = 600;

    private static final Map<String, String> lastRefusalByItem = new HashMap<>();
    private static final Map<String, Integer> lastFrameByItem = new HashMap<>();
    private static final Map<String, Integer> firstFrameByItem = new HashMap<>();

    private ScheduleRefusal() {
    }

    public static void note(Producible item, String reason, int frame) {
        if (item == null || reason == null) return;

        String id = item.id();
        boolean sameStorm = reason.equals(lastRefusalByItem.get(id));
        Integer lastFrame = lastFrameByItem.get(id);
        if (sameStorm && lastFrame != null && frame - lastFrame < HEARTBEAT_FRAMES) return;

        Integer firstFrame = firstFrameByItem.get(id);
        if (!sameStorm || firstFrame == null) firstFrame = frame;
        lastRefusalByItem.put(id, reason);
        lastFrameByItem.put(id, frame);
        firstFrameByItem.put(id, firstFrame);

        EventLog.key(LogKey.SCHEDULE_REFUSED)
            .param("item", id)
            .param("reason", reason)
            .param("elapsed", frame - firstFrame)
            .log(A.now(), A.minSec());
    }

    /**
     * Test seam: static refusal memory never crosses tests (or games -
     * see {@code OnGameStarted}, same as the attack decision's latch).
     */
    public static void clearForTests() {
        lastRefusalByItem.clear();
        lastFrameByItem.clear();
        firstFrameByItem.clear();
    }
}
