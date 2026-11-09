package atlantis.production.execution;

import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

import java.util.List;

/**
 * The V2 half of the decision audit (NEXT Phase 5 thinness watch): every
 * plan item the dispatcher acted on, with the director's reason - issued
 * or refused, and why. Legacy production answers the same question in
 * DECISIONS.log; V2 answers here, in both DRY_RUN and LIVE, so a thin
 * game reads as few rows, not as silence.
 *
 * <p>Nearly pure over the dispatch results: identical re-offers heartbeat
 * instead of rowing every frame (see below), so tests reset the clock
 * with {@link #clearForTests} next to the log.</p>
 */
public final class V2DispatchTwin {

    private V2DispatchTwin() {
    }

    public static void emit(int frame, List<DispatchResult> results) {
        if (results == null) return;

        for (DispatchResult result : results) {
            if (result == null || result.item() == null || result.item().item() == null) continue;
            if (isHeartbeatDuplicate(frame, result)) continue;

            EventLog.key(LogKey.V2_DISPATCH)
                .param("item", result.item().item().id())
                .param("start", result.item().startFrame())
                .param("issued", result.issued())
                .param("detail", result.detail())
                .log(frame, frameToClock(frame));
        }
    }

    /**
     * Re-offer heartbeat (owner 2026-11-09): the dispatcher retries due
     * items every frame, so a stuck mint (or a busy facility) used to
     * row identically hundreds of times - the Citadel drought rowed
     * "no placement" 250x. Identical rows (item, verdict, reason)
     * twin once, then heartbeat every {@link #HEARTBEAT_FRAMES};
     * anything changed twins immediately. The plan start is deliberately
     * NOT part of the key: the scheduler rebuilds the plan every frame,
     * so a stuck-but-affordable demand slides its start forward daily
     * and a start-keyed heartbeat would never catch it. The start still
     * rides the row as a param. Test seam below keeps the
     * pure-function pins hermetic.
     */
    public static final int HEARTBEAT_FRAMES = 240;

    private static final java.util.Map<String, Integer> lastTwinAt = new java.util.HashMap<>();

    public static void clearForTests() {
        lastTwinAt.clear();
    }

    private static boolean isHeartbeatDuplicate(int frame, DispatchResult result) {
        try {
            String key = result.item().item().id() + "|" + result.issued() + "|"
                + result.detail();
            Integer last = lastTwinAt.get(key);
            if (last != null && frame - last < HEARTBEAT_FRAMES) return true;

            if (lastTwinAt.size() > 400) lastTwinAt.clear();
            lastTwinAt.put(key, frame);

            return false;
        } catch (Exception | Error ignored) {
            return false;
        }
    }

    private static String frameToClock(int frame) {
        int seconds = frame / 24;
        return (seconds / 60) + ":" + String.format("%02d", seconds % 60);
    }
}
