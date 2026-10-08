package tests.unit;

import atlantis.production.v2.PylonPlacementScore;
import atlantis.production.v2.PylonPlacementScore.Tile;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Pins the Pylon scoring rule of 01_PRODUCTION.md's `BuildPositionResolver`: a
 * Pylon goes where it unlocks the most buildable room, not where a finder
 * happens to land first.
 *
 * <p>
 * Pure geometry, so no game and no stubs: the caller reads the map and hands in
 * rectangles, exactly like {@code ResourceTimeline} takes numbers instead of
 * BWAPI. That split is what makes the rule provable here.
 * </p>
 */
public class PylonPlacementScoreTest {

        @Test
        public void aTileWithNothingAroundItUnlocksNothing() {
                List<Tile> buildable = Arrays.asList(new Tile(100, 100));

                assertEquals(0, PylonPlacementScore.score(0, 0, buildable, Collections.<Tile>emptyList()),
                                "a Pylon far from every buildable tile powers nothing");
        }

        @Test
        public void everyBuildableTileInsideTheRadiusCounts() {
                // Three buildable tiles within 7 tiles of (10,10), one outside.
                List<Tile> buildable = Arrays.asList(
                                new Tile(10, 12),
                                new Tile(14, 10),
                                new Tile(5, 5),
                                new Tile(30, 30));

                assertEquals(3, PylonPlacementScore.score(10, 10, buildable, Collections.<Tile>emptyList()),
                                "the three tiles inside the power radius are unlocked, the far one is not");
        }

        @Test
        public void tilesThatAlreadyHavePowerAreWorthNothing() {
                List<Tile> buildable = Arrays.asList(new Tile(10, 12), new Tile(14, 10));
                List<Tile> alreadyPowered = Arrays.asList(new Tile(10, 12));

                assertEquals(1, PylonPlacementScore.score(10, 10, buildable, alreadyPowered),
                                "a second Pylon covering the same ground is wasted minerals - only the"
                                                + " unpowered tile counts");
        }

        @Test
        public void poweringOnlyBlockedTilesBuysNothing() {
                // Powering a tile nobody can build on is not a reason to place a Pylon.
                List<Tile> buildable = Collections.emptyList();
                List<Tile> blocked = Arrays.asList(new Tile(10, 12), new Tile(14, 10));

                assertEquals(0, PylonPlacementScore.score(10, 10, buildable, Collections.<Tile>emptyList()),
                                "blocked tiles are not buildable, so they contribute nothing");
                assertEquals(2, blocked.size(), "the scenario must have had blocked tiles to ignore");
        }

        @Test
        public void theBestCandidateIsTheOneUnlockingMost() {
                List<Tile> candidates = Arrays.asList(
                                new Tile(0, 0),
                                new Tile(20, 20),
                                new Tile(50, 50));

                List<Tile> buildable = new ArrayList<>();
                // Two tiles near (0,0), five near (20,20), none near (50,50).
                buildable.add(new Tile(1, 1));
                buildable.add(new Tile(2, 2));
                for (int i = 0; i < 5; i++)
                        buildable.add(new Tile(20 + i, 21));

                Tile best = PylonPlacementScore.best(candidates, buildable, Collections.<Tile>emptyList());

                assertEquals(20, best.x, "the tile unlocking five tiles must win over the one unlocking two");
                assertEquals(20, best.y);
        }

        @Test
        public void tiesKeepTheFirstCandidateSoTheFindersOrderSurvives() {
                List<Tile> candidates = Arrays.asList(new Tile(5, 5), new Tile(40, 40));
                List<Tile> buildable = Arrays.asList(new Tile(5, 6), new Tile(40, 41));

                Tile best = PylonPlacementScore.best(candidates, buildable, Collections.<Tile>emptyList());

                assertSame(candidates.get(0), best,
                                "on a tie the caller's order decides - that is how the position finder's"
                                                + " own preference survives this rule instead of being overwritten");
        }

        @Test
        public void noCandidatesMeansNoAnswer() {
                assertNull(PylonPlacementScore.best(
                                Collections.<Tile>emptyList(),
                                Collections.singletonList(new Tile(1, 1)),
                                Collections.<Tile>emptyList()),
                                "nothing to choose from must be null, not a fabricated tile");
        }

        @Test
        public void theRadiusIsBoundedBothWays() {
                // The rule must not depend on direction: a tile 7 to the left counts
                // exactly like one 7 to the right, and 8 does not count at all.
                List<Tile> buildable = Arrays.asList(
                                new Tile(3, 10),
                                new Tile(17, 10),
                                new Tile(2, 10),
                                new Tile(18, 10));

                assertEquals(2, PylonPlacementScore.score(10, 10, buildable, Collections.<Tile>emptyList()),
                                "7 tiles on either side count, 8 do not");
        }
}
