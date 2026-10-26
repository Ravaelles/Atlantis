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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * March in formation, not single file (owner: the front fights alone
 * while the rear arrives 5 seconds later). A cluster marching at a
 * distant anchor with no enemy in reach takes crescent slots facing
 * the march direction; without slots every unit beelines the same
 * pixel and the march strings out into a line.
 */
@Tag("combat")
public class MarchFormationSimTest extends AbstractTestWithUnits {

    private static final int TILE = 32;

    @Test
    public void marchingClusterTakesFormationSlots() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 5, 20);
        FakeUnit[] dragoons = new FakeUnit[] {
            fake(AUnitType.Protoss_Dragoon, 10, 19),
            fake(AUnitType.Protoss_Dragoon, 10, 20),
            fake(AUnitType.Protoss_Dragoon, 10, 21),
            fake(AUnitType.Protoss_Dragoon, 11, 19),
            fake(AUnitType.Protoss_Dragoon, 11, 21),
        };
        for (FakeUnit d : dragoons) {
            d.hp = 160;
            d.shields = 80;
            d.cooldown = 0;
            Alpha.get().addUnit(d);
        }

        FakeUnit[] ours = new FakeUnit[] {
            nexus, dragoons[0], dragoons[1], dragoons[2], dragoons[3], dragoons[4]
        };
        SimArena arena = new SimArena(90 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(ours, new FakeUnit[] {}, arena);
        try {
            BattleCommander.clearForTests();
            EventLog.clearForTests();

            APosition anchor = APosition.create(60, 20);
            int slotted = 0;
            for (int i = 0; i < 60; i++) {
                world.tick(() -> {
                    for (FakeUnit d : dragoons) {
                        if (d.hp <= 0) continue;
                        BattleCommander.act(d, FightOrFlight.Tactic.FIGHT, anchor, dragoons[0]);
                    }
                });
                if (i == 30) {
                    for (FakeUnit d : dragoons) {
                        atlantis.map.position.HasPosition slot = atlantis.combat.cluster.ClusterRally.formationSlotFor(
                            d, dragoons[0], anchor, FightOrFlight.Tactic.FIGHT);
                        if (slot != null) slotted++;
                    }
                }
            }

            assertTrue(slotted >= 3,
                "mid-march the cluster holds formation slots facing the anchor: "
                    + slotted + " of " + dragoons.length);

            double minX = Double.MAX_VALUE;
            double maxY = Double.MIN_VALUE;
            double minY = Double.MAX_VALUE;
            for (FakeUnit d : dragoons) {
                minX = Math.min(minX, d.position.x());
                minY = Math.min(minY, d.position.y());
                maxY = Math.max(maxY, d.position.y());
            }
            double lateralTiles = (maxY - minY) / TILE;
            double advancedTiles = (minX - 10 * TILE) / TILE;

            assertTrue(advancedTiles >= 5.0,
                "the march advances at the anchor: " + advancedTiles + " tiles");
            assertTrue(lateralTiles >= 2.0,
                "the march keeps a lateral body instead of single file: "
                    + lateralTiles + " tiles wide");
        }
        finally {
            for (FakeUnit d : dragoons) Alpha.get().removeUnit(d);
            world.close();
            BattleCommander.clearForTests();
            EventLog.clearForTests();
        }
    }
}
