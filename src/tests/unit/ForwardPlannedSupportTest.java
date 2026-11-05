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
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;
import tests.fakes.SnapshotTerrain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * V3 single-door support half (owner order 2026-11-06): the planner
 * only DECLARES power support; the composition root forwards it.
 * An unpowered Gateway demand must mint a Pylon construction on a
 * covering planned Pylon slot (not a hunt tile, not nothing), once,
 * with a supported twin - on TauCross, headless, no live game.
 */
@Tag("placement")
public class ForwardPlannedSupportTest extends WorldStubForTests {

    @AfterEach
    public void clean() {
        ReservedSlots.clearForTests();
        EventLog.clearForTests();
        atlantis.production.constructions.ConstructionRequests.constructions.clear();
    }

    private static CataloguePlacementPlanner realPlanner(TileAvailabilityGrid grid) {
        return new CataloguePlacementPlanner(
            grid,
            new atlantis.placement.race.ProtossPlacementStrategy(
                new atlantis.placement.core.PsiGating(
                    new atlantis.placement.engine.EnginePowerSource())),
            null, true);
    }

    @Test
    public void unpoweredGatewayDemandMintsCoveringPylon() {
        SnapshotTerrain terrain = new SnapshotTerrain(SnapshotTerrain.tauCross());
        APosition main = terrain.baseTile(1);

        TileAvailabilityGrid planGrid = new TileAvailabilityGrid(terrain);
        java.util.List<ReservedSlots.Slot> layout = ReservedSlots.planOn(
            new BuildLocationCatalogue(planGrid),
            main,
            AUnitType.Protoss_Gateway, 10);
        assertFalse(layout.isEmpty(), "the layout exists");

        currentMinerals = 400;
        FakeUnit probe = fake(AUnitType.Protoss_Probe, main.tx() + 2, main.ty());
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, main.tx(), main.ty()),
            probe
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            TileAvailabilityGrid grid = new TileAvailabilityGrid(terrain);
            ReservedSlots.setSlotsForTests(layout);
            try {
                CataloguePlacementPlanner planner = realPlanner(grid);
                planner.startPass();

                // No Pylon anywhere: the Gateway cannot be served, and
                // the planner must declare support instead of silence.
                PlacementReservation failed = planner.reservePlacement(
                    UnitProducible.of(AUnitType.Protoss_Gateway),
                    TargetPlacement.inNeighbourhood(main.tx(), main.ty()), 100);
                assertFalse(failed.isSuccessful(), "unpowered: no Gateway reservation");

                // The composition-root half: forward the buffered support.
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
                assertNotNull(support, "forwarding mints the support Pylon (GAME_120B0292 never did)");

                final APosition at = support.buildPosition();
                boolean onPlannedSlot = false;
                boolean coversGatewaySlot = false;
                for (ReservedSlots.Slot slot : layout) {
                    if (!slot.type.isPylon()) continue;
                    if (slot.tile.tx() != at.tx() || slot.tile.ty() != at.ty()) continue;
                    onPlannedSlot = true;
                    for (ReservedSlots.Slot gateway : layout) {
                        if (gateway.type.isPylon()) continue;
                        if (Math.max(Math.abs(gateway.tile.tx() - at.tx()),
                            Math.abs(gateway.tile.ty() - at.ty())) <= 6) {
                            coversGatewaySlot = true;
                        }
                    }
                }
                assertTrue(onPlannedSlot, "the support Pylon lands on a planned P slot: " + at);
                assertTrue(coversGatewaySlot, "that P slot covers a Gateway slot: " + at);
                assertTrue(EventLog.toJsonLines().contains("\"result\":\"supported\""),
                    "the forward twins the support: " + EventLog.toJsonLines());

                // Idempotent: a re-offer on the same tile must not mint a twin row.
                int before = atlantis.production.constructions.ConstructionRequests.constructions.size();
                ProductionEngine.forwardDrainedSupport(
                    planner, new atlantis.production.execution.GameOrderDirector(), 100);
                assertEquals(before,
                    atlantis.production.constructions.ConstructionRequests.constructions.size(),
                    "the same tile re-offer dedupes instead of doubling");
            }
            finally {
                ReservedSlots.clearForTests();
            }
        });
    }
}
