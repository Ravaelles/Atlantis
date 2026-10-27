package tests.unit;

import atlantis.game.util.CompletedUnitsHistory;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The record of what the bot BUILT, which the end-of-game report needs and the
 * live board
 * cannot supply.
 *
 * <p>
 * The summary used to print only what stands at the end, so a game with 3
 * Pylons left could
 * not say whether a fourth was built and lost. The report is now
 * {@code Pylon=3/4}: remaining over built. These tests pin the two halves that
 * make that
 * number meaningful - that only COMPLETED units count, and that the ordering is
 * the game's
 * own progression rather than a hand-written list.
 * </p>
 */
@Tag("infra")
public class CompletedUnitsHistoryTest extends AbstractTestWithUnits {

    private static final AUnitType PYLON = AUnitType.Protoss_Pylon;
    private static final AUnitType GATEWAY = AUnitType.Protoss_Gateway;
    private static final AUnitType CORE = AUnitType.Protoss_Cybernetics_Core;

    @BeforeEach
    @AfterEach
    public void clear() {
        CompletedUnitsHistory.reset();
    }

    /**
     * Nothing built is reported as nothing, not as a wrong number.
     */
    @Test
    public void anEmptyHistoryCountsNothing() {
        assertEquals(0, CompletedUnitsHistory.completedCount(PYLON), "no Pylon was built");
        assertEquals(0, CompletedUnitsHistory.completedCount(GATEWAY), "no Gateway was built");
        assertTrue(
            CompletedUnitsHistory.typesByFirstCompletion().isEmpty(),
            "an empty history lists no types - the report says \"(nothing completed)\" for this");
    }

    /**
     * Each completion counts once, and repeated completions of the same type
     * accumulate.
     */
    @Test
    public void everyCompletedUnitIsCounted() {
        CompletedUnitsHistory.record(PYLON);
        CompletedUnitsHistory.record(PYLON);
        CompletedUnitsHistory.record(PYLON);

        assertEquals(3, CompletedUnitsHistory.completedCount(PYLON), "three Pylons built");
        assertEquals(0, CompletedUnitsHistory.completedCount(GATEWAY), "and no Gateway");
    }

    /**
     * <b>The ordering rule, and the reason it is not hardcoded.</b> Types come out
     * in the
     * order their FIRST unit finished, so the line reads like the build progression
     * - the
     * opening first, tech later. A hand-written list in the report class would need
     * editing
     * every time the bot's opening changed.
     */
    @Test
    public void typesAreOrderedByWhenTheirFirstUnitFinished() {
        CompletedUnitsHistory.record(PYLON);
        CompletedUnitsHistory.record(GATEWAY);
        CompletedUnitsHistory.record(PYLON);
        CompletedUnitsHistory.record(CORE);

        List<AUnitType> order = CompletedUnitsHistory.typesByFirstCompletion();

        // Arrays.asList, not List.of: the whole tree compiles with --release 8 (CONVENTIONS
        // §16) and List.of is a Java 9 API - it compiles on a modern javac and then breaks
        // the game build, which is a trap this repository has paid for before.
        assertEquals(
            Arrays.asList(PYLON, GATEWAY, CORE), order,
            "Pylon first, then Gateway, then Core - the order they were first built, not the "
                + "order they were last built and not a list written by hand");
    }

    /**
     * The first-completion frame is what the ordering is built on, and it must be
     * the FIRST
     * one - a later completion of the same type must not move it. Otherwise a type
     * could
     * drift up and down the report as the game went on.
     */
    @Test
    public void theFirstCompletionFrameIsNotOverwrittenByLaterOnes() {
        // The clock is moved between recordings on purpose. Without that, every frame is 0
        // and `put` and `putIfAbsent` answer the same thing - a first version of this test
        // passed against a deliberately broken implementation for exactly that reason.
        useFakeTime(100);
        CompletedUnitsHistory.record(PYLON);
        int firstFrame = CompletedUnitsHistory.firstCompletedFrame(PYLON);

        useFakeTime(200);
        CompletedUnitsHistory.record(PYLON);
        useFakeTime(300);
        CompletedUnitsHistory.record(PYLON);

        assertEquals(
            100, firstFrame, "precondition: the first recording captured frame 100");
        assertEquals(
            100, CompletedUnitsHistory.firstCompletedFrame(PYLON),
            "the first completion keeps its frame - it is the sort key, and a later "
                + "recording must not move it");
        assertEquals(
            3, CompletedUnitsHistory.completedCount(PYLON),
            "while the COUNT still accumulates - only the first frame is frozen");
    }

    /**
     * A type never built reports no frame, which sorts it last rather than first.
     */
    @Test
    public void aTypeNeverBuiltHasNoCompletionFrame() {
        assertEquals(
            Integer.MAX_VALUE, CompletedUnitsHistory.firstCompletedFrame(GATEWAY),
            "a type we never built must sort last, not first - a zero default would put it "
                + "at the top of the report");
    }

    /**
     * Ties are broken by the order the types were first RECORDED in - not alphabetically and
     * not by whatever order the runtime iterates a map in.
     *
     * <p>
     * Frame numbers alone are not enough in a game: a round of Pylons, a Gateway and a Zealot
     * routinely finish on the same frame, and an ordering that depended on collection
     * iteration order could differ between two runs of the same game - making the report
     * incomparable and any test flaky. Recording order is deterministic and is also the order
     * the events arrived.
     * </p>
     */
    @Test
    public void tiesFollowTheOrderTheTypesWereFirstRecorded() {
        CompletedUnitsHistory.record(GATEWAY);
        CompletedUnitsHistory.record(PYLON);

        assertEquals(
            Arrays.asList(GATEWAY, PYLON), CompletedUnitsHistory.typesByFirstCompletion(),
            "with equal frames the type recorded first comes first - Gateway here");

        CompletedUnitsHistory.reset();
        CompletedUnitsHistory.record(PYLON);
        CompletedUnitsHistory.record(GATEWAY);

        assertEquals(
            Arrays.asList(PYLON, GATEWAY), CompletedUnitsHistory.typesByFirstCompletion(),
            "reversing the recording order reverses the report, which is what proves the "
                + "tie-break is the sequence rather than an accident of iteration order");
    }

    /**
     * Recording nothing is a no-op rather than an error, because the hook may see
     * null.
     */
    @Test
    public void recordingNullIsIgnored() {
        CompletedUnitsHistory.record(null);

        assertTrue(
            CompletedUnitsHistory.typesByFirstCompletion().isEmpty(),
            "a null type must not create an entry");
    }
}
