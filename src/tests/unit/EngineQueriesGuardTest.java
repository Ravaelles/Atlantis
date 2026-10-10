package tests.unit;

import atlantis.config.env.Env;
import atlantis.map.EngineQueries;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The guard that makes a silent wrong answer impossible on OpenBW.
 *
 * <p>
 * OpenBW returns a constant {@code false}/{@code null} for {@code hasPath},
 * {@code getRegionAt} and {@code canBuildHere}
 * ({@code _AI/CHALLENGES/OpenBW-API.md}).
 * Twelve production call sites used to read those constants and take the "no
 * path"
 * branch, which is why expansion, attack targeting and worker retreat were
 * quietly
 * disabled. {@link EngineQueries} is now the only sanctioned door to those
 * engine
 * queries, and on OpenBW it <b>throws</b> - so a caller that forgot to use our
 * own
 * model finds out at that line instead of from a game that never expands.
 * </p>
 *
 * <p>
 * These tests pin the fail-fast contract itself, which is the part that would
 * regress silently: if someone removes the guard, the tests go red rather than
 * the
 * bot going quiet.
 * </p>
 */
public class EngineQueriesGuardTest {

    @AfterEach
    public void leaveOpenBwFlagOff() {
        // The flag is read through Env; other suites assume the default (off), so a
        // test must not leak it.
        Env.setOpenBwForTest(false);
    }

    /**
     * The guard fires on OpenBW: an unsafe path query throws instead of answering.
     */
    @Test
    public void hasPathThrowsOnOpenBw() {
        Env.setOpenBwForTest(true);

        EngineQueries.UnreliableOnOpenBw thrown = assertThrows(
                EngineQueries.UnreliableOnOpenBw.class,
                () -> EngineQueries.hasPath(null, null, null),
                "asking the engine for a path on OpenBW must fail fast, not return a constant");

        assertTrue(thrown.getMessage().contains("Game.hasPath"),
                "the message must name the query that failed: " + thrown.getMessage());
        assertTrue(thrown.getMessage().contains("MapTiles.hasPathBetween")
                || thrown.getMessage().contains("Pathfinding.reachable"),
                "the message must name what to use instead: " + thrown.getMessage());
    }

    /** Same for the unit-shaped overload. */
    @Test
    public void unitHasPathThrowsOnOpenBw() {
        Env.setOpenBwForTest(true);

        assertThrows(EngineQueries.UnreliableOnOpenBw.class,
                () -> EngineQueries.hasPath((bwapi.Unit) null, null),
                "Unit.hasPath is the same broken query and must also fail fast");
    }

    /** The region lookup is broken there too, so it is guarded as well. */
    @Test
    public void getRegionAtThrowsOnOpenBw() {
        Env.setOpenBwForTest(true);

        EngineQueries.UnreliableOnOpenBw thrown = assertThrows(
                EngineQueries.UnreliableOnOpenBw.class,
                () -> EngineQueries.getRegionAt(null, null),
                "the engine region table is empty on OpenBW, so this must fail fast");

        assertTrue(thrown.getMessage().contains("getArea") || thrown.getMessage().contains("BWEM"),
                "the message must point at our BWEM model: " + thrown.getMessage());
    }

    /**
     * And the build query, which is refused because it ends in the broken path
     * check.
     */
    @Test
    public void canBuildHereThrowsOnOpenBw() {
        Env.setOpenBwForTest(true);

        assertThrows(EngineQueries.UnreliableOnOpenBw.class,
                () -> EngineQueries.canBuildHere(null, null, null, null),
                "canBuildHere is false for every tile on OpenBW and must fail fast");
    }

    /**
     * Off OpenBW the guard must be invisible: the engine's answer is correct there
     * and is the cheapest one available, so no exception and no interception.
     */
    @Test
    public void noGuardOffOpenBw() {
        Env.setOpenBwForTest(false);

        // A null game would throw a NullPointerException if the guard fired; it must
        // instead reach the engine call. Either way it must NOT be our exception.
        try {
            EngineQueries.hasPath(null, null, null);
        } catch (EngineQueries.UnreliableOnOpenBw e) {
            throw new AssertionError("the guard must not fire off OpenBW", e);
        } catch (NullPointerException expected) {
            // Reached the real engine call, which is the point.
        }
    }
}
