package tests.unit;

import org.junit.jupiter.api.Tag;
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
@Tag("production")
public class GatewayRequeueAfterCancelTest {

    private static final String TRAVEL =
        "src/atlantis/production/constructions/builders/TravelToConstruct.java";

    @Test
    public void theTravelThresholdComparesAPlainDistanceNotAJbwebOrCentreDistance()
        throws Exception {
        String source = new String(Files.readAllBytes(Paths.get(TRAVEL)), StandardCharsets.UTF_8);

        assertTrue(source.contains("double distanceToConstruction = distanceForBuildOrderIssue(unit, construction, type);"),
            "the travel threshold must compare the distance the worker walks -"
                + " since the owner call the metric is the footprint center for"
                + " every type (it used to be the tile, with gas as the only"
                + " center exception), and the threshold below is footprint-sized"
                + " so a 4-wide walk target always counts on arrival. The two"
                + " historical mistakes this guards against: groundDist through"
                + " JBWEB (unusable on OpenBW), and a fixed 1.4 line against a"
                + " center ~2.5 tiles from its origin."
                + " Behavioural pins: GasBuildIssueDistanceTest.");
    }

    private static final String SOURCE =
        "src/atlantis/production/constructions/commanders/CancelTooLongConstructions.java";

    @Test
    public void theRequeueGuardRunsAfterTheCancelThatFreesTheSlot() throws Exception {
        String source = new String(Files.readAllBytes(Paths.get(SOURCE)), StandardCharsets.UTF_8);
        String body = methodBody(source,
            "private static boolean constructionCancelledRequestAgainBecauseItsImportant");

        int cancel = body.indexOf("CancelNotStarted.cancel(type, \"Taking too long, re-try\")");
        int guard = body.indexOf("shouldVetoRequeue(type)");

        assertTrue(cancel > 0, "the re-try cancel must still be there");
        assertTrue(guard > 0, "the not-started guard must still be there");
        assertTrue(cancel < guard,
            "the guard asks 'do we already have the maximum not-started ones?' - so it must run"
                + " AFTER the cancel that frees the slot, not before. With the old order a plan"
                + " holding two not-started Gateways vetoed its own replacement and the requeue"
                + " never ran (measured in a full game).");

        String veto = methodBody(source, "private static boolean shouldVetoRequeue");
        assertTrue(veto.contains("countTriggerableNotStarted(type)"),
            "the guard counts only triggerable rows - untriggerable ones vetoed"
                + " the rescue rebuild and deadlocked 9/9 (game 201239)");
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
