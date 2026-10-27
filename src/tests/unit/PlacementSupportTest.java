package tests.unit;

import atlantis.placement.blocks.BlockTemplates;
import atlantis.placement.core.BuildBlock;
import atlantis.placement.core.RacePlacementStrategy;
import atlantis.placement.core.SupportRequest;
import atlantis.placement.core.TileAvailabilityGrid;
import atlantis.placement.engine.CataloguePlacementPlanner;
import atlantis.placement.race.ProtossPlacementStrategy;
import atlantis.production.v2.PlacementReservation;
import atlantis.production.v2.TargetPlacement;
import atlantis.production.v2.UnitProducible;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * R1: placement declares support, it never performs it. A race strategy
 * answers WHAT and WHERE as data; the planner accumulates one pass worth;
 * the composition root that ran the pass forwards it to production.
 * No queue write happens below the composition root - the old
 * catch-Throwable around a direct AddToQueue is gone with the write.
 */
@Tag("placement")
public class PlacementSupportTest {

    @Test
    public void strategyDeclaresSupportAsData() {
        SupportRequest request =
            new ProtossPlacementStrategy(null).requestAvailabilitySupport(10, 20, "Protoss_Gateway");

        assertNotNull(request, "a Pylon makes the spot usable - declared, not ordered");
        assertEquals(AUnitType.Protoss_Pylon, request.unitType());
        assertEquals(10, request.tileX());
        assertEquals(20, request.tileY());
    }

    @Test
    public void plannerCollectsAndDrainsOnce() {
        CataloguePlacementPlanner planner = new CataloguePlacementPlanner(
            new TileAvailabilityGrid(new FlatTerrain()), new AlwaysUnpowered());
        planner.startPass();

        PlacementReservation result = planner.reservePlacement(
            UnitProducible.of(AUnitType.Protoss_Pylon), TargetPlacement.anywhere(), 0);

        assertFalse(result.isSuccessful(), "unpowered tile: no reservation, support instead");
        List<SupportRequest> drained = planner.drainSupportRequests();
        assertEquals(1, drained.size(), "one declared support per unpowered answer");
        assertEquals(AUnitType.Protoss_Pylon, drained.get(0).unitType());
        assertTrue(
            planner.drainSupportRequests().isEmpty(),
            "drain consumes: the next pass re-decides instead of double-ordering");
    }

    private static final class FlatTerrain implements TileAvailabilityGrid.TerrainSource {
        @Override
        public int mapWidth() {
            return 40;
        }

        @Override
        public int mapHeight() {
            return 40;
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

    private static final class AlwaysUnpowered implements RacePlacementStrategy {
        @Override
        public List<BuildBlock.Spec> blockTemplates() {
            return BlockTemplates.normal();
        }

        @Override
        public int framesUntilAvailable(int tx, int ty, String buildableTypeId) {
            return 0;
        }

        @Override
        public boolean requiresAvailability(String buildableTypeId) {
            return true;
        }

        @Override
        public AvailabilityVerdict availabilityVerdict(int tx, int ty, String buildableTypeId) {
            return AvailabilityVerdict.NEEDS_SUPPORT;
        }

        @Override
        public SupportRequest requestAvailabilitySupport(int tx, int ty, String buildableTypeId) {
            return new SupportRequest(AUnitType.Protoss_Pylon, tx, ty);
        }
    }
}
