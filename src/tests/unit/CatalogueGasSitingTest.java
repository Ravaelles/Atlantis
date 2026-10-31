package tests.unit;

import atlantis.map.position.APosition;
import atlantis.placement.core.TileAvailabilityGrid;
import atlantis.placement.engine.CataloguePlacementPlanner;
import atlantis.production.placement.PlacementReservation;
import atlantis.production.producibles.Producible;
import atlantis.production.placement.TargetPlacement;
import atlantis.production.producibles.UnitProducible;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeOurs;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;

/**
 * A refinery is sited on its geyser's own tile, never searched: the terrain
 * grid marks geyser tiles blocked (they are resources), so the generic rank
 * could never serve one and V2 never built gas (live f10200: 16 Zealots,
 * zero Dragoons, bank 714, no Assimilator ever - same in sim). The planner's
 * gas branch answers the legacy exact-geyser rule directly.
 */
@Tag("placement")
public class CatalogueGasSitingTest extends WorldStubForTests {

    private static final int GX = 86;
    private static final int GY = 114;

    private static CataloguePlacementPlanner planner(int w, int h) {
        return new CataloguePlacementPlanner(
            new TileAvailabilityGrid(new FlatTerrain(w, h)), null, null, false);
    }

    @Test
    public void assimilatorLandsOnItsGeyserTile() {
        FakeUnit base = fake(AUnitType.Protoss_Nexus, 80, 110);
        neutralInWorld = new FakeUnit[]{fake(AUnitType.Resource_Vespene_Geyser, GX, GY)};

        world(1, fakeOurs(base), fakeEnemies(), () -> {
            CataloguePlacementPlanner planner = planner(128, 128);
            planner.startPass();
            Producible gas = UnitProducible.of(AUnitType.Protoss_Assimilator);
            PlacementReservation reservation =
                planner.reservePlacement(gas, TargetPlacement.anywhere(), 100);

            assertTrue(reservation.isSuccessful(), "a free geyser must be served, not declined");
            assertEquals(GX, reservation.tileX(), "refinery tile x is the geyser's own");
            assertEquals(GY, reservation.tileY(), "refinery tile y is the geyser's own");
        });
    }

    @Test
    public void secondGasRequestSamePassFailsClosed() {
        FakeUnit base = fake(AUnitType.Protoss_Nexus, 80, 110);
        neutralInWorld = new FakeUnit[]{fake(AUnitType.Resource_Vespene_Geyser, GX, GY)};

        world(1, fakeOurs(base), fakeEnemies(), () -> {
            CataloguePlacementPlanner planner = planner(128, 128);
            planner.startPass();
            Producible gas = UnitProducible.of(AUnitType.Protoss_Assimilator);
            PlacementReservation first =
                planner.reservePlacement(gas, TargetPlacement.anywhere(), 100);
            assertTrue(first.isSuccessful(), "first gas order takes the geyser");

            PlacementReservation second =
                planner.reservePlacement(gas, TargetPlacement.anywhere(), 100);
            assertFalse(second.isSuccessful(), "same pass, same geyser: no double booking");
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
