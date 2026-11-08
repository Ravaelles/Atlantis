package tests.unit;

import atlantis.map.position.APosition;
import atlantis.placement.core.BuildLocationCatalogue;
import atlantis.placement.core.ReservedSlots;
import atlantis.placement.core.TileAvailabilityGrid;
import atlantis.placement.engine.CataloguePlacementPlanner;
import atlantis.production.placement.PlacementReservation;
import atlantis.production.placement.TargetPlacement;
import atlantis.production.producibles.UnitProducible;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;
import tests.fakes.SnapshotTerrain;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * V3 StarEngine power contract, planner level (owner order
 * 2026-11-06): on the TauCross snapshot, with the REAL placement
 * strategy (ProtossPlacementStrategy + PsiGating + EnginePowerSource
 * over live stub units), a Gateway must reserve on a powered tile
 * when a Pylon stands in reach - and must fail closed with a
 * declared support request when nothing powers it. The null-strategy
 * pins prove slot ORDER; these prove POWER, the exact gate live
 * games kept tripping (GAME_120B0292: 9x no-placement heartbeats,
 * zero Gateway warps).
 */
@Tag("placement")
public class PlannerPowerReproTest extends WorldStubForTests {

    private static CataloguePlacementPlanner realPlanner(TileAvailabilityGrid grid) {
        return new CataloguePlacementPlanner(
            grid,
            new atlantis.placement.race.ProtossPlacementStrategy(
                new atlantis.placement.core.PsiGating(
                    new atlantis.placement.engine.EnginePowerSource())),
            null, true);
    }

    @Test
    public void poweredGatewayReservesOnTauCross() {
        SnapshotTerrain terrain = new SnapshotTerrain(SnapshotTerrain.tauCross());
        APosition main = terrain.baseTile(0);

        TileAvailabilityGrid planGrid = new TileAvailabilityGrid(terrain);
        java.util.List<ReservedSlots.Slot> layout = ReservedSlots.planOn(
            new BuildLocationCatalogue(planGrid),
            main,
            AUnitType.Protoss_Gateway, 10);
        ReservedSlots.Slot g2 = null;
        for (ReservedSlots.Slot slot : layout) {
            if (slot.label.equals("G2")) g2 = slot;
        }
        assertNotNull(g2, "the layout owes G2");
        final APosition g2tile = g2.tile;

        FakeUnit probe = fake(AUnitType.Protoss_Probe, main.tx() + 2, main.ty());
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, main.tx(), main.ty()),
            fake(AUnitType.Protoss_Pylon, g2tile.tx() + 5, g2tile.ty()),
            probe
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            TileAvailabilityGrid grid = new TileAvailabilityGrid(terrain);
            ReservedSlots.setSlotsForTests(layout);
            try {
                CataloguePlacementPlanner planner = realPlanner(grid);
                planner.startPass();

                PlacementReservation result = planner.reservePlacement(
                    UnitProducible.of(AUnitType.Protoss_Gateway),
                    TargetPlacement.inNeighbourhood(main.tx(), main.ty()), 100);

                assertTrue(result.isSuccessful(),
                    "a powered Gateway slot must reserve on TauCross (live GAME_120B0292 never did)");
                assertTrue(
                    atlantis.production.constructions.position.conditions.can_build_here.IsPoweredByAPylon.check(
                        APosition.create(result.tileX(), result.tileY()), AUnitType.Protoss_Gateway),
                    "the served tile is really powered: " + result.tileX() + "," + result.tileY());
            }
            finally {
                ReservedSlots.clearForTests();
            }
        });
    }

    /**
     * The live GAME_120B0292 shape on its own spawn (TauCross start 1,
     * top-right): Nexus + the exact Pylon tiles the game warped while
     * the Gateway demand starved 1:12-4:34. If reservePlacement fails
     * here, StarEngine reproduces the live drought and the fix can
     * land without a single Wine game (owner order 2026-11-06).
     */
    @Test
    public void liveDroughtShapeReservesOnTauCross() {
        SnapshotTerrain terrain = new SnapshotTerrain(SnapshotTerrain.tauCross());
        APosition main = terrain.baseTile(1);

        TileAvailabilityGrid planGrid = new TileAvailabilityGrid(terrain);
        java.util.List<ReservedSlots.Slot> layout = ReservedSlots.planOn(
            new BuildLocationCatalogue(planGrid),
            main,
            AUnitType.Protoss_Gateway, 10);
        ReservedSlots.setSlotsForTests(layout);

        FakeUnit probe = fake(AUnitType.Protoss_Probe, main.tx() + 2, main.ty());
        final FakeUnit nexus = fake(AUnitType.Protoss_Nexus, main.tx(), main.ty());
        final FakeUnit pyl1 = fake(AUnitType.Protoss_Pylon, 114, 11);
        final FakeUnit pyl2 = fake(AUnitType.Protoss_Pylon, 121, 22);
        final FakeUnit pyl3 = fake(AUnitType.Protoss_Pylon, 126, 21);
        final java.util.List<FakeUnit> cast = java.util.Arrays.asList(nexus, pyl1, pyl2, pyl3, probe);
        world(5, fakeOurs(nexus, pyl1, pyl2, pyl3, probe), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            TileAvailabilityGrid grid = new TileAvailabilityGrid(terrain);
            ReservedSlots.setSlotsForTests(layout);
            try {
                CataloguePlacementPlanner planner = new CataloguePlacementPlanner(
                    grid,
                    new atlantis.placement.race.ProtossPlacementStrategy(
                        new atlantis.placement.core.PsiGating(
                            new atlantis.placement.engine.EnginePowerSource())),
                    null, true);
                planner.startPass();

                PlacementReservation result = planner.reservePlacement(
                    UnitProducible.of(AUnitType.Protoss_Gateway),
                    TargetPlacement.inNeighbourhood(main.tx(), main.ty()), 100);

                assertTrue(result.isSuccessful(),
                    "live Pylons stand, a Gateway slot must reserve (GAME_120B0292 never did)");

                // Visual proof hook (owner order 2026-11-06): with
                // -Datlantis.visualize=true the TauCross layout, units and
                // reserved slots stay on screen 5 s after the test.
                tests.unit.sim.SimVisualizer.show(SnapshotTerrain.tauCross(),
                    "live-drought shape, base 1", cast);
            }
            finally {
                tests.unit.sim.SimVisualizer.finish();
                ReservedSlots.clearForTests();
            }
        });
    }

    @Test
    public void unpoweredCannonDeclaresSupportOnTauCross() {
        SnapshotTerrain terrain = new SnapshotTerrain(SnapshotTerrain.tauCross());
        APosition main = terrain.baseTile(0);

        FakeUnit probe = fake(AUnitType.Protoss_Probe, main.tx() + 2, main.ty());
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, main.tx(), main.ty()),
            probe
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            TileAvailabilityGrid grid = new TileAvailabilityGrid(terrain);
            try {
                CataloguePlacementPlanner planner = realPlanner(grid);
                planner.startPass();

                PlacementReservation result = planner.reservePlacement(
                    UnitProducible.of(AUnitType.Protoss_Photon_Cannon),
                    TargetPlacement.inNeighbourhood(main.tx(), main.ty()), 100);

                assertTrue(!result.isSuccessful(), "no Pylon anywhere: no Cannon reservation");
                java.util.List<atlantis.placement.core.SupportRequest> drained =
                    planner.drainSupportRequests();
                assertTrue(!drained.isEmpty(), "the planner declares its support Pylon");
            }
            finally {
                ReservedSlots.clearForTests();
            }
        });
    }

    @Test
    public void unpoweredGatewayDeclaresNoSupportOnTauCross() {
        SnapshotTerrain terrain = new SnapshotTerrain(SnapshotTerrain.tauCross());
        APosition main = terrain.baseTile(0);

        FakeUnit probe = fake(AUnitType.Protoss_Probe, main.tx() + 2, main.ty());
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, main.tx(), main.ty()),
            probe
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            TileAvailabilityGrid grid = new TileAvailabilityGrid(terrain);
            try {
                CataloguePlacementPlanner planner = realPlanner(grid);
                planner.startPass();

                PlacementReservation result = planner.reservePlacement(
                    UnitProducible.of(AUnitType.Protoss_Gateway),
                    TargetPlacement.inNeighbourhood(main.tx(), main.ty()), 100);

                assertTrue(!result.isSuccessful(), "no Pylon anywhere: no Gateway reservation");
                java.util.List<atlantis.placement.core.SupportRequest> drained =
                    planner.drainSupportRequests();
                assertTrue(drained.isEmpty(),
                    "a Gateway never orders a Pylon - it waits for planned power (owner 2026-11-08)");
            }
            finally {
                ReservedSlots.clearForTests();
            }
        });
    }
}
