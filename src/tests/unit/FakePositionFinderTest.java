package tests.unit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import atlantis.map.MapTiles;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeMapTiles;
import tests.unit.sim.SimArena;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The stub position-finder answers like the engine source (owner
 * 2026-10-24): with walkable-everywhere stubs the origin passes the
 * first check (bit-identical to the old identity answer), and with an
 * arena-backed MapTiles source it really spirals. The identity
 * shortcut blinded every sim that needed a slide.
 */
@Tag("map")
public class FakePositionFinderTest extends WorldStubForTests {

    private void useArenaWalkability(final SimArena arena) {
        MapTiles.useSource(new MapTiles.Source() {
            @Override
            public boolean isWalkable(HasPosition at) {
                APosition p = at.position();

                return arena.isWalkablePx(p.x(), p.y());
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
                return true;
            }

            @Override
            public boolean hasPathBetween(HasPosition from, HasPosition to) {
                return true;
            }

            @Override
            public boolean canBuildHere(AUnit builder, AUnitType building, APosition at) {
                return true;
            }
        });
    }

    @Test
    public void walkableOriginPassesThrough() {
        APosition open = APosition.create(30, 20);

        assertEquals(open, open.makeWalkable(6, 3, null),
            "walkable origin: identical to the old identity answer");
    }

    @Test
    public void walledOriginSlidesOut() {
        SimArena arena = new SimArena(70 * 32, 40 * 32).block(38 * 32, 0, 2 * 32, 40 * 32);
        try {
            useArenaWalkability(arena);

            APosition walled = APosition.create(39, 20);
            APosition slid = walled.makeWalkable(6, 1, null);

            assertNotNull(slid, "a slide exists next to the wall");
            assertTrue(arena.isWalkablePx(slid.x(), slid.y()),
                "the slide lands on walkable ground: " + slid.tx() + "," + slid.ty());
        }
        finally {
            FakeMapTiles.installAsSource();
        }
    }

    @Test
    public void sealedPocketDeclines() {
        SimArena arena = new SimArena(70 * 32, 40 * 32);
        // Wall the origin in on all sides within radius 2.
        arena.block(0, 0, 70 * 32, 40 * 32);
        try {
            useArenaWalkability(arena);

            // Punch one hole far outside the search radius.
            assertNull(APosition.create(35, 20).makeWalkable(2, 1, null),
                "no walkable tile in radius: null instead of an unwalkable origin");
        }
        finally {
            FakeMapTiles.installAsSource();
        }
    }
}
