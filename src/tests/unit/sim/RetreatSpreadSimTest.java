package tests.unit.sim;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.BattleCommander;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;
import tests.unit.AbstractTestWithUnits;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Retreat pile-up (Tomas Cere game, 52 Dragoons lost): on FLEE every unit
 * receives the SAME rally point ({@code gatherPointFor} returns the
 * anchor for all), so the whole cluster converges onto one pixel, wedges
 * body-on-body, and dies standing with retreat orders active (zero
 * displacement over 100 frames while hp falls).
 *
 * <p>Repro: four Dragoons flee toward the home rally. Pre-fix they all
 * stack on the rally pixel; the fix spreads each unit onto its own
 * standable point around it (a rally scatter, never an arc facing the
 * pursuer - FLEE still never forms up).</p>
 */
@Tag("combat")
public class RetreatSpreadSimTest extends AbstractTestWithUnits {

    private static final int TILE = 32;

    /**
     * Wall block biting into the rally ring (tiles).
     */
    private static final int WALL_X = 11;
    private static final int WALL_Y = 21;
    private static final int WALL_W = 2;
    private static final int WALL_H = 2;

    private static boolean insideWall(APosition p) {
        return p.tx() >= WALL_X && p.tx() < WALL_X + WALL_W
            && p.ty() >= WALL_Y && p.ty() < WALL_Y + WALL_H;
    }

    @Test
    public void fleeingUnitsSpreadAroundRallyInsteadOfStacking() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 20);
        FakeUnit[] dragoons = new FakeUnit[]{
            fake(AUnitType.Protoss_Dragoon, 17, 19),
            fake(AUnitType.Protoss_Dragoon, 18, 20),
            fake(AUnitType.Protoss_Dragoon, 17, 21),
            fake(AUnitType.Protoss_Dragoon, 18, 21),
        };
        for (FakeUnit d : dragoons) {
            d.hp = 160;
            d.shields = 80;
            d.cooldown = 0;
            Alpha.get().addUnit(d);
        }
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 45, 20);
        foe.setEnemy();
        foe.hp = 100;

        FakeUnit[] ours = new FakeUnit[]{
            nexus, dragoons[0], dragoons[1], dragoons[2], dragoons[3]
        };
        SimArena arena = new SimArena(70 * TILE, 40 * TILE)
            .block(WALL_X * TILE, WALL_Y * TILE, WALL_W * TILE, WALL_H * TILE);
        SimWorld world = new SimWorld(
            ours, new FakeUnit[]{foe}, arena);
        try {
            BattleCommander.clearForTests();
            EventLog.clearForTests();

            APosition anchor = APosition.create(10, 20);
            double[] startX = new double[dragoons.length];
            double[] startY = new double[dragoons.length];
            for (int i = 0; i < dragoons.length; i++) {
                startX[i] = dragoons[i].position.x();
                startY[i] = dragoons[i].position.y();
            }

            for (int i = 0; i < 250; i++) {
                world.tick(() -> {
                    for (FakeUnit d : dragoons) {
                        if (d.hp <= 0) continue;
                        BattleCommander.act(d, FightOrFlight.Tactic.FLEE, anchor, dragoons[0]);
                    }
                });
            }

            Set<String> tiles = new HashSet<>();
            int displaced = 0;
            for (int i = 0; i < dragoons.length; i++) {
                FakeUnit d = dragoons[i];
                APosition pos = APosition.create(d.position.x(), d.position.y());
                assertFalse(insideWall(pos), "rally scatter never stands inside the wall: " + pos);
                tiles.add(d.tx() + "," + d.ty());
                double moved = Math.hypot(
                    d.position.x() - startX[i], d.position.y() - startY[i]) / TILE;
                if (moved >= 2.0) displaced++;
            }

            assertEquals(displaced, dragoons.length, "every retreating unit displaces toward the rally: "
                + displaced + " of " + dragoons.length);
            assertTrue(tiles.size() >= 3,
                "retreat destinations spread instead of stacking on one pixel: "
                    + tiles);
        } finally {
            for (FakeUnit d : dragoons) Alpha.get().removeUnit(d);
            world.close();
            BattleCommander.clearForTests();
            EventLog.clearForTests();
        }
    }
}
