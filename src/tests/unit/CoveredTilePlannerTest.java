package tests.unit;

import atlantis.map.position.APosition;
import atlantis.placement.core.TileAvailabilityGrid;
import atlantis.placement.engine.CataloguePlacementPlanner;
import atlantis.production.constructions.position.conditions.OverlapsOurStandingBuilding;
import atlantis.production.placement.PlacementReservation;
import atlantis.production.producibles.Producible;
import atlantis.production.placement.TargetPlacement;
import atlantis.production.producibles.UnitProducible;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Building-covered tiles are never served (NEXT #145): the planner used to
 * hand out tiles a standing building covers, and every warp there died to
 * a proximity-finish cancel while the goal re-fired on the same tile
 * (measured: 17 same-tile Pylon re-fires in the opening sim, 2065x live
 * on [105,115]). A rank-time skip: the next candidate wins instead.
 */
@Tag("placement")
public class CoveredTilePlannerTest extends WorldStubForTests {

    private static CataloguePlacementPlanner planner(int w, int h) {
        return new CataloguePlacementPlanner(new TileAvailabilityGrid(new FlatTerrain(w, h)), null, null, false);
    }

    @Test
    public void coveredTileIsSkipped() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 30, 30);
        world(5, fakeOurs(nexus, probe), fakeEnemies(), () -> {
            CataloguePlacementPlanner planner = planner(40, 40);
            planner.startPass();
            Producible pylon = UnitProducible.of(AUnitType.Protoss_Pylon);
            PlacementReservation reservation = planner.reservePlacement(
                pylon, TargetPlacement.inNeighbourhood(20, 20), 100);

            assertTrue(reservation.isSuccessful(), "a free tile exists nearby");
            APosition answered = APosition.create(reservation.tileX(), reservation.tileY());
            assertFalse(
                OverlapsOurStandingBuilding.check(answered, AUnitType.Protoss_Pylon),
                "the served tile never overlaps the standing Nexus footprint");
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
