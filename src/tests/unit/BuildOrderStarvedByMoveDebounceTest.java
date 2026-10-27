package tests.unit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The build attempt must not be gated on the age of an action that issuing a
 * move resets.
 *
 * <p>
 * <b>Measured (OpenBW, {@code BUILDER_LOG=1}, 2026-10-11).</b> A Gateway
 * builder sat at
 * {@code distToSite=1.3} with {@code minDistanceToIssueBuildOrder=1.4}, so
 * {@code shouldMoveToConstruct} correctly answered "do not move" and
 * {@code travelWhenReady}
 * reached {@code IssueBuildOrder} - and then the build was never attempted,
 * because of:
 * </p>
 *
 * <pre>
 *   if (unit.lastActionMoreThanAgo(20)) {   // IssueBuildOrder
 *       ... unit.build(...) ...
 *   }
 * </pre>
 *
 * <p>
 * {@code AUnitOrders.move} sets the action and the receive time on every call:
 * {@code unit.setLastActionReceivedNow().setAction(unitAction)} - so a worker
 * whose last
 * command was {@code MOVE_BUILD} has {@code lastActionAgo(MOVE_BUILD)} reset
 * towards 0, and
 * {@code lastActionMoreThanAgo(20)} is false on almost every frame. The trace
 * shows exactly
 * that sawtooth: {@code lastActionAgo} climbing 2, 5, 8, 11, 14, 17, 20 and
 * then snapping back
 * to 2 - and the one build attempt that did happen occurred on the single frame
 * where it
 * reached 20.
 * </p>
 *
 * <p>
 * This is the third instance of one shape in this file's neighbourhood, and the
 * file already
 * documents the first two: a debounce on a value that the code path itself
 * keeps refreshing.
 * {@code asProtossMultiBuilderDoNotSwitchConstructions} was fixed for it; the
 * {@code
 * IssueBuildOrder} gate was not.
 * </p>
 *
 * <p>
 * <b>Why source-pinned.</b> The stub world does not reproduce the
 * frame-by-frame action
 * sawtooth (it has no real command pipeline to reset it), so a behavioural test
 * here would
 * assert a property of the harness. What the pin protects is the rule: the
 * build-attempt gate
 * must not read an action that a move refreshes.
 * </p>
 */
@Tag("production")
public class BuildOrderStarvedByMoveDebounceTest {

    private static final String ISSUE = "src/atlantis/production/constructions/builders/IssueBuildOrder.java";

    @Test
    public void theBuildAttemptIsNotGatedOnTheActionAMoveWouldReset() throws Exception {
        String source = new String(Files.readAllBytes(Paths.get(ISSUE)), StandardCharsets.UTF_8);

        boolean gatedOnSelfRefreshingAction = source.contains("if (unit.lastActionMoreThanAgo(20)) {");

        assertFalse(gatedOnSelfRefreshingAction, "the build attempt must not be gated on `unit.lastActionMoreThanAgo(20)`: `move()`"
            + " calls setLastActionReceivedNow().setAction(...) on every invocation, so for a"
            + " worker whose last command is MOVE_BUILD that age resets to 0 and the gate is"
            + " false on nearly every frame - measured as a soak at distToSite=1.3 with"
            + " lastActionAgo sawtoothing 2,5,8,11,14,17,20 and only one build attempt in the"
            + " whole run. Gate on something the move does not touch (e.g. the command age,"
            + " `lastCommandIssuedAgo`, which is only updated when a command is actually"
            + " ISSUED).");
    }
}
