package tests.unit;

import atlantis.map.position.APosition;
import atlantis.placement.core.BuildLocationCatalogue;
import atlantis.placement.core.ReservedSlots;
import atlantis.placement.core.TileAvailabilityGrid;
import atlantis.placement.engine.CataloguePlacementPlanner;
import atlantis.production.engine.ProductionEngine;
import atlantis.production.placement.PlacementReservation;
import atlantis.production.placement.TargetPlacement;
import atlantis.production.producibles.UnitProducible;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;
import tests.fakes.SnapshotTerrain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * V3 single-door support half (owner order 2026-11-06): the planner
 * only DECLARES power support; the composition root forwards it.
 * An unpowered Gateway demand on TauCross must mint a Pylon
 * construction on a covering planned slot - exactly once, with
 * exactly one supported twin - and a re-offer must dedupe silently.
 */
@Tag("placement")
public class ForwardPlannedSupportTest extends WorldStubForTests {

    @AfterEach
    public void clean() {
        ReservedSlots.clearForTests();
        EventLog.clearForTests();
        atlantis.production.constructions.ConstructionRequests.constructions.clear();
    }

    private static int supportedTwins() {
        int n = 0;
        for (String line : EventLog.toJsonLines().split("\n")) {
            if (line.contains("\"result\":\"supported\"")) n++;
        }

        return n;
    }

    private static int droppedTwins(String reason) {
        int n = 0;
        for (String line : EventLog.toJsonLines().split("\n")) {
            if (line.contains("\"result\":\"support-dropped\"")
                && line.contains("\"reason\":\"" + reason + "\"")) n++;
        }

        return n;
    }

    @Test
    public void brokeSupportTwinsItsGateOnce() {
        SnapshotTerrain terrain = new SnapshotTerrain(SnapshotTerrain.tauCross());
        APosition main = terrain.baseTile(1);
        currentMinerals = 0;

        TileAvailabilityGrid planGrid = new TileAvailabilityGrid(terrain);
        java.util.List<ReservedSlots.Slot> layout = ReservedSlots.planOn(
            new BuildLocationCatalogue(planGrid),
            main,
            AUnitType.Protoss_Gateway, 10);
        ReservedSlots.setSlotsForTests(layout);

        FakeUnit probe = fake(AUnitType.Protoss_Probe, main.tx() + 2, main.ty());
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, main.tx(), main.ty()),
            probe
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            TileAvailabilityGrid grid = new TileAvailabilityGrid(terrain);
            CataloguePlacementPlanner planner = new CataloguePlacementPlanner(
                grid,
                new atlantis.placement.race.ProtossPlacementStrategy(
                    new atlantis.placement.core.PsiGating(
                        new atlantis.placement.engine.EnginePowerSource())),
                null, true);
            planner.startPass();

            PlacementReservation failed = planner.reservePlacement(
                UnitProducible.of(AUnitType.Protoss_Gateway),
                TargetPlacement.inNeighbourhood(main.tx(), main.ty()), 100);
            assertTrue(!failed.isSuccessful(), "no Pylon anywhere: no Gateway reservation");

            ProductionEngine.forwardDrainedSupport(
                planner, new atlantis.production.execution.GameOrderDirector(), 100);
            ProductionEngine.forwardDrainedSupport(
                planner, new atlantis.production.execution.GameOrderDirector(), 100);

            assertEquals(1, droppedTwins("unaffordable"),
                "a broke support names its gate once, throttled on the repeat: "
                    + EventLog.toJsonLines());
        });
    }

    @Test
    public void unpoweredGatewayDemandMintsCoveringPylonOnce() {
        SnapshotTerrain terrain = new SnapshotTerrain(SnapshotTerrain.tauCross());
        APosition main = terrain.baseTile(1);
        currentMinerals = 400;

        TileAvailabilityGrid planGrid = new TileAvailabilityGrid(terrain);
        java.util.List<ReservedSlots.Slot> layout = ReservedSlots.planOn(
            new BuildLocationCatalogue(planGrid),
            main,
            AUnitType.Protoss_Gateway, 10);
        ReservedSlots.setSlotsForTests(layout);

        // Two probes: the support mint needs a free builder, and the
        // no-builder guard (correctly) refuses a one-worker world.
        FakeUnit probe = fake(AUnitType.Protoss_Probe, main.tx() + 2, main.ty());
        FakeUnit spare = fake(AUnitType.Protoss_Probe, main.tx() + 3, main.ty() + 1);
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, main.tx(), main.ty()),
            probe,
            spare
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            TileAvailabilityGrid grid = new TileAvailabilityGrid(terrain);
            try {
                CataloguePlacementPlanner planner = new CataloguePlacementPlanner(
                    grid,
                    new atlantis.placement.race.ProtossPlacementStrategy(
                        new atlantis.placement.core.PsiGating(
                            new atlantis.placement.engine.EnginePowerSource())),
                    null, true);
                planner.startPass();

                PlacementReservation failed = planner.reservePlacement(
                    UnitProducible.of(AUnitType.Protoss_Gateway),
                    TargetPlacement.inNeighbourhood(main.tx(), main.ty()), 100);
                assertTrue(!failed.isSuccessful(), "no Pylon anywhere: no Gateway reservation");

                ProductionEngine.forwardDrainedSupport(
                    planner, new atlantis.production.execution.GameOrderDirector(), 100);

                atlantis.production.constructions.Construction support = null;
                for (atlantis.production.constructions.Construction c :
                    atlantis.production.constructions.ConstructionRequests.constructions) {
                    if (c.buildingType() != null && c.buildingType().isPylon()
                        && c.buildPosition() != null) {
                        support = c;
                    }
                }
                assertNotNull(support,
                    "the composition root mints the support Pylon (GAME_120B0292 never did)");

                final APosition at = support.buildPosition();
                boolean onPlannedSlot = false;
                boolean coversGateway = false;
                for (ReservedSlots.Slot slot : layout) {
                    if (!slot.type.isPylon()) continue;
                    if (slot.tile.tx() != at.tx() || slot.tile.ty() != at.ty()) continue;
                    onPlannedSlot = true;
                    for (ReservedSlots.Slot gateway : layout) {
                        if (gateway.type.isPylon()) continue;
                        if (Math.abs(gateway.tile.tx() - at.tx()) <= 6
                            && Math.abs(gateway.tile.ty() - at.ty()) <= 6) {
                            coversGateway = true;
                        }
                    }
                }
                assertTrue(onPlannedSlot, "the support Pylon lands on a planned P slot: " + at);
                assertTrue(coversGateway, "that slot covers a Gateway slot: " + at);
                assertEquals(1, supportedTwins(), "exactly one supported twin, no per-frame spam");

                int before = atlantis.production.constructions.ConstructionRequests.constructions.size();
                planner.startPass();
                PlacementReservation stillFailing = planner.reservePlacement(
                    UnitProducible.of(AUnitType.Protoss_Gateway),
                    TargetPlacement.inNeighbourhood(main.tx(), main.ty()), 101);
                assertTrue(!stillFailing.isSuccessful(), "the walking Pylon does not power yet");
                ProductionEngine.forwardDrainedSupport(
                    planner, new atlantis.production.execution.GameOrderDirector(), 101);
                assertEquals(before,
                    atlantis.production.constructions.ConstructionRequests.constructions.size(),
                    "a re-offer on the same tile dedupes instead of doubling");
                assertEquals(1, supportedTwins(),
                    "the re-offer stays silent too: one twin per support Pylon");
            }
            finally {
                ReservedSlots.clearForTests();
            }
        });
    }

    @Test
    public void stuckPylonRowBlocksSecondSupportPylon() {
        // Live B189F12D 2:14: the first Pylon row lost its builder and
        // sat unstarted while a support Pylon minted behind it on a poor
        // bank - the cap must see the builderless row, not just warping
        // and not-started ones.
        SnapshotTerrain terrain = new SnapshotTerrain(SnapshotTerrain.tauCross());
        APosition main = terrain.baseTile(1);
        currentMinerals = 150;

        TileAvailabilityGrid planGrid = new TileAvailabilityGrid(terrain);
        java.util.List<ReservedSlots.Slot> layout = ReservedSlots.planOn(
            new BuildLocationCatalogue(planGrid),
            main,
            AUnitType.Protoss_Gateway, 10);
        ReservedSlots.setSlotsForTests(layout);

        FakeUnit probe = fake(AUnitType.Protoss_Probe, main.tx() + 2, main.ty());
        FakeUnit spare = fake(AUnitType.Protoss_Probe, main.tx() + 3, main.ty() + 1);
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, main.tx(), main.ty()),
            probe,
            spare
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            TileAvailabilityGrid grid = new TileAvailabilityGrid(terrain);
            try {
                // The stuck first Pylon: adopted then builder lost, row
                // alive, no unit, no warp. Exactly the live shape.
                atlantis.production.constructions.Construction stuck =
                    new atlantis.production.constructions.Construction(AUnitType.Protoss_Pylon);
                stuck.setPositionToBuild(APosition.create(main.tx() + 2, main.ty() + 2));
                stuck.setStatus(atlantis.production.constructions.ConstructionOrderStatus.IN_PROGRESS);
                atlantis.production.constructions.ConstructionRequests.addConstruction(stuck);

                CataloguePlacementPlanner planner = new CataloguePlacementPlanner(
                    grid,
                    new atlantis.placement.race.ProtossPlacementStrategy(
                        new atlantis.placement.core.PsiGating(
                            new atlantis.placement.engine.EnginePowerSource())),
                    null, true);
                planner.startPass();

                PlacementReservation failed = planner.reservePlacement(
                    UnitProducible.of(AUnitType.Protoss_Gateway),
                    TargetPlacement.inNeighbourhood(main.tx(), main.ty()), 100);
                assertTrue(!failed.isSuccessful(), "no Pylon anywhere: no Gateway reservation");

                ProductionEngine.forwardDrainedSupport(
                    planner, new atlantis.production.execution.GameOrderDirector(), 100);

                assertEquals(1,
                    atlantis.production.constructions.ConstructionRequests.constructions.size(),
                    "the stuck row blocks the second Pylon: still exactly one row");
                assertEquals(0, supportedTwins(), "no supported twin on a refused support");
                assertEquals(1, droppedTwins("pylon-cap"),
                    "the refusal names the cap, not silence");
            }
            finally {
                ReservedSlots.clearForTests();
            }
        });
    }
}
