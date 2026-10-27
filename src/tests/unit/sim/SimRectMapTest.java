package tests.unit.sim;

import atlantis.combat.BattleCommander;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.map.region.ARegion;
import atlantis.map.region.Regions;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;
import tests.unit.AbstractTestWithUnits;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sim rect-region map (NEXT #191, owner sketch): four quadrants
 * separated by blocked bands with choke gaps, instead of a walkable
 * void. Pins the mapping (corners land in their quadrants, neighbours
 * link through the gaps) and the sweep march (no memory anywhere: the
 * line walks at region centers instead of standing).
 */
@Tag("combat")
public class SimRectMapTest extends AbstractTestWithUnits {

    private static final int TILE = 32;

    private static APosition mapCenterForTest() {
        ARegion nearest = null;
        double best = Double.MAX_VALUE;
        for (ARegion r : Regions.regions()) {
            if (r == null || r.center() == null) continue;
            try {
                double d = APosition.create(30, 25).distTo(r.center());
                if (d < best) {
                    best = d;
                    nearest = r;
                }
            } catch (Exception ignored) {
            }
        }
        assertNotNull(nearest, "the rect map must answer regions in the sim");

        return nearest.center();
    }

    private static String nameOf(ARegion region) {
        assertNotNull(region, "every tile answers a region");
        assertInstanceOf(SimRectRegion.class, region, "the rect map answers: " + region.getClass());

        return ((SimRectRegion) region).name();
    }

    @Test
    public void quadrantsMapAndLink() {
        SimArena arena = new SimArena(70 * TILE, 40 * TILE);
        SimRectMap map = SimRectMap.install(arena, 70, 40);
        try {
            assertEquals("enemy", nameOf(Regions.getRegion(APosition.create(5, 5))));
            assertEquals("main", nameOf(Regions.getRegion(APosition.create(60, 5))));
            assertEquals("middle", nameOf(Regions.getRegion(APosition.create(5, 35))));
            assertEquals("natural", nameOf(Regions.getRegion(APosition.create(60, 35))));

            assertTrue(map.main.getReachableRegions().contains(map.enemy), "north choke links");
            assertTrue(map.main.getReachableRegions().contains(map.natural), "east choke links");
            assertTrue(map.enemy.getReachableRegions().contains(map.middle), "west choke links");
            assertTrue(map.middle.getReachableRegions().contains(map.natural), "south choke links");
            assertFalse(map.main.getReachableRegions().contains(map.middle), "no diagonal shortcut");
        } finally {
            SimRectMap.uninstall();
            tests.fakes.FakeMapTiles.installAsSource();
        }
    }

    @Test
    public void sweepMarchesAtRegionCentersWithNoMemory() {
        aGame.when(AGame::isUms).thenReturn(true);

        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 25);
        dragoon.cooldown = 0;

        SimArena arena = new SimArena(70 * TILE, 40 * TILE);
        useArenaWalkability(arena);
        SimRectMap.install(arena, 70, 40);

        SimWorld world = new SimWorld(new FakeUnit[]{dragoon}, new FakeUnit[0], arena);
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();

            double startDist = dragoon.distTo(mapCenterForTest());
            for (int i = 0; i < 200; i++) {
                world.tick(() -> {
                    if (dragoon.hp > 0) {
                        BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
                    }
                });
                if (i == 199) {
                    double endDist = dragoon.distTo(mapCenterForTest());
                    assertTrue(endDist < startDist - 2,
                        "with no memory anywhere the line walks at the regions instead of standing: "
                            + startDist + " -> " + endDist);
                }
            }
        } finally {
            SimRectMap.uninstall();
            tests.fakes.FakeMapTiles.installAsSource();
            world.close();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
        }
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
                return true;
            }

            @Override
            public boolean canBuildHere(atlantis.units.AUnit builder, atlantis.units.AUnitType building, APosition at) {
                return true;
            }
        });
    }
}
