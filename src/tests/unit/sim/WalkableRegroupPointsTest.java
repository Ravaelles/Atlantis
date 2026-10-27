package tests.unit.sim;

import atlantis.combat.strategy.ChokeCollapse;
import atlantis.combat.strategy.ChokeStandoff;
import atlantis.combat.strategy.SquadScout;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeChoke;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regroup points must be marchable (NEXT #199): the regroup-point
 * producers answer pure geometry off the choke center, and nothing
 * stops that point from landing inside a wall - Regroup.march then
 * feeds it to move(), which refuses and logs an error-with-stacktrace
 * every frame (log spam, no movement). Walkable-or-null is the
 * contract; the caller already handles null.
 */
@Tag("combat")
public class WalkableRegroupPointsTest extends WorldStubForTests {

    private static final int TILE = 32;

    @Test
    public void regroupPointsAreNeverInsideWalls() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 20, 30);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 20, 32);
        zealot.setEnemy();

        SimArena arena = new SimArena(70 * TILE, 40 * TILE);
        // Wall band the raw points land in: bait (20,18) and hold (20,26).
        arena.block(19 * TILE, 17 * TILE, 7 * TILE, 3 * TILE);
        arena.block(19 * TILE, 24 * TILE, 3 * TILE, 5 * TILE);
        useArenaWalkability(arena);

        FakeChoke choke = new FakeChoke(APosition.create(20, 16), 8);

        world(5, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            try (MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class)) {
                chokes.when(() -> Chokes.nearestChoke(
                    Mockito.any(), Mockito.eq("ALL")
                )).thenReturn(choke);

                APosition hold = ChokeStandoff.holdPointFor(dragoon);
                assertTrue(hold == null || hold.isWalkable(),
                    "hold point must be marchable, got: " + hold);

                APosition bait = SquadScout.baitPointFor(dragoon);
                assertTrue(bait == null || bait.isWalkable(),
                    "bait point must be marchable, got: " + bait);

                APosition collapse = ChokeCollapse.collapsePointFor(dragoon);
                assertTrue(collapse == null || collapse.isWalkable(),
                    "collapse point must be marchable, got: " + collapse);
            }
        });
    }

    private void useArenaWalkability(final SimArena arena) {
        atlantis.map.MapTiles.useSource(new atlantis.map.MapTiles.Source() {
            @Override
            public boolean isWalkable(atlantis.map.position.HasPosition at) {
                APosition p = at.position();

                return arena.isWalkablePx(p.x(), p.y());
            }

            @Override
            public boolean isExplored(atlantis.map.position.HasPosition at) {
                return true;
            }

            @Override
            public boolean isVisible(atlantis.map.position.HasPosition at) {
                return true;
            }

            @Override
            public boolean hasPathBetween(atlantis.map.position.HasPosition from, atlantis.map.position.HasPosition to) {
                return true;
            }

            @Override
            public boolean isBuildable(atlantis.map.position.HasPosition at, boolean alsoCheckBuildings) {
                return arena.isWalkablePx(at.position().x(), at.position().y());
            }

            @Override
            public boolean canBuildHere(atlantis.units.AUnit builder, atlantis.units.AUnitType building, APosition at) {
                return arena.isWalkablePx(at.position().x(), at.position().y());
            }
        });
    }
}
