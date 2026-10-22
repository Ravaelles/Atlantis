package atlantis.production.v2.execution;

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
 * <p>Pure over the dispatch results: the engine calls it with the frame,
 * tests call it with numbers.</p>
 */
public final class V2DispatchTwin {

    private V2DispatchTwin() {
    }

    public static void emit(int frame, List<DispatchResult> results) {
        if (results == null) return;

        for (DispatchResult result : results) {
            if (result == null || result.item() == null || result.item().item() == null) continue;
            EventLog.key(LogKey.V2_DISPATCH)
                .param("item", result.item().item().id())
                .param("start", result.item().startFrame())
                .param("issued", result.issued())
                .param("detail", result.detail())
                .log(frame, frameToClock(frame));
        }
    }

    private static String frameToClock(int frame) {
        int seconds = frame / 24;
        return (seconds / 60) + ":" + String.format("%02d", seconds % 60);
    }
}
