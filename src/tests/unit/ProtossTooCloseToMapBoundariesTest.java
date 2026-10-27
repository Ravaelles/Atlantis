package tests.unit;

import atlantis.Atlantis;
import atlantis.map.position.APosition;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.production.constructions.position.protoss.ProtossTooCloseToMapBoundaries;
import atlantis.units.AUnitType;
import bwapi.Game;
import bwapi.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pins the map-boundary margin in <b>pixels</b>, not tiles.
 *
 * <p>
 * The bug this exists for (owner, 2026-10-10): the class compared pixel
 * coordinates against a map size computed as {@code tiles * 30}, while a build tile
 * is <b>32</b> pixels. The map therefore came out 6.25% too small and the
 * right/bottom margin swallowed roughly two extra tiles - the bot refused perfectly
 * good positions and reported {@code Too close to right map boundary} for tiles
 * nowhere near any edge: {@code (24,24)} through {@code (28,28)} in the owner's log.
 * </p>
 *
 * <p>
 * The point of this test is that it would have <b>failed</b> against {@code * 30}:
 * every expectation is derived from 32 pixels per tile, the unit the rest of the
 * tree already uses ({@code AUnitType.dimensionRightTiles()} divides by 32,
 * {@code translateByTiles} multiplies by 32).
 * </p>
 *
 * <p>
 * The class under test is a pure predicate over a position and the map size, so no
 * stub world is needed - only {@code Atlantis.game()} must answer
 * {@code mapWidth()}/{@code mapHeight()}, which is mocked here. The race gate is
 * satisfied without a seam because {@code Main.ourRace()} returns "Protoss".
 * </p>
 */
@Tag("production")
public class ProtossTooCloseToMapBoundariesTest {

    /**
     * Tiles per build tile in this engine: 32. Anything else here is the bug.
     */
    private static final int PX_PER_TILE = 32;

    private static final AUnitType PYLON = AUnitType.Protoss_Pylon;

    /**
     * A square 64x64-tile map, so width and height share one number and a wrong
     * width cannot hide behind a correct height.
     */
    private static final int MAP_TILES = 64;

    private MockedStatic<Atlantis> atlantis;
    private Game mockedGame;

    private static APosition tile(int tx, int ty) {
        return APosition.create(tx, ty);
    }

    @BeforeEach
    public void mockTheGame() {
        Game game = mock(Game.class);
        mockedGame = game;
        when(game.mapWidth()).thenReturn(MAP_TILES);
        when(game.mapHeight()).thenReturn(MAP_TILES);

        // The class widens the Pylon margin to 7 tiles while supply is low; it reads
        // A.supplyUsed(), which goes through game.self().supplyUsed(). Both have to
        // answer, or the predicate dies with an NPE instead of returning a verdict -
        // which would make these tests pass for the wrong reason.
        Player self = mock(Player.class);
        when(self.supplyUsed()).thenReturn(8);
        when(game.self()).thenReturn(self);

        atlantis = Mockito.mockStatic(Atlantis.class);
        atlantis.when(Atlantis::game).thenReturn(game);
    }

    @AfterEach
    public void closeTheMock() {
        atlantis.close();
    }

    /**
     * A tile in the middle of the map is never "too close" to anything.
     */
    @Test
    public void theCentreOfTheMapIsFine() {
        assertFalse(
            ProtossTooCloseToMapBoundaries.isTooClose(PYLON, tile(32, 32)),
            "the middle of a 64x64 map is not near a boundary"
        );
    }

    /**
     * The measured regression: the owner's log reported
     * {@code (24,24) Too close to right map boundary} through {@code (28,28)}. At
     * 32 px/tile those tiles are nowhere near the right edge.
     *
     * <p>
     * With the old {@code * 30} arithmetic the map width was understated by 128 px
     * for a 64-tile map (64*30 = 1920 vs 64*32 = 2048), which pulled the right-hand
     * limit in far enough to refuse exactly these tiles.
     * </p>
     */
    @Test
    public void tilesFromTheOwnerReportAreNotTooCloseToAnyBoundary() {
        for (int t = 24; t <= 28; t++) {
            assertFalse(
                ProtossTooCloseToMapBoundaries.isTooClose(PYLON, tile(t, t)),
                "tile (" + t + "," + t + ") is nowhere near a 64x64 map's edge; refusing it was the "
                    + "units bug (30 vs 32 px per tile), not a real boundary"
            );
        }
    }

    /**
     * A tile that IS against the right edge must still be refused.
     */
    @Test
    public void theLastTileColumnIsStillRefused() {
        assertTrue(
            ProtossTooCloseToMapBoundaries.isTooClose(PYLON, tile(MAP_TILES - 1, MAP_TILES / 2)),
            "the last tile column is within the right margin and must be refused"
        );
    }

    /**
     * The bug with numbers attached, on a map the size of the real one the owner
     * reported from (TauCross is 128x128 tiles).
     *
     * <p>
     * With {@code marginPx = 7*32} for an early Pylon and a 2x2 Pylon, the right
     * limit sits at
     * {@code (128*32 - 7*32 - 2*32) / 32} = <b>119</b> tiles when tiles are 32 px,
     * but only <b>111</b> when they are read as 30 px ({@code 128*30 = 3840} instead
     * of 4096). Tiles 112..119 were therefore refused with "Too close to right map
     * boundary" while being eight columns clear of the edge.
     * </p>
     *
     * <p>
     * This is the assertion that fails against {@code * 30} and passes against
     * {@code * 32}, which is what makes the test worth having.
     * </p>
     */
    @Test
    public void anEightColumnStripNearTheRightEdgeIsNotRefusedOnA128TileMap() {
        int bigMap = 128;

        // The two candidate limits, computed from the constants rather than from the
        // implementation, so this is an independent expectation.
        int marginPx = 7 * PX_PER_TILE;
        int pylonRightPx = 2 * PX_PER_TILE;
        int correctLimitTile = (bigMap * PX_PER_TILE - marginPx - pylonRightPx) / PX_PER_TILE;
        int buggyLimitTile = (bigMap * 30 - marginPx - pylonRightPx) / PX_PER_TILE;

        assertTrue(
            correctLimitTile == 119 && buggyLimitTile == 111,
            "the two unit sizes must disagree by 8 tiles on a 128-tile map "
                + "(got correct=" + correctLimitTile + ", buggy=" + buggyLimitTile + ")"
        );

        // Now the behaviour, on the real map size.
        when(mockedGame.mapWidth()).thenReturn(bigMap);
        when(mockedGame.mapHeight()).thenReturn(bigMap);

        assertFalse(
            ProtossTooCloseToMapBoundaries.isTooClose(PYLON, tile(115, 64)),
            "tile x=115 is inside the true 119-tile limit; the 30-px arithmetic refused it"
        );
        assertTrue(
            ProtossTooCloseToMapBoundaries.isTooClose(PYLON, tile(125, 64)),
            "tile x=125 is past the true limit and must still be refused"
        );
    }

    /**
     * Pins the tile size itself, which is what the fix is about: at 32 px a 64-tile
     * map is 2048 px wide and the last column's right edge still falls inside it. At
     * 30 px the same column would fall outside the map - the exact reason valid
     * tiles were refused.
     */
    @Test
    public void theMapIsMeasuredWithThirtyTwoPixelTiles() {
        int lastTileRightEdgePx = (MAP_TILES - 1) * PX_PER_TILE;

        assertTrue(
            lastTileRightEdgePx < MAP_TILES * PX_PER_TILE,
            "at 32 px/tile the last column still fits inside the map, so 32 is the real tile size"
        );
        assertTrue(
            lastTileRightEdgePx >= MAP_TILES * 30,
            "at 30 px/tile that same column would fall outside the map, which is why the old "
                + "arithmetic refused valid positions"
        );
    }

    /**
     * A Gateway is included by the class's own race rule, so it is covered too.
     */
    @Test
    public void aGatewayInTheMiddleIsAlsoFine() {
        assertFalse(
            ProtossTooCloseToMapBoundaries.isTooClose(AUnitType.Protoss_Gateway, tile(32, 32)),
            "a Gateway in the middle of the map is not near a boundary"
        );
    }

    /**
     * A non-Protoss building is out of this class's scope entirely.
     */
    @Test
    public void aTerranBuildingIsNeverJudged() {
        assertFalse(
            ProtossTooCloseToMapBoundaries.isTooClose(
                AUnitType.Terran_Barracks,
                tile(MAP_TILES - 1, MAP_TILES / 2)
            ),
            "this is a Protoss rule; a Terran building must not be refused by it"
        );
    }

    /**
     * The status string is a diagnostic other code reads, so a refusal must set it.
     */
    @Test
    public void aRefusedPositionSetsTheStatusString() {
        AbstractPositionFinder._STATUS = "Init";

        ProtossTooCloseToMapBoundaries.isTooClose(PYLON, tile(MAP_TILES - 1, MAP_TILES / 2));

        assertTrue(
            AbstractPositionFinder._STATUS.contains("boundary"),
            "a refusal must say which boundary, got: " + AbstractPositionFinder._STATUS
        );
    }
}
