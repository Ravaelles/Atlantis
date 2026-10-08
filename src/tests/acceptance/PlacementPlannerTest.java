package tests.acceptance;

import atlantis.placement.core.TileAvailabilityGrid;
import atlantis.placement.engine.CataloguePlacementPlanner;
import atlantis.production.v2.PlacementReservation;
import atlantis.production.v2.TargetPlacement;
import atlantis.production.v2.UnitProducible;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Acceptance tests for the {@code PlacementPlanner} seam
 * (`_AI/redesign/03_PLACEMENT.md` §4.2, S1-S4), exercised through the planner
 * the
 * production scheduler actually calls.
 *
 * <p>
 * A synthetic flat map and no strategy/no gating, so every fact is
 * deterministic
 * and the test says something about the planner rather than about the map. The
 * engine-backed behaviour (real terrain, real power) is covered by the other
 * placement acceptance tests.
 * </p>
 */
public class PlacementPlannerTest {

    /** Flat, fully buildable map. */
    private static final class Flat implements TileAvailabilityGrid.TerrainSource {
        private final int w;
        private final int h;

        Flat(int w, int h) {
            this.w = w;
            this.h = h;
        }

        @Override
        public int mapWidth() {
            return w;
        }

        @Override
        public int mapHeight() {
            return h;
        }

        @Override
        public boolean isBuildable(int tx, int ty) {
            return true;
        }

        @Override
        public boolean isWalkable(int tx, int ty) {
            return true;
        }

        @Override
        public boolean isResource(int tx, int ty) {
            return false;
        }

        @Override
        public boolean isDepotOrigin(int tx, int ty) {
            return false;
        }
    }

    private static CataloguePlacementPlanner planner(int w, int h) {
        return new CataloguePlacementPlanner(new TileAvailabilityGrid(new Flat(w, h)));
    }

    /**
     * A planner over a grid with NO block factories, so the exact-tile and footprint
     * rules are tested on their own rather than through whatever a Block happened to
     * reserve. The block interaction has its own test.
     */
    private static CataloguePlacementPlanner blocklessPlanner(int w, int h) {
        TileAvailabilityGrid grid = new TileAvailabilityGrid(new Flat(w, h));
        return new CataloguePlacementPlanner(grid, null, null, false);
    }

    @Test
    public void aGatewayIsPlacedOnAFreeTileOfItsOwnFootprint() {
        CataloguePlacementPlanner planner = blocklessPlanner(40, 40);

        planner.startPass();
        PlacementReservation reservation = planner.reservePlacement(
                UnitProducible.of(AUnitType.Protoss_Gateway),
                TargetPlacement.inNeighbourhood(20, 20),
                0);

        assertTrue(reservation.isSuccessful(), "a Gateway must be placeable on a flat map");
        assertTrue(reservation.tileX() >= 0 && reservation.tileY() >= 0);
    }

    @Test
    public void twoBuildingsInOnePassDoNotShareATile() {
        CataloguePlacementPlanner planner = blocklessPlanner(40, 40);
        planner.startPass();

        PlacementReservation first = planner.reservePlacement(
                UnitProducible.of(AUnitType.Protoss_Gateway),
                TargetPlacement.inNeighbourhood(20, 20), 0);
        PlacementReservation second = planner.reservePlacement(
                UnitProducible.of(AUnitType.Protoss_Gateway),
                TargetPlacement.inNeighbourhood(20, 20), 0);

        assertTrue(first.isSuccessful() && second.isSuccessful(),
                "a flat map has room for two Gateways");
        assertNotEquals(first.tileX() + ":" + first.tileY(), second.tileX() + ":" + second.tileY(),
                "the pass-scoped reservation must keep two identical buildings off one tile");
    }

    @Test
    public void aNewPassClearsTheReservations() {
        CataloguePlacementPlanner planner = planner(40, 40);

        planner.startPass();
        PlacementReservation first = planner.reservePlacement(
                UnitProducible.of(AUnitType.Protoss_Pylon),
                TargetPlacement.inNeighbourhood(20, 20), 0);

        // The plan is recomputed every frame, so the same tile may be offered again.
        planner.startPass();
        PlacementReservation again = planner.reservePlacement(
                UnitProducible.of(AUnitType.Protoss_Pylon),
                TargetPlacement.inNeighbourhood(20, 20), 0);

        assertEquals(first.tileX() + ":" + first.tileY(), again.tileX() + ":" + again.tileY(),
                "reservations are per pass, not permanent");
    }

    @Test
    public void anExactTileIsHonouredWhenItIsFree() {
        CataloguePlacementPlanner planner = blocklessPlanner(40, 40);
        planner.startPass();

        PlacementReservation reservation = planner.reservePlacement(
                UnitProducible.of(AUnitType.Protoss_Gateway),
                TargetPlacement.exactTile(25, 25), 0);

        assertTrue(reservation.isSuccessful());
        assertEquals(25, reservation.tileX());
        assertEquals(25, reservation.tileY());
    }

    @Test
    public void anExactTileIsRefusedWhenSomethingIsAlreadyReservedThere() {
        CataloguePlacementPlanner planner = blocklessPlanner(40, 40);
        planner.startPass();

        // First building claims the area, second asks for the same exact tile.
        planner.reservePlacement(
                UnitProducible.of(AUnitType.Protoss_Gateway),
                TargetPlacement.exactTile(25, 25), 0);

        PlacementReservation second = planner.reservePlacement(
                UnitProducible.of(AUnitType.Protoss_Gateway),
                TargetPlacement.exactTile(25, 25), 0);

        assertFalse(second.isSuccessful(),
                "an explicit tile that is no longer free must be refused, not handed back - "
                        + "this is the stored-exact-position bug POSITION-FINDER.md §2.2 describes");
    }

    @Test
    public void aBuildingTooLargeForTheFreeSpaceIsRefused() {
        // A map just wide enough for a 4x3 footprint, then blocked so nothing fits.
        TileAvailabilityGrid grid = new TileAvailabilityGrid(new Flat(4, 3));
        CataloguePlacementPlanner planner = new CataloguePlacementPlanner(grid, null, null, false);
        planner.startPass();

        PlacementReservation first = planner.reservePlacement(
                UnitProducible.of(AUnitType.Protoss_Nexus), TargetPlacement.anywhere(), 0);
        assertTrue(first.isSuccessful(), "the only 4x3 spot on a 4x3 map is free");

        // Every tile is now used, so a second Nexus cannot fit anywhere.
        planner.startPass();
        PlacementReservation second = planner.reservePlacement(
                UnitProducible.of(AUnitType.Protoss_Nexus), TargetPlacement.anywhere(), 0);
        assertFalse(second.isSuccessful(),
                "with the map full, a 4x3 building must be refused rather than overlap");
    }

    @Test
    public void aUnitIsAskedForAPlacementOnlyWhenItIsABuilding() {
        // The seam is asked for every planned building; a unit that occupies no tile
        // in a real game is not the planner's business. What matters is that asking
        // does not throw and does not consume the pass.
        CataloguePlacementPlanner planner = blocklessPlanner(40, 40);
        planner.startPass();

        planner.reservePlacement(
                UnitProducible.of(AUnitType.Protoss_Zealot),
                TargetPlacement.anywhere(), 0);

        // A building asked right after must still get a tile: the unit request must
        // not have consumed anything.
        PlacementReservation after = planner.reservePlacement(
                UnitProducible.of(AUnitType.Protoss_Gateway),
                TargetPlacement.inNeighbourhood(20, 20), 0);

        assertTrue(after.isSuccessful(),
                "a unit request must not consume the pass's capacity");
    }
}
