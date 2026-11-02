package tests.unit;

import atlantis.map.position.APosition;
import atlantis.placement.engine.CataloguePlacementPlanner;
import atlantis.placement.core.TileAvailabilityGrid;
import atlantis.production.constructions.position.RefusedTiles;
import atlantis.production.placement.PlacementReservation;
import atlantis.production.placement.TargetPlacement;
import atlantis.production.producibles.Producible;
import atlantis.production.producibles.UnitProducible;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A tile the engine hard-refused must not be re-served while the
 * refusal stands (NEXT #151 remainder: the V2-direct path mints from
 * scheduler reservations, which never consulted the refused-tile
 * memory - measured live 20261102_014019: Nexus [105,115] refused by
 * the engine 1031 times while bot-side math read buildable, the same
 * tile re-served every frame, natural never online).
 *
 * <p>Pure planner seam, no game: reserve, note the refusal, reserve
 * again - the second reservation must name a different tile (or fail
 * when nothing else serves).</p>
 */
@Tag("placement")
public class RefusedTilePlannerTest extends WorldStubForTests {

    @AfterEach
    public void clearRefused() {
        RefusedTiles.clearForTests();
    }

    private static CataloguePlacementPlanner planner(int w, int h) {
        return new CataloguePlacementPlanner(new TileAvailabilityGrid(new FlatTerrain(w, h)), null, null, false);
    }

    @Test
    public void refusedTileIsNotReservedAgain() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 30, 30);
        world(5, fakeOurs(nexus, probe), fakeEnemies(), () -> {
            CataloguePlacementPlanner planner = planner(40, 40);
            planner.startPass();
            Producible pylon = UnitProducible.of(AUnitType.Protoss_Pylon);
            PlacementReservation first = planner.reservePlacement(
                pylon, TargetPlacement.inNeighbourhood(20, 20), 100);
            assertTrue(first.isSuccessful(), "a free tile exists nearby");

            RefusedTiles.note(
                APosition.create(first.tileX(), first.tileY()),
                AUnitType.Protoss_Pylon);

            planner.startPass();
            PlacementReservation second = planner.reservePlacement(
                pylon, TargetPlacement.inNeighbourhood(20, 20), 100);
            assertTrue(second.isSuccessful(), "other tiles still serve");
            assertFalse(
                second.tileX() == first.tileX() && second.tileY() == first.tileY(),
                "the refused tile is skipped, not re-served: "
                    + first.tileX() + "," + first.tileY());
        });
    }

    private static final class FlatTerrain implements TileAvailabilityGrid.TerrainSource {
        private final int w;
        private final int h;

        FlatTerrain(int w, int h) {
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
}
