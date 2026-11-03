package tests.unit;

import atlantis.map.position.APosition;
import atlantis.map.snapshot.MapSnapshot;
import atlantis.placement.core.TileAvailabilityGrid;
import atlantis.placement.engine.CataloguePlacementPlanner;
import atlantis.production.placement.PlacementReservation;
import atlantis.production.placement.TargetPlacement;
import atlantis.production.producibles.Producible;
import atlantis.production.producibles.UnitProducible;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;
import tests.fakes.SnapshotTerrain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * V3 S0: the siting vehicle. The planner stack runs headless against
 * the real TauCross snapshot - tiles, regions, chokes, resources,
 * depot origins - instead of a flat stub, so a green suite cannot
 * serve tiles the engine refuses (measured live: unpowered Gateway
 * at [123,26], 82 s of refused Builds, defeat with a full bank).
 *
 * <p>This test pins the vehicle itself, not a rule: both building
 * types reserve successfully near a real base, on tiles the snapshot
 * calls walkable and buildable, deterministically. Every siting pin
 * from here on drives this harness.</p>
 */
@Tag("placement")
public class SnapshotSitingTest extends WorldStubForTests {

    private static CataloguePlacementPlanner planner(SnapshotTerrain terrain) {
        return new CataloguePlacementPlanner(
            new TileAvailabilityGrid(terrain), null, null, true);
    }

    @Test
    public void tauCrossSnapshotLoads() {
        MapSnapshot snap = SnapshotTerrain.tauCross();

        assertTrue(snap.map.contains("TauCross"), "the shared fixture is TauCross: " + snap.map);
        assertEquals(128, snap.w);
        assertEquals(128, snap.h);
        assertTrue(snap.bases.size() >= 2, "start locations with mineral lines");
        assertTrue(snap.resources.size() > 50, "minerals + geysers present");
    }

    @Test
    public void pylonAndGatewayReserveOnTauCrossNearAMain() {
        MapSnapshot snap = SnapshotTerrain.tauCross();
        APosition main = new SnapshotTerrain(snap).baseTile(0);

        // Fake units stand on the snapshot's own ground so travel answers
        // read real distances instead of cross-map nonsense.
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, main.tx(), main.ty());
        FakeUnit probe = fake(AUnitType.Protoss_Probe, main.tx() + 2, main.ty());

        world(5, fakeOurs(nexus, probe), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            SnapshotTerrain terrain = new SnapshotTerrain(snap);
            CataloguePlacementPlanner planner = planner(terrain);
            planner.startPass();

            Producible pylon = UnitProducible.of(AUnitType.Protoss_Pylon);
            PlacementReservation first = planner.reservePlacement(
                pylon, TargetPlacement.inNeighbourhood(main.tx(), main.ty()), 100);
            assertTrue(first.isSuccessful(), "a Pylon site exists near a real main");

            Producible gateway = UnitProducible.of(AUnitType.Protoss_Gateway);
            PlacementReservation second = planner.reservePlacement(
                gateway, TargetPlacement.inNeighbourhood(main.tx(), main.ty()), 100);
            assertTrue(second.isSuccessful(), "a Gateway site exists near a real main");

            for (PlacementReservation reservation : new PlacementReservation[]{first, second}) {
                int tx = reservation.tileX();
                int ty = reservation.tileY();
                assertTrue(terrain.isWalkable(tx, ty), "served tile walks: " + tx + "," + ty);
                assertTrue(terrain.isBuildable(tx, ty), "served tile builds: " + tx + "," + ty);
            }

            planner.startPass();
            PlacementReservation repeat = planner.reservePlacement(
                pylon, TargetPlacement.inNeighbourhood(main.tx(), main.ty()), 100);
            assertTrue(repeat.isSuccessful(), "second pass still serves");
            assertEquals(first.tileX(), repeat.tileX(), "same pass in, same tile out");
            assertEquals(first.tileY(), repeat.tileY(), "same pass in, same tile out");
        });
    }
}
