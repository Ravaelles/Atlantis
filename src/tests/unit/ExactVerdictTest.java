package tests.unit;

import atlantis.map.position.APosition;
import atlantis.placement.core.BuildLocationCatalogue;
import atlantis.placement.core.ReservedSlots;
import atlantis.placement.core.TileAvailabilityGrid;
import atlantis.placement.engine.CataloguePlacementPlanner;
import atlantis.production.constructions.position.RefusedTiles;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Exact tiles answer the single verdict (V3 one-door): a refused,
 * unpowered or mineral-crowded exact hint fails instead of minting
 * an illegal warp with no downstream check. Same vehicle as the
 * slot-first power pin (real strategy on the TauCross snapshot).
 */
@Tag("placement")
public class ExactVerdictTest extends WorldStubForTests {

    @AfterEach
    public void clean() {
        ReservedSlots.clearForTests();
        RefusedTiles.clearForTests();
    }

    private CataloguePlacementPlanner planner(SnapshotTerrain terrain) {
        TileAvailabilityGrid grid = new TileAvailabilityGrid(terrain);
        // No blocks (documented test seam): block templates stamp their
        // tiles used on the planner's grid, which is a grid/catalogue
        // parity question of its own - these pins target the verdict,
        // not the stamping.
        CataloguePlacementPlanner planner = new CataloguePlacementPlanner(
            grid,
            new atlantis.placement.race.ProtossPlacementStrategy(
                new atlantis.placement.core.PsiGating(
                    new atlantis.placement.engine.EnginePowerSource())),
            null, false);
        planner.startPass();
        return planner;
    }

    @Test
    public void refusedExactTileFails() {
        SnapshotTerrain terrain = new SnapshotTerrain(SnapshotTerrain.tauCross());
        APosition main = terrain.baseTile(0);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, main.tx(), main.ty()),
            fake(AUnitType.Protoss_Pylon, main.tx() + 5, main.ty()),
            fake(AUnitType.Protoss_Probe, main.tx() + 2, main.ty())
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            APosition tile = APosition.create(main.tx() + 6, main.ty() + 1);
            RefusedTiles.note(tile, AUnitType.Protoss_Gateway);

            PlacementReservation result = planner(terrain).reservePlacement(
                UnitProducible.of(AUnitType.Protoss_Gateway),
                TargetPlacement.exactTile(tile.tx(), tile.ty()), 100);

            assertFalse(result.isSuccessful(),
                "an engine-refused exact tile must fail, never mint");
        });
    }

    @Test
    public void unpoweredExactGatewayFails() {
        SnapshotTerrain terrain = new SnapshotTerrain(SnapshotTerrain.tauCross());
        APosition main = terrain.baseTile(0);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, main.tx(), main.ty()),
            fake(AUnitType.Protoss_Probe, main.tx() + 2, main.ty())
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            APosition tile = APosition.create(main.tx() + 8, main.ty() + 2);
            PlacementReservation result = planner(terrain).reservePlacement(
                UnitProducible.of(AUnitType.Protoss_Gateway),
                TargetPlacement.exactTile(tile.tx(), tile.ty()), 100);

            assertFalse(result.isSuccessful(),
                "no Pylon anywhere: an unpowered exact Gateway is refused, not served");
        });
    }

    @Test
    public void poweredExactGatewayServes() {
        SnapshotTerrain terrain = new SnapshotTerrain(SnapshotTerrain.tauCross());
        APosition main = terrain.baseTile(0);

        TileAvailabilityGrid planGrid = new TileAvailabilityGrid(terrain);
        java.util.List<ReservedSlots.Slot> layout = ReservedSlots.planOn(
            new BuildLocationCatalogue(planGrid), main,
            AUnitType.Protoss_Gateway, 10);
        ReservedSlots.Slot g2 = null;
        for (ReservedSlots.Slot slot : layout) {
            if (slot.label.equals("G2")) g2 = slot;
        }
        assertNotNull(g2, "the layout owes G2");
        final APosition g2tile = g2.tile;

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, main.tx(), main.ty()),
            fake(AUnitType.Protoss_Pylon, g2tile.tx() + 5, g2tile.ty()),
            fake(AUnitType.Protoss_Probe, main.tx() + 2, main.ty())
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            PlacementReservation result = planner(terrain).reservePlacement(
                UnitProducible.of(AUnitType.Protoss_Gateway),
                TargetPlacement.exactTile(g2tile.tx(), g2tile.ty()), 100);

            assertTrue(result.isSuccessful(),
                "a powered legal exact tile still serves: " + g2tile);
        });
    }

    @Test
    public void coveredExactPylonServesBuilderDecides() {
        SnapshotTerrain terrain = new SnapshotTerrain(SnapshotTerrain.tauCross());
        APosition main = terrain.baseTile(0);

        TileAvailabilityGrid planGrid = new TileAvailabilityGrid(terrain);
        java.util.List<ReservedSlots.Slot> layout = ReservedSlots.planOn(
            new BuildLocationCatalogue(planGrid), main,
            AUnitType.Protoss_Gateway, 10);
        ReservedSlots.Slot g2 = null;
        for (ReservedSlots.Slot slot : layout) {
            if (slot.label.equals("G2")) g2 = slot;
        }
        assertNotNull(g2, "the layout owes G2");
        final APosition g2tile = g2.tile;

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, main.tx(), main.ty());
        FakeUnit w1 = fake(AUnitType.Protoss_Probe, g2tile.tx(), g2tile.ty() + 2);
        FakeUnit w2 = fake(AUnitType.Protoss_Probe, g2tile.tx() + 1, g2tile.ty() + 2);
        FakeUnit w3 = fake(AUnitType.Protoss_Probe, g2tile.tx() + 2, g2tile.ty() + 2);
        FakeUnit w4 = fake(AUnitType.Protoss_Probe, g2tile.tx() + 2, g2tile.ty() + 1);

        world(5, fakeOurs(nexus, w1, w2, w3, w4), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            assertTrue(atlantis.production.constructions.position.conditions.can_build_here.IsWorkerCovered.check(
                APosition.create(g2tile.tx(), g2tile.ty())),
                "setup: four miners cover the tile");

            PlacementReservation result = planner(terrain).reservePlacement(
                UnitProducible.of(AUnitType.Protoss_Pylon),
                TargetPlacement.exactTile(g2tile.tx(), g2tile.ty()), 100);

            assertTrue(result.isSuccessful(),
                "cover is a builder question, answered at the mint door - "
                    + "the site itself still serves: " + g2tile);
        });
    }
}
