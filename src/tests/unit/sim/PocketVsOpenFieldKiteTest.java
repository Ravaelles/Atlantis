package tests.unit.sim;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.BattleCommander;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.map.MapTiles;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeMapTiles;
import tests.fakes.FakeUnit;
import tests.unit.AbstractTestWithUnits;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pocket-vs-logic isolation (NEXT #182, Dragoon#174 shape): one dragoon
 * under zealot contact, same REAL decision code ({@code BattleCommander.act}),
 * same threat - the only difference is geometry. Open field must kite
 * (stepped-aside rows + displacement); a walled pocket must stand and
 * trade (stuck-no-step rows, no displacement, shots still fired).
 *
 * <p>If the pocketed twin stands while shooting and the open twin kites,
 * #174's stand-and-trade is explained by the map, not by logic failing
 * to kite. If the pocketed twin kited too, the pocket would not explain
 * it and the logic would own the stand.</p>
 *
 * <p>Walkability vehicle: decision code reads tiles through the
 * {@code MapTiles} port, so the scenario installs an arena-backed source
 * for the run ({@code FakeMapTiles} answers walkable-everywhere) and
 * restores it in {@code finally}, the same discipline as
 * {@code AbstractTestWithUnits.setUp}.</p>
 */
@Tag("combat")
public class PocketVsOpenFieldKiteTest extends AbstractTestWithUnits {
    private static final int TILE = 32;
    private static final int TICKS = 600;

    private static FakeUnit mk(double tx, double ty) {
        FakeUnit d = fake(AUnitType.Protoss_Dragoon, tx, ty);
        d.hp = 180;
        d.shields = 80;
        d.cooldown = 0;

        return d;
    }

    private static FakeUnit foe(double tx, double ty) {
        FakeUnit z = fake(AUnitType.Protoss_Zealot, tx, ty);
        z.setEnemy();
        z.hp = 160;
        z.shields = 60;
        z.cooldown = 0;

        return z;
    }

    /**
     * Scripted presser: nearest live unit, move into range, then trade.
     */
    private static void scriptFoes(List<FakeUnit> foes, List<FakeUnit> ours) {
        for (FakeUnit f : foes) {
            if (f.hp <= 0) continue;
            FakeUnit n = null;
            double best = Double.MAX_VALUE;
            for (FakeUnit u : ours) {
                if (u.hp <= 0) continue;
                double d = f.distTo(u);
                if (d < best) {
                    best = d;
                    n = u;
                }
            }
            if (n == null) {
                f.lastCommand = "Stop";
                continue;
            }
            f.lastCommand = best <= SimWorld.reachTiles(f, n) ? "AttackUnit" : "Move";
            f.target = n;
            f.targetPosition = n.position;
        }
    }

    private static int ourShots(SimWorld world) {
        try {
            java.lang.reflect.Field f = SimWorld.class.getDeclaredField("ourShotsFired");
            f.setAccessible(true);

            return (Integer) f.get(world);
        } catch (Exception e) {
            return -1;
        }
    }

    @Test
    public void openFieldKitesAway() {
        Result open = runScenario(false);

        System.err.println("POCKET-ISOLATION open: " + open);

        assertTrue(open.steppedAside >= 5,
            "open field must step aside repeatedly, got " + open);
        assertEquals(0, open.stuckNoStep,
            "open field has nowhere to be stuck, got " + open);
        assertEquals(0, open.contactStuck,
            "open field validates a step even under contact, got " + open);
        assertTrue(open.displacementTiles >= 2.0,
            "open-field dragoon must displace (kite), got " + open);
        assertTrue(open.tookHits, "the presser must reach the dragoon, got " + open);
    }

    @Test
    public void walledPocketStandsAndTrades() {
        Result pocket = runScenario(true);

        System.err.println("POCKET-ISOLATION pocket: " + pocket);

        assertTrue(pocket.contactStuck >= 20,
            "pocketed dragoon must row stuck-no-step under contact, got " + pocket);
        assertTrue(pocket.contactStepped <= 30,
            "stuck must dominate validated steps in a walled pocket, got " + pocket);
        assertTrue(pocket.displacementTiles <= 1.5,
            "pocketed dragoon must hold position, got " + pocket);
        assertTrue(pocket.ourShots >= 1,
            "pocketed dragoon dies trading, not idle - it must fire, got " + pocket);
        assertTrue(pocket.tookHits, "the presser must reach the dragoon, got " + pocket);
    }

    private Result runScenario(boolean pocketed) {
        FakeUnit dragoon = mk(20, 20);
        FakeUnit foe = foe(26, 20);
        FakeUnit[] ours = new FakeUnit[]{dragoon};
        APosition start = dragoon.position;

        SimArena arena = new SimArena(70 * TILE, 40 * TILE);
        if (pocketed) {
            // U-pocket open toward the foe (+x), back wall THICK: the
            // straight-back aim lands inside the wall mass (not beyond
            // it in open space), the spiral shift only finds the unit's
            // own corridor cells, and the short-distance gate kills
            // those - so no aim validates and the twin rows
            // stuck-no-step, the #174 signature. A thin back wall
            // instead validates aims beyond it (reachable-looking but
            // sim-blocked): stepped-aside rows with zero displacement.
            arena.block(15 * TILE, 18 * TILE, 5 * TILE, 4 * TILE);
            arena.block(15 * TILE, 18 * TILE, 11 * TILE, TILE);
            arena.block(15 * TILE, 21 * TILE, 11 * TILE, TILE);
        }
        SimWorld world = new SimWorld(ours, new FakeUnit[]{foe}, arena);

        useArenaWalkability(arena);
        Result result = new Result();
        result.dragoonId = dragoon.id();
        try {
            BattleCommander.clearForTests();
            EventLog.clearForTests();
            Alpha.gather();

            List<FakeUnit> ourList = new ArrayList<FakeUnit>();
            ourList.add(dragoon);
            List<FakeUnit> foeList = new ArrayList<FakeUnit>();
            foeList.add(foe);

            for (int i = 0; i < TICKS; i++) {
                world.tick(() -> {
                    APosition anchor = APosition.create(60, 20);
                    for (FakeUnit u : ours) {
                        if (u.hp <= 0) continue;
                        BattleCommander.act(u, FightOrFlight.Tactic.FIGHT, anchor, dragoon);
                    }
                    scriptFoes(foeList, ourList);
                });
                if (dragoon.hp <= 0 && foe.hp <= 0) break;
            }

            result.displacementTiles = dragoon.position.distTo(start);
            result.ourShots = ourShots(world);
            result.tookHits = (dragoon.hp + dragoon.shields) < 260;
            countKiteRows(dragoon, result);
        } finally {
            for (FakeUnit u : ours) Alpha.get().removeUnit(u);
            world.close();
            BattleCommander.clearForTests();
            EventLog.clearForTests();
            FakeMapTiles.installAsSource();
        }

        return result;
    }

    private void countKiteRows(FakeUnit dragoon, Result result) {
        String all = EventLog.toJsonLines();
        String tag = "\"unit\":" + dragoon.id();
        for (String line : all.split("\n")) {
            if (!line.contains("\"key\":\"KITE\"") || !line.contains(tag)) continue;
            boolean stuck = line.contains("\"result\":\"stuck-no-step\"");
            boolean stepped = line.contains("\"result\":\"stepped-aside\"");
            if (!stuck && !stepped) continue;
            if (stuck) result.stuckNoStep++;
            else result.steppedAside++;
            // Contact phase only (dist param is tiles x10): the approach
            // kites in both geometries, the pocket-vs-logic question is
            // what happens once the foe is on top.
            int dist = distParam(line);
            if (dist >= 0 && dist <= 20) {
                if (stuck) result.contactStuck++;
                else result.contactStepped++;
            }
        }
    }

    private int distParam(String line) {
        int at = line.indexOf("\"dist\":");
        if (at < 0) return -1;

        int start = at + 7;
        int end = start;
        while (end < line.length() && Character.isDigit(line.charAt(end))) end++;
        if (end == start) return -1;

        try {
            return Integer.parseInt(line.substring(start, end));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Tiles answer from the scenario geometry, everything else stays stub.
     */
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

    private static class Result {
        int dragoonId;
        int steppedAside;
        int stuckNoStep;
        int contactStepped;
        int contactStuck;
        double displacementTiles;
        int ourShots;
        boolean tookHits;

        @Override
        public String toString() {
            return "id=" + dragoonId
                + " steppedAside=" + steppedAside + " stuckNoStep=" + stuckNoStep
                + " contactStepped=" + contactStepped + " contactStuck=" + contactStuck
                + " displacement=" + displacementTiles + " ourShots=" + ourShots
                + " tookHits=" + tookHits;
        }
    }
}
