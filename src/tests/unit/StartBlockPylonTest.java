package tests.unit;

import atlantis.map.position.APosition;
import atlantis.map.snapshot.MapSnapshot;
import atlantis.placement.core.PsiGating;
import atlantis.placement.core.TileAvailabilityGrid;
import atlantis.placement.engine.CataloguePlacementPlanner;
import atlantis.placement.engine.EnginePowerSource;
import atlantis.placement.race.ProtossPlacementStrategy;
import atlantis.production.placement.PlacementReservation;
import atlantis.production.placement.TargetPlacement;
import atlantis.production.producibles.UnitProducible;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;
import tests.fakes.SnapshotTerrain;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * V3 S3: the first Pylon takes the start block's power slot (Stardust
 * doctrine - the first pylon is PART of the layout, not the nearest
 * free tile to the base). Pinned on the TauCross snapshot, per base:
 * the slot the block names can sit several tiles out (measured base 0:
 * 7.81), so the bound is 10, frozen here - not "near", not hope.
 * A second Pylon in the same pass ranks normally (the slot is
 * consumed, never double-booked).
 */
@Tag("placement")
public class StartBlockPylonTest extends WorldStubForTests {

    @Override
    public bwapi.Race initRace() {
        return bwapi.Race.Protoss;
    }

    private static CataloguePlacementPlanner planner(SnapshotTerrain terrain) {
        return new CataloguePlacementPlanner(
            new TileAvailabilityGrid(terrain),
            new ProtossPlacementStrategy(new PsiGating(new EnginePowerSource())),
            null, true,
            new CataloguePlacementPlanner.GameContext() {
                @Override
                public boolean inGame() {
                    return true;
                }
            });
    }

    private static void stubMap128() {
        bwapi.Game game = atlantis.Atlantis.game();
        org.mockito.Mockito.when(game.mapWidth()).thenReturn(128);
        org.mockito.Mockito.when(game.mapHeight()).thenReturn(128);
    }

    private void firstPylonNearBase(int baseIndex) {
        MapSnapshot snap = SnapshotTerrain.tauCross();
        APosition main = new SnapshotTerrain(snap).baseTile(baseIndex);
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, main.tx(), main.ty());
        FakeUnit probe = fake(AUnitType.Protoss_Probe, main.tx() + 2, main.ty());

        world(5, fakeOurs(nexus, probe), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            stubMap128();

            CataloguePlacementPlanner planner = planner(new SnapshotTerrain(snap));
            planner.startPass();
            PlacementReservation first = planner.reservePlacement(
                UnitProducible.of(AUnitType.Protoss_Pylon),
                TargetPlacement.inNeighbourhood(main.tx(), main.ty()), 100);

            assertTrue(first.isSuccessful(), "a first-Pylon slot exists near base " + baseIndex);
            double dist = Math.hypot(first.tileX() - main.tx(), first.tileY() - main.ty());
            assertTrue(dist <= 10,
                "the first Pylon sits in the start block, within 10 of the Nexus "
                    + "(base " + baseIndex + " measured " + String.format("%.2f", dist) + ")");
        });
    }

    @Test
    public void firstPylonNearBase0() {
        firstPylonNearBase(0);
    }

    @Test
    public void firstPylonNearCornerBase1() {
        firstPylonNearBase(1);
    }

    @Test
    public void firstPylonNearBase2() {
        firstPylonNearBase(2);
    }

    @Test
    public void secondPylonRanksNormally() {
        MapSnapshot snap = SnapshotTerrain.tauCross();
        APosition main = new SnapshotTerrain(snap).baseTile(0);
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, main.tx(), main.ty());
        FakeUnit probe = fake(AUnitType.Protoss_Probe, main.tx() + 2, main.ty());

        world(5, fakeOurs(nexus, probe), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            stubMap128();

            CataloguePlacementPlanner planner = planner(new SnapshotTerrain(snap));
            planner.startPass();
            PlacementReservation first = planner.reservePlacement(
                UnitProducible.of(AUnitType.Protoss_Pylon),
                TargetPlacement.inNeighbourhood(main.tx(), main.ty()), 100);
            PlacementReservation second = planner.reservePlacement(
                UnitProducible.of(AUnitType.Protoss_Pylon),
                TargetPlacement.inNeighbourhood(main.tx(), main.ty()), 100);

            assertTrue(first.isSuccessful() && second.isSuccessful(), "two Pylons serve in one pass");
            assertNotEquals(first.tileX() + ":" + first.tileY(),
                second.tileX() + ":" + second.tileY(),
                "the block slot is consumed once - the second Pylon ranks for its own tile");
        });
    }
}
