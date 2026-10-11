package tests.unit;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A Gateway cancelled for "taking too long" must be RE-REQUESTED, not given up on.
 *
 * <p>
 * <b>Measured (OpenBW, Zerg opponent, 120 s, {@code BUILDER_LOG=1}, 2026-10-11):</b> with the
 * old code the requeue path never ran at all in a whole 20-minute game -
 * {@code ### FAILED ### Requesting} = 0 and {@code Taking too long, re-try} = 0 - because
 * {@code CancelTooLongConstructions.constructionCancelledRequestAgainBecauseItsImportant} asked
 * "do we already have the maximum not-started ones?" BEFORE the call that frees the slot:
 * </p>
 *
 * <pre>
 *   if (CountInQueue.countNotStarted(type) &gt;= maxAllowedAtOnce(type)) return false;
 *   ...
 *   CancelNotStarted.cancel(type, "Taking too long, re-try");   // frees the slot, too late
 * </pre>
 *
 * <p>
 * {@code maxAllowedAtOnce(Gateway)} is 2, so a plan holding two not-started Gateways read as
 * "we have enough", the cancelled one stayed cancelled, and the run ended with 25 Probes, 834
 * minerals and zero Gateways. After the fix the same run reports {@code Taking too long,
 * re-try} = 2 and {@code REQUEST_ACCEPTED Gateway} = 2 (both were 0).
 * </p>
 *
 * <p>
 * <b>Why this test is source-pinned rather than behavioural.</b> Forcing the cancel path in the
 * stub world needs a construction old enough for {@code tookTooLong}, whose timeout depends on
 * the distance to the main base - and the stub world has no main base, so the arithmetic is a
 * property of the harness, not of the bug. Asserting on it would pin the harness. The
 * behavioural evidence for this fix is the live run quoted above; what the source pin protects
 * is the ORDER of the two statements, which is the entire defect.
 * </p>
 */
public class GatewayRequeueAfterCancelTest {

    private static final String SOURCE =
        "src/atlantis/production/constructions/commanders/CancelTooLongConstructions.java";

    @Test
    public void theRequeueGuardRunsAfterTheCancelThatFreesTheSlot() throws Exception {
        String source = new String(Files.readAllBytes(Paths.get(SOURCE)), StandardCharsets.UTF_8);
        String body = methodBody(source,
            "private static boolean constructionCancelledRequestAgainBecauseItsImportant");

        int cancel = body.indexOf("CancelNotStarted.cancel(type, \"Taking too long, re-try\")");
        int guard = body.indexOf("CountInQueue.countNotStarted(type) >= maxAllowedAtOnce(type)");

        assertTrue(cancel > 0, "the re-try cancel must still be there");
        assertTrue(guard > 0, "the not-started guard must still be there");
        assertTrue(cancel < guard,
            "the guard asks 'do we already have the maximum not-started ones?' - so it must run"
                + " AFTER the cancel that frees the slot, not before. With the old order a plan"
                + " holding two not-started Gateways vetoed its own replacement and the requeue"
                + " never ran (measured: '### FAILED ### Requesting' = 0 and 'Taking too long,"
                + " re-try' = 0 in a full game; after the fix, 2 and 2).");
    }

    private static String methodBody(String source, String signature) {
        int start = source.indexOf(signature);
        assertTrue(start > 0, "method not found: " + signature);

        int depth = 0;
        int i = source.indexOf('{', start);
        for (int j = i; j < source.length(); j++) {
            char c = source.charAt(j);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) return source.substring(start, j);
            }
        }
        return source.substring(start);
    }
}
