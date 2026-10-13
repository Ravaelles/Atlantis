package tests.acceptance;

import atlantis.map.position.APosition;
import atlantis.units.BuildingTilesAreOccupied;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The reported bug: "Next PYLON and Next GATEWAY return a position that lands
 * exactly on the existing buildings", so nothing gets built.
 *
 * <p>
 * The finder's last word on "can I build here" is {@code MapTiles.canBuildHere},
 * which falls back to the map data (JBWEB) when the engine's own answer is
 * unreliable on OpenBW. Two things were wrong with that fallback:
 * </p>
 * <ul>
 *   <li>JBWEB's {@code usedGrid} was frozen at game start, because Atlantis never
 *       called {@code JBWEB.onUnitDiscover}/{@code onUnitDestroy} - so an occupied
 *       tile was reported free and new buildings were placed on top of existing
 *       ones;</li>
 *   <li>nothing else double-checked the tile, so there was no guard against it.</li>
 * </ul>
 *
 * <p>
 * Both are fixed (the lifecycle wiring in {@code Atlantis}, and the occupancy check
 * in {@code MapTiles.canBuildHere} / {@code DefineExactPositionForNewConstruction}).
 * This test pins the occupancy answer itself, which is the piece that made
 * "occupied" detectable at all.
 * </p>
 */
public class OccupiedTileIsNeverBuildableTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void anEmptyAreaIsNotOccupied() {
        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            assertFalse(
                BuildingTilesAreOccupied.check(APosition.create(40, 40), AUnitType.Protoss_Pylon),
                "an empty area must not read as occupied, or the guard would empty the map");
        });
    }

    @Test
    public void aBuildingDirectlyOnTheTileOccupiesIt() {
        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Gateway, 26, 23), // occupies [26..28]x[23..25]
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            assertTrue(
                BuildingTilesAreOccupied.check(APosition.create(26, 23), AUnitType.Protoss_Pylon),
                "a tile an existing building stands on is occupied - this is the report");
        });
    }

    @Test
    public void anOverlapOnASingleTileCounts() {
        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Gateway, 26, 23), // [26..28]x[23..25]
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            // A Gateway placed at [25,22] covers [25..27]x[22..24] and shares only
            // the [26,23] tile with the existing Gateway there.
            assertTrue(
                BuildingTilesAreOccupied.check(APosition.create(25, 22), AUnitType.Protoss_Gateway),
                "an overlap on a single tile is still an overlap");
        });
    }

    @Test
    public void aBuildingCloseButNotOverlappingDoesNotOccupy() {
        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Gateway, 26, 23), // [26..28]x[23..25]
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            // [30,23] covers [30..32]x[23..25]: adjacent, no shared tile.
            assertFalse(
                BuildingTilesAreOccupied.check(APosition.create(30, 23), AUnitType.Protoss_Gateway),
                "adjacency is not occupancy - buildings are routinely 2-3 tiles apart");
        });
    }

    @Test
    public void aUnitStandingOnTheTileOccupiesIt() {
        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 26, 23) // a worker parked where we would build
        ), fakeEnemies(), () -> {
            // Owner ruling #49: with no builder assigned yet, nothing may be
            // on the footprint at all - so a lone worker DOES occupy here.
            // (The old assertion, worker-never-counts, predates the ruling;
            // the name of this test finally matches what it pins.)
            assertTrue(
                BuildingTilesAreOccupied.check(APosition.create(26, 23), AUnitType.Protoss_Pylon),
                "with no builder to exclude, a worker on the tile occupies it");
        });
    }

    @Test
    public void theAssignedBuilderDoesNotOccupyItsOwnSite() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 26, 23);
        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            builder
        ), fakeEnemies(), () -> {
            // The other half of ruling #49: StarCraft lets a building go up
            // under its builder, so the assigned builder is excluded - and the
            // builder stepping aside is what unblocks mineral-line sites.
            assertFalse(
                BuildingTilesAreOccupied.check(APosition.create(26, 23), AUnitType.Protoss_Pylon, builder),
                "the assigned builder is not an obstacle to its own site");
        });
    }
}
