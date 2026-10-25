package tests.unit.sim;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import atlantis.combat.BattleCommander;
import atlantis.battle.cluster.Alpha;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.units.AUnitType;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * The zombie library drives a real sim loop: a chasing ling closes
 * from 8 tiles to contact and trades (our hp drops or it dies) -
 * proof the policies are not dead helpers but the loop's opponents.
 */
@Tag("combat")
public class ZombieSimLoopTest extends tests.acceptance.WorldStubForTests {

    @Test
    public void chaseNearestClosesAndTrades() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit d = fake(AUnitType.Protoss_Dragoon, 30, 20);
        d.hp = 120;
        d.shields = 80;
        d.cooldown = 0;
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 38, 20);
        ling.setEnemy();
        List<FakeUnit> foes = new ArrayList<>();
        foes.add(ling);

        SimArena arena = new SimArena(70 * 32, 40 * 32);
        SimWorld world = new SimWorld(
            new FakeUnit[] { nexus, d }, foes.toArray(new FakeUnit[0]), arena);
        try {
            BattleCommander.clearForTests();
            Alpha.gather();

            boolean closed = false;
            for (int i = 0; i < 300 && d.hp > 0 && ling.hp > 0; i++) {
                world.tick(() -> {
                    if (d.hp > 0) BattleCommander.act(d, FightOrFlight.Tactic.FIGHT, null, d);
                    ZombiePolicies.chaseNearest(foes, new ArrayList<>(java.util.Arrays.asList(d)));
                });
                if (d.position() != null && d.distTo(ling) < 1.0) closed = true;
            }
            assertTrue(closed, "the zombie closes 8 tiles to contact through the loop");
            assertTrue(ling.hp < 35 || d.hp + d.shields < 200,
                "contact trades damage one way or the other");
        }
        finally {
            world.close();
            BattleCommander.clearForTests();
        }
    }
}
