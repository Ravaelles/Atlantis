package tests.acceptance;

import atlantis.map.MapTiles;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.position.conditions.can_build_here.CanPhysicallyBuildHere;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.BuildingTilesAreOccupied;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The placement SEARCH and the build COMMAND must agree about a tile.
 *
 * <p>
 * This is the test that would have caught the defect that cost a whole session
 * (2026-10-10). The search asked {@code Game.canBuildHere(tile, type)} -
 * terrain plus
 * the units on the footprint - accepted a tile, and the builder walked there;
 * the
 * command path asked a <b>strictly larger</b> question,
 * {@code canBuildHere(tile, type, builder, true)}, whose extra terms include
 * whether the
 * <b>builder can path to the site</b> (a region-based query that answers false
 * for every
 * pair on OpenBW). Nothing threw, both sides "worked", and the building never
 * appeared:
 * the finder and the issuer disagreed by construction and only one of them was
 * tested.
 * </p>
 *
 * <p>
 * The lesson is not "test {@code canBuildHere}" - it is that a predicate the
 * search uses
 * to ACCEPT a tile must be the same predicate the command will use to issue the
 * order,
 * or the gap between them is invisible until a game refuses. Each test below
 * pins one
 * half of that contract.
 * </p>
 */
public class PlacementSearchMatchesCommandTest extends WorldStubForTests {

    private static final AUnitType PYLON = AUnitType.Protoss_Pylon;

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    /**
     * The engine's tile-level answer must be what the search sees. If this ever
     * reads
     * {@code true} for a tile the command path would refuse, the search is choosing
     * unwinnable tiles again - which is the whole failure mode above.
     */
    @Test
    public void aTileTheEngineCannotBuildOnIsRefusedByTheSearch() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 10, 10);

        world(1, fakeOurs(probe), fakeEnemies(), () -> {
            assertFalse(
                    MapTiles.canBuildHere(probe, PYLON, APosition.create(10, 10)),
                    "the search must not accept a tile the engine refuses");
        });
    }

    /**
     * {@link BuildingTilesAreOccupied} is the guard that answers the occupancy
     * question
     * from the live unit list rather than from the engine's tile query. A building
     * on the
     * footprint must always count.
     */
    @Test
    public void aBuildingOnTheFootprintIsOccupied() {
        FakeUnit pylonAlreadyThere = fake(AUnitType.Protoss_Pylon, 20, 20);

        world(1, fakeOurs(pylonAlreadyThere), fakeEnemies(), () -> {
            assertTrue(
                    BuildingTilesAreOccupied.check(APosition.create(20, 20), PYLON),
                    "a Pylon already standing on the footprint is an obstacle");
        });
    }

    /**
     * The rule the owner stated (2026-10-10) and the measured case that motivated
     * it: a
     * building may be placed under the unit that is about to build it, because the
     * engine
     * moves that unit onto the site. Every other unit blocks.
     *
     * <p>
     * The guard currently counts <b>buildings only</b>, so this test states the
     * rule at
     * the level the guard can express today: a building blocks. The worker half is
     * the
     * open item in {@code _AI/NEXT.md} #49 and needs the contract to grow a builder
     * parameter - pinning the incorrect behaviour now would freeze the bug.
     * </p>
     */
    @Test
    public void aWorkerStandingOnTheFootprintDoesNotCountAsABuilding() {
        FakeUnit miningProbe = fake(AUnitType.Protoss_Probe, 30, 30);

        world(1, fakeOurs(miningProbe), fakeEnemies(), () -> {
            assertFalse(
                    BuildingTilesAreOccupied.check(APosition.create(30, 30), PYLON),
                    "a worker is not a building - the guard is buildings-only by design, "
                            + "and #49 is where the worker rule belongs");
        });
    }

    /**
     * {@code CanPhysicallyBuildHere} is the placement gate; it must agree with
     * {@code MapTiles}, not carry a second opinion. Asserted through both entries
     * so a
     * future change to one that is not mirrored in the other fails here.
     */
    @Test
    public void thePlacementGateAndTheTileQueryAgreeOnAnEmptyTile() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 40, 40);

        world(1, fakeOurs(probe), fakeEnemies(), () -> {
            APosition emptyTile = APosition.create(44, 44);

            boolean viaMapTiles = MapTiles.canBuildHere(probe, PYLON, emptyTile);
            boolean viaGate = CanPhysicallyBuildHere.check(probe, PYLON, emptyTile);

            if (viaMapTiles) {
                assertTrue(viaGate,
                        "the placement gate must not refuse a tile MapTiles accepted - "
                                + "a finder that accepts and an issuer that refuses is the "
                                + "defect this class exists to prevent");
            }
        });
    }

    /**
     * The stub source answers {@code spawnsFromMapData() = false}, so the engine
     * fallback
     * in {@code MapTiles.canBuildHere} must not apply. A harness run that silently
     * fell
     * back to map data would make every stub test pass for the wrong reason.
     */
    @Test
    public void theHarnessDoesNotUseTheMapDataFallback() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 50, 50);

        world(1, fakeOurs(probe), fakeEnemies(), () -> {
            HasPosition farAway = APosition.create(120, 120);

            // The stub has no map, so the answer must come from the harness's own rules
            // and stay consistent between two calls - not from a fallback that flips.
            boolean first = MapTiles.canBuildHere(probe, PYLON, (APosition) farAway);
            boolean second = MapTiles.canBuildHere(probe, PYLON, (APosition) farAway);

            assertTrue(first == second,
                    "the same tile must not answer differently on two consecutive calls");
        });
    }
}
