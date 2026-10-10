package tests.acceptance;

import atlantis.map.MapTiles;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeMapTiles;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Placement on OpenBW, where JBWEB is not available at all.
 *
 * <p>
 * JBWEB is a JNI library whose natives do not exist on Linux; {@code InitJBWEB}
 * fails and {@code AMap} catches it and continues
 * ({@code _AI/LOCAL-STARCRAFT.md} 187-189). That left
 * {@code MapTiles.canBuildHere} with only the engine's own composite answer -
 * which {@code MapTiles} documents as refusing valid tiles on OpenBW - plus the
 * occupancy guard, which can only ever say "not occupied". So a free, valid
 * tile
 * had no way to be accepted, and the bot could not place its first Pylon:
 *
 * <pre>
 * 0:39: Can't find place for `Pylon` ... (reason: Can't physically build here)
 * </pre>
 *
 * <p>
 * The fix answers from the <b>engine at tile level</b> when JBWEB is missing:
 * every tile the building covers must be walkable and buildable. These tests
 * pin
 * that path, which is the one OpenBW uses.
 * </p>
 */
public class OpenBWPlacementWithoutJbwebTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    /**
     * The engine source (so the map-data fallback applies) that refuses the
     * composite query - the OpenBW symptom - and reports tile qualities from this
     * class's flags instead.
     */
    private static class EngineSourceRefusingComposite implements MapTiles.Source {
        final boolean walkable;
        final boolean buildableTile;

        EngineSourceRefusingComposite(boolean walkable, boolean buildableTile) {
            this.walkable = walkable;
            this.buildableTile = buildableTile;
        }

        @Override
        public boolean isWalkable(HasPosition at) {
            return walkable;
        }

        @Override
        public boolean isExplored(HasPosition at) {
            return true;
        }

        @Override
        public boolean isVisible(HasPosition at) {
            return true;
        }

        @Override
        public boolean isBuildable(HasPosition at, boolean alsoCheckBuildings) {
            if (!buildableTile) return false;

            // The engine contract: with alsoCheckBuildings, a tile a building stands
            // on is not buildable. Without it, only the terrain is answered. Modelling
            // that is what makes the occupied case testable on this path.
            if (alsoCheckBuildings
                && atlantis.units.BuildingTilesAreOccupied.check(at.position(), AUnitType.Protoss_Pylon)) {
                return false;
            }

            return true;
        }

        @Override
        public boolean hasPathBetween(HasPosition from, HasPosition to) {
            return true;
        }

        /** The composite engine answer: unreliable on OpenBW, so it refuses. */
        @Override
        public boolean canBuildHere(AUnit builder, AUnitType building, APosition at) {
            return false;
        }

        @Override
        public boolean spawnsFromMapData() {
            return true;
        }
    }

    @AfterEach
    public void restoreHarnessTiles() {
        FakeMapTiles.installAsSource();
    }

    @Test
    public void aPylonCanBePlacedOnOpenBWWhenEveryTileIsWalkableAndBuildable() {
        // walkable, buildable - the ordinary case that was refused on OpenBW.
        // (JBWEB is not initialized in the stub world, which is exactly the OpenBW
        // condition this path exists for.)
        MapTiles.useSource(new EngineSourceRefusingComposite(true, true));

        world(1, fakeOurs(
                fake(AUnitType.Protoss_Nexus, 20, 20),
                fake(AUnitType.Protoss_Probe, 21, 21)), fakeEnemies(), () -> {
                    APosition free = APosition.create(26, 23);

                    assertTrue(MapTiles.canBuildHere(null, AUnitType.Protoss_Pylon, free),
                            "a free tile whose covered tiles are walkable and buildable must be placeable - "
                                    + "this is the first Pylon the bot could not place on OpenBW");
                });
    }

    @Test
    public void anUnwalkableTileIsNotPlaceable() {
        MapTiles.useSource(new EngineSourceRefusingComposite(false, true));

        world(1, fakeOurs(
                fake(AUnitType.Protoss_Nexus, 20, 20),
                fake(AUnitType.Protoss_Probe, 21, 21)), fakeEnemies(), () -> {
                    assertFalse(MapTiles.canBuildHere(null, AUnitType.Protoss_Pylon, APosition.create(26, 23)),
                            "a building cannot stand on unwalkable terrain");
                });
    }

    @Test
    public void anUnbuildableTileIsNotPlaceable() {
        MapTiles.useSource(new EngineSourceRefusingComposite(true, false));

        world(1, fakeOurs(
                fake(AUnitType.Protoss_Nexus, 20, 20),
                fake(AUnitType.Protoss_Probe, 21, 21)), fakeEnemies(), () -> {
                    assertFalse(MapTiles.canBuildHere(null, AUnitType.Protoss_Pylon, APosition.create(26, 23)),
                            "a non-buildable tile is not placeable");
                });
    }

    @Test
    public void anOccupiedTileIsStillRefusedOnTheOpenBWPath() {
        // The OpenBW tile-level fallback must not become a licence to build on top
        // of a building: the occupancy guard runs before it.
        MapTiles.useSource(new EngineSourceRefusingComposite(true, true));

        world(1, fakeOurs(
                fake(AUnitType.Protoss_Nexus, 20, 20),
                fake(AUnitType.Protoss_Gateway, 26, 23),
                fake(AUnitType.Protoss_Probe, 21, 21)), fakeEnemies(), () -> {
                    assertFalse(MapTiles.canBuildHere(null, AUnitType.Protoss_Pylon, APosition.create(26, 23)),
                            "an occupied tile must stay unbuildable even on the OpenBW path");
                });
    }
}
