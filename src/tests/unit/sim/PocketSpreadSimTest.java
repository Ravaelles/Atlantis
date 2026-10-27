package tests.unit.sim;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.BattleCommander;
import atlantis.combat.FormationHold;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;
import tests.unit.AbstractTestWithUnits;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NEXT #209: blocked units deep in our own base occupy the same ground
 * instead of spreading. Repro: a pylon wall segment sits on the crescent
 * the formation wants to stand on. The engine's walkability is
 * terrain-only, so without an occupancy check every slot reads standable
 * and the march wedges on the wall instead of spreading around it.
 *
 * <p>The pin is geometric and uses the test's own wall: no assigned slot
 * may sit inside the 2x2 tiles the test walled off, and assigned units
 * must actually displace (march, not freeze). Displacement doubles as
 * the #182 check: units that move never trip the wedge watch, so the
 * spacing answer and the unstick do not fight.</p>
 */
@Tag("combat")
public class PocketSpreadSimTest extends AbstractTestWithUnits {

    private static final int TILE = 32;

    /**
     * Test-geometry wall, co-located with the pylon below (tiles).
     */
    private static final int WALL_X = 19;
    private static final int WALL_Y = 20;
    private static final int WALL_W = 2;
    private static final int WALL_H = 2;

    private static boolean insideWall(APosition p) {
        return p.tx() >= WALL_X && p.tx() < WALL_X + WALL_W
            && p.ty() >= WALL_Y && p.ty() < WALL_Y + WALL_H;
    }

    @Test
    public void lineMarchesAroundBuildingsInsteadOfWedgingOnThem() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 20);
        FakeUnit[] dragoons = new FakeUnit[]{
            fake(AUnitType.Protoss_Dragoon, 18, 20),
            fake(AUnitType.Protoss_Dragoon, 19, 21),
            fake(AUnitType.Protoss_Dragoon, 19, 19),
        };
        for (FakeUnit d : dragoons) {
            d.hp = 100;
            d.shields = 80;
            d.cooldown = 0;
            Alpha.get().addUnit(d);
        }
        FakeUnit pylon = fake(AUnitType.Protoss_Pylon, WALL_X, WALL_Y);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 37, 20);
        foe.setEnemy();
        foe.hp = 100;

        FakeUnit[] ours = new FakeUnit[]{nexus, dragoons[0], dragoons[1], dragoons[2], pylon};
        SimArena arena = new SimArena(70 * TILE, 40 * TILE)
            .block(WALL_X * TILE, WALL_Y * TILE, WALL_W * TILE, WALL_H * TILE);
        SimWorld world = new SimWorld(
            ours, new FakeUnit[]{foe}, arena);
        try {
            BattleCommander.clearForTests();
            EventLog.clearForTests();

            APosition anchor = APosition.create(30, 20);
            for (int i = 0; i < 150; i++) {
                world.tick(() -> {
                    for (FakeUnit d : dragoons) {
                        if (d.hp <= 0) continue;
                        FormationHold.holdIfCalm(d, foe, FightOrFlight.Tactic.FIGHT, dragoons[0], anchor);
                    }
                });
            }

            List<String> trails = new ArrayList<>();
            int assigned = 0;
            int displaced = 0;
            for (FakeUnit d : dragoons) {
                HasPosition slot = atlantis.combat.cluster.ClusterRally.formationSlotFor(
                    d, dragoons[0], anchor, FightOrFlight.Tactic.FIGHT);
                trails.add("id=" + d.id() + " pos=" + d.position + " slot=" + slot);
                if (slot == null) continue;
                assigned++;
                assertFalse(insideWall((APosition) slot), "no assigned slot sits inside the walled tiles: " + slot);
                if (d.position.distTo(slot) <= 2.0) displaced++;
            }
            System.out.println("POCKET " + trails);

            assertTrue(assigned >= 2,
                "open ground around the wall still hosts the crescent: "
                    + assigned + " slots assigned");
            assertTrue(displaced >= 2,
                "assigned units march at standable slots instead of freezing "
                    + "on the wall: " + displaced + " of " + assigned + " placed");
            assertTrue(EventLog.toJsonLines().contains("\"verdict\":\"HOLD\""),
                "the line marches in formation, it does not fall through");
        } finally {
            for (FakeUnit d : dragoons) Alpha.get().removeUnit(d);
            world.close();
            BattleCommander.clearForTests();
            EventLog.clearForTests();
        }
    }
}
