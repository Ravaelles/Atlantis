package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.position.BuildingTileHelper;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the arithmetic every position condition stands on.
 *
 * <p>
 * This class is the module's measuring stick: {@code TooCloseToUnwalkable} and
 * several siblings ask it "where is the tile one step left of this building?"
 * and
 * then judge walkability there. A silent unit error here moves every one of
 * those
 * answers, which is what makes it worth a test of its own rather than being
 * covered incidentally through a world.
 * </p>
 *
 * <p>
 * <b>The trap it documents, measured 2026-10-10:</b> a StarCraft building's
 * dimensions are <b>not</b> multiples of a tile. A Gateway is 4x3 tiles but
 * {@code dimensionLeftPixels() = 48} and {@code dimensionRightPixels() = 48} -
 * one
 * and a half tiles - and {@code dimensionDownPixels() = 40} (1.25 tiles). The
 * pixel
 * accessors keep that precision; the {@code ...Tiles()} accessors divide by 32
 * with
 * <b>integer truncation</b>, so {@code dimensionRightTiles()} answers 1 for a
 * Gateway and loses 16 px. Any code that mixes the two units is wrong by a
 * fraction
 * of a tile, and the owner has already been bitten once by a hand-typed tile
 * size
 * in this module ({@code ProtossTooCloseToMapBoundaries}, 30 vs 32).
 * </p>
 *
 * <p>
 * No game is needed: the helper is pure arithmetic over an {@code AUnitType}
 * (whose dimensions come from the engine data, not a live unit) and an
 * {@code APosition}.
 * </p>
 */
@Tag("production")
public class BuildingTileHelperTest {

    private static final int PX_PER_TILE = 32;

    private static final AUnitType GATEWAY = AUnitType.Protoss_Gateway;

    private static APosition at(int tx, int ty) {
        return APosition.create(tx, ty);
    }

    /**
     * The engine-data fact this whole file is about: a Gateway is 4x3 tiles, yet
     * its
     * left/right extent is 48 px (1.5 tiles) and its down extent 40 px (1.25).
     * Pinned
     * so an engine swap that changes it fails loudly instead of shifting every
     * position decision by a fraction of a tile.
     */
    @Test
    public void gatewayDimensionsAreNotWholeTiles() {
        assertEquals(4, GATEWAY.getTilesWidth(), "a Gateway is 4 tiles wide");
        assertEquals(3, GATEWAY.getTilesHeights(), "a Gateway is 3 tiles tall");

        assertEquals(48, GATEWAY.dimensionLeftPixels(), "left extent is 1.5 tiles, not 1 or 2");
        assertEquals(48, GATEWAY.dimensionRightPixels(), "right extent is 1.5 tiles");
        assertEquals(32, GATEWAY.dimensionUpPixels(), "up extent is exactly 1 tile");
        assertEquals(40, GATEWAY.dimensionDownPixels(), "down extent is 1.25 tiles");
    }

    /**
     * The truncation trap, stated as a fact so nobody "fixes" the pixel arithmetic
     * to match the tiles arithmetic: they genuinely disagree, and the pixel one is
     * the accurate of the two.
     */
    @Test
    public void theTilesAccessorsTruncateAndLosePrecision() {
        assertEquals(1, GATEWAY.dimensionRightTiles(),
                "48/32 truncates to 1: the tiles accessor loses 16 px of a Gateway's extent");
        assertEquals(1, GATEWAY.dimensionDownTiles(),
                "40/32 truncates to 1: the down extent loses 8 px");

        assertTrue(
                GATEWAY.dimensionRightTiles() * PX_PER_TILE != GATEWAY.dimensionRightPixels(),
                "the two accessors disagree for a Gateway - code must not mix them");
    }

    /**
     * A tile one step left of the building's left edge, measured in pixels: the
     * position is moved by the left extent plus one tile, and it lands exactly one
     * tile to the left of that edge.
     */
    @Test
    public void oneTileLeftSitsOneTileBeyondTheLeftEdge() {
        APosition origin = at(50, 50);

        APosition left = BuildingTileHelper.tileLeftFrom(GATEWAY, origin);

        int expectedShift = -(GATEWAY.dimensionLeftPixels() + PX_PER_TILE);
        assertEquals(origin.x() + expectedShift, left.x(), "x is the left extent plus one tile");
        assertEquals(origin.y(), left.y(), "a leftward step does not move y");
    }

    /** The general form agrees with the specific one it duplicates. */
    @Test
    public void theGeneralAndSpecificLeftFormsAgree() {
        APosition origin = at(50, 50);

        assertEquals(
                BuildingTileHelper.tileLeftFrom(GATEWAY, origin).x(),
                BuildingTileHelper.tilesLeftFrom(GATEWAY, origin, 1).x(),
                "tileLeftFrom must equal tilesLeftFrom(..., 1) - they are the same operation");
        assertEquals(
                BuildingTileHelper.tiles2LeftFrom(GATEWAY, origin).x(),
                BuildingTileHelper.tilesLeftFrom(GATEWAY, origin, 2).x(),
                "the 2-tile form must equal the general form with 2");
        assertEquals(
                BuildingTileHelper.tiles4RightFrom(GATEWAY, origin).x(),
                BuildingTileHelper.tilesRightFrom(GATEWAY, origin, 4).x(),
                "the 4-tile form must equal the general form with 4");
    }

    /**
     * Each extra tile step moves exactly one tile further: the separation is
     * linear,
     * so a bug that adds a constant once (or forgets to scale) shows up here.
     */
    @Test
    public void eachStepMovesExactlyOneTileFurther() {
        APosition origin = at(50, 50);

        int one = BuildingTileHelper.tilesRightFrom(GATEWAY, origin, 1).x();
        int two = BuildingTileHelper.tilesRightFrom(GATEWAY, origin, 2).x();
        int three = BuildingTileHelper.tilesRightFrom(GATEWAY, origin, 3).x();

        assertEquals(PX_PER_TILE, two - one, "the second step is one tile beyond the first");
        assertEquals(PX_PER_TILE, three - two, "the third step is one tile beyond the second");
    }

    /** Down and up scale the same way, on the y axis, and never touch x. */
    @Test
    public void verticalStepsScaleOnYAndLeaveXAlone() {
        APosition origin = at(50, 50);

        APosition down1 = BuildingTileHelper.tilesDownFrom(GATEWAY, origin, 1);
        APosition down2 = BuildingTileHelper.tilesDownFrom(GATEWAY, origin, 2);
        APosition up1 = BuildingTileHelper.tilesUpFrom(GATEWAY, origin, 1);
        APosition up2 = BuildingTileHelper.tilesUpFrom(GATEWAY, origin, 2);

        assertEquals(origin.x(), down1.x(), "a vertical step does not move x");
        assertEquals(origin.x(), up1.x(), "a vertical step does not move x");

        assertEquals(PX_PER_TILE, down2.y() - down1.y(), "each downward step is one tile");
        assertEquals(PX_PER_TILE, up1.y() - up2.y(), "each upward step is one tile (y grows downward)");
    }

    /**
     * The direction names match the signs: left/up are negative, right/down
     * positive.
     */
    @Test
    public void directionsHaveTheExpectedSigns() {
        APosition origin = at(50, 50);

        assertTrue(BuildingTileHelper.tileLeftFrom(GATEWAY, origin).x() < origin.x(), "left is -x");
        assertTrue(BuildingTileHelper.tileRightFrom(GATEWAY, origin).x() > origin.x(), "right is +x");
        assertTrue(BuildingTileHelper.tileUpFrom(GATEWAY, origin).y() < origin.y(), "up is -y");
        assertTrue(BuildingTileHelper.tileDownFrom(GATEWAY, origin).y() > origin.y(), "down is +y");
    }
}
