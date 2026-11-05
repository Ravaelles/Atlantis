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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;
import tests.fakes.SnapshotTerrain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * V3 S6 slot order (pure) + StarEngine power contract (owner order
 * 2026-11-06): the layout pins order without a world; the powered
 * reservation pins power with the REAL strategy (ProtossPlacementStrategy
 * + PsiGating + EnginePowerSource) on the TauCross snapshot. G1 sits
 * under our own Nexus (standing cover skips it, same as live), so the
 * power test feeds free G2.
 */
@Tag("placement")
public class SlotFirstPlannerTest extends tests.acceptance.WorldStubForTests {

    private static List<ReservedSlots.Slot> layout() {
        SnapshotTerrain terrain = new SnapshotTerrain(SnapshotTerrain.tauCross());
        TileAvailabilityGrid grid = new TileAvailabilityGrid(terrain);
        return ReservedSlots.planOn(
            new BuildLocationCatalogue(grid),
            terrain.baseTile(0),
            AUnitType.Protoss_Gateway, 10);
    }

    @AfterEach
    public void clean() {
        ReservedSlots.clearForTests();
    }

    @Test
    public void firstFreeSlotIsG1() {
        List<ReservedSlots.Slot> layout = layout();
        ReservedSlots.setSlotsForTests(layout);

        APosition tile = ReservedSlots.firstFreeSlotOfType(
            AUnitType.Protoss_Gateway, (t, b) -> false);

        assertNotNull(tile, "an empty board serves the first gateway slot");
        assertEquals(label(layout, "G1").tile.tx(), tile.tx(), "oldest planned first");
        assertEquals(label(layout, "G1").tile.ty(), tile.ty(), "oldest planned first");
    }

    @Test
    public void coveredSlotIsSkipped() {
        List<ReservedSlots.Slot> layout = layout();
        ReservedSlots.setSlotsForTests(layout);
        final APosition g1 = label(layout, "G1").tile;
        final APosition g2 = label(layout, "G2").tile;

        Set<String> covered = new HashSet<String>();
        covered.add(g1.tx() + "," + g1.ty());
        APosition tile = ReservedSlots.firstFreeSlotOfType(
            AUnitType.Protoss_Gateway,
            (t, b) -> covered.contains(t.tx() + "," + t.ty()));

        assertNotNull(tile, "a covered G1 skips, never declines while slots stay free");
        assertEquals(g2.tx(), tile.tx(), "the next free slot");
        assertEquals(g2.ty(), tile.ty(), "the next free slot");
    }

    @Test
    public void allCoveredMeansNoSlot() {
        List<ReservedSlots.Slot> layout = layout();
        ReservedSlots.setSlotsForTests(layout);

        Set<String> covered = new HashSet<String>();
        for (ReservedSlots.Slot slot : layout) {
            if (slot.type.equals(AUnitType.Protoss_Gateway)) {
                covered.add(slot.tile.tx() + "," + slot.tile.ty());
            }
        }
        APosition tile = ReservedSlots.firstFreeSlotOfType(
            AUnitType.Protoss_Gateway,
            (t, b) -> covered.contains(t.tx() + "," + t.ty()));

        assertNull(tile, "full slots fall through to the hunt, never invent a tile");
    }

    @Test
    public void unknownTypeAndNullsReadAsNoSlot() {
        ReservedSlots.setSlotsForTests(layout());

        assertNull(ReservedSlots.firstFreeSlotOfType(
            AUnitType.Protoss_Forge, (t, b) -> false),
            "no planned slots for tech - the hunt owns it");
        assertNull(ReservedSlots.firstFreeSlotOfType(null, (t, b) -> false),
            "null type never serves");
        assertNull(ReservedSlots.firstFreeSlotOfType(AUnitType.Protoss_Gateway, null),
            "null occupancy never serves");
        ReservedSlots.clearForTests();
        assertNull(ReservedSlots.firstFreeSlotOfType(
            AUnitType.Protoss_Gateway, (t, b) -> false),
            "no layout means no slot");
        assertTrue(true, "fail-closed paths never throw");
    }

    private static ReservedSlots.Slot label(List<ReservedSlots.Slot> layout, String label) {
        for (ReservedSlots.Slot slot : layout) {
            if (slot.label.equals(label)) return slot;
        }
        throw new IllegalArgumentException("no slot " + label);
    }

    @Test
    public void poweredSlotServesWithRealStrategy() {
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

                assertTrue(result.isSuccessful(), "a powered free slot must serve on TauCross");
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

    @Test
    public void unpoweredSlotsDeclareSupport() {
        SnapshotTerrain terrain = new SnapshotTerrain(SnapshotTerrain.tauCross());
        APosition main = terrain.baseTile(0);

        TileAvailabilityGrid planGrid = new TileAvailabilityGrid(terrain);
        java.util.List<ReservedSlots.Slot> layout = ReservedSlots.planOn(
            new BuildLocationCatalogue(planGrid),
            main,
            AUnitType.Protoss_Gateway, 10);

        FakeUnit probe = fake(AUnitType.Protoss_Probe, main.tx() + 2, main.ty());
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, main.tx(), main.ty()),
            probe
        ), fakeEnemies(), () -> {
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

                assertFalse(result.isSuccessful(), "no Pylon anywhere: no Gateway reservation");
                java.util.List<atlantis.placement.core.SupportRequest> drained =
                    planner.drainSupportRequests();
                assertFalse(drained.isEmpty(), "the unpowered slot declares its support Pylon");
                assertEquals(AUnitType.Protoss_Pylon, drained.get(0).unitType(),
                    "support means a Pylon");
            }
            finally {
                ReservedSlots.clearForTests();
            }
        });
    }
}
