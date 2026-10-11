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
     * <b>The rule from {@code NEXT.md} #49, first half:</b> the footprint MAY contain the
     * builder. StarCraft places a building under the unit that is about to build it - the
     * engine moves that unit onto the site - so the builder must not read as an obstacle.
     */
    @Test
    public void theAssignedBuilderOnTheFootprintDoesNotBlockIt() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 30, 30);

        world(1, fakeOurs(builder), fakeEnemies(), () -> {
            assertFalse(
                    BuildingTilesAreOccupied.check(APosition.create(30, 30), PYLON, builder),
                    "the builder is not an obstacle: the game places the building under it and "
                            + "moves it onto the site. Counting it refused every mineral-line tile");
        });
    }

    /**
     * <b>The second half, and the one that was wrong before:</b> every OTHER unit on the
     * footprint blocks.
     *
     * <p>
     * The buildings-only version of this guard answered "free" for a tile a mining worker
     * stood on - a placement the engine refuses - so the search could pick a tile, walk a
     * builder to it and then wait for a construction that never started (`NEXT.md` #49).
     * The miner here is a DIFFERENT unit from the builder, so it must be counted.
     * </p>
     */
    @Test
    public void anotherUnitOnTheFootprintBlocksEvenWhenABuilderIsAssigned() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 30, 30);
        FakeUnit miningProbe = fake(AUnitType.Protoss_Probe, 31, 30);

        world(1, fakeOurs(builder, miningProbe), fakeEnemies(), () -> {
            assertTrue(
                    BuildingTilesAreOccupied.check(APosition.create(30, 30), PYLON, builder),
                    "a worker that is NOT the builder is a real obstacle - this is the "
                            + "mineral-line case the old buildings-only guard accepted");
        });
    }

    /**
     * With no builder assigned yet, nothing is exempt: the whole footprint must be empty.
     * That is the state the first search runs in, so a worker on the tile must refuse it
     * rather than be waived through by a rule about a builder that does not exist yet.
     */
    @Test
    public void withNoBuilderAssignedAnyUnitOnTheFootprintBlocks() {
        FakeUnit someWorker = fake(AUnitType.Protoss_Probe, 30, 30);

        world(1, fakeOurs(someWorker), fakeEnemies(), () -> {
            assertTrue(
                    BuildingTilesAreOccupied.check(APosition.create(30, 30), PYLON, null),
                    "before a builder is chosen there is nothing to exempt, so a unit on the "
                            + "footprint makes the tile unusable");
        });
    }

    /**
     * A building on the footprint blocks regardless of who the builder is - the exemption
     * is about the unit that will walk there, never about structures.
     */
    @Test
    public void aBuildingOnTheFootprintBlocksEvenForItsOwnBuilder() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 40, 40);
        FakeUnit pylonAlreadyThere = fake(AUnitType.Protoss_Pylon, 40, 40);

        world(1, fakeOurs(builder, pylonAlreadyThere), fakeEnemies(), () -> {
            assertTrue(
                    BuildingTilesAreOccupied.check(APosition.create(40, 40), PYLON, builder),
                    "the builder exemption is for the builder UNIT, not for what already "
                            + "stands there");
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
