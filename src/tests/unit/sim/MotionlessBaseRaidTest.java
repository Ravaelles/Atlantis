package tests.unit.sim;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.BattleCommander;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;
import tests.unit.AbstractTestWithUnits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * StarEngine motionless-base raid (owner order): the enemy sits on a Nexus
 * with 10 Zealots holding still in their base. A Dragoon mass staged at home
 * must go there and win - the live suspicion is that the army waits at home
 * (it did, before the EnemyMainBase latch fix), so this pins the capability
 * half: ordered at the enemy base, the army CAN kill it.
 *
 * <p>Not a mission-layer test: the focus that sends the army is proven live
 * (010715 marched to [95,119] and won; 013929 held it). What this guards is
 * the fight itself - per-unit logic gluing, kiting away, or losing a
 * straight trade against motionless defenders would fail here first.</p>
 */
@Tag("combat")
public class MotionlessBaseRaidTest extends AbstractTestWithUnits {

    private static final int TILE = 32;

    private static FakeUnit dragoon(double tx, double ty) {
        FakeUnit z = fake(AUnitType.Protoss_Dragoon, tx, ty);
        z.hp = 180;
        z.shields = 20;
        z.cooldown = 0;

        return z;
    }

    private static FakeUnit zealot(double tx, double ty) {
        FakeUnit z = fake(AUnitType.Protoss_Zealot, tx, ty);
        z.hp = 100;
        z.cooldown = 0;

        return z;
    }

    private static FakeUnit enemyZealot(double tx, double ty) {
        FakeUnit z = zealot(tx, ty);
        z.setEnemy();

        return z;
    }

    @Test
    public void dragoonMassRaidsMotionlessBaseAndWins() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit foeNexus = fake(AUnitType.Protoss_Nexus, 110, 110);
        foeNexus.setEnemy();
        foeNexus.hp = 1500;

        List<FakeUnit> ours = new ArrayList<>();
        for (int i = 0; i < 12; i++) ours.add(dragoon(12 + i, 12));
        ours.add(nexus);

        List<FakeUnit> foes = new ArrayList<>(Arrays.asList(
            foeNexus,
            enemyZealot(108, 108), enemyZealot(109, 108), enemyZealot(110, 108),
            enemyZealot(108, 109), enemyZealot(109, 109), enemyZealot(110, 109),
            enemyZealot(108, 110), enemyZealot(109, 110), enemyZealot(110, 110),
            enemyZealot(111, 108)));

        SimArena arena = new SimArena(130 * TILE, 130 * TILE);
        SimWorld world = new SimWorld(
            ours.toArray(new FakeUnit[0]), foes.toArray(new FakeUnit[0]), arena);
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
            Alpha.gather();

            for (int i = 0; i < 6000; i++) {
                world.tick(() -> {
                    for (FakeUnit u : ours) {
                        if (u.hp <= 0 || !u.type().equals(AUnitType.Protoss_Dragoon)) continue;
                        BattleCommander.act(u, FightOrFlight.Tactic.FIGHT, foeNexus.position(), u);
                    }
                    ZombiePolicies.holdStill(foes, ours);
                });
                if (foeNexus.hp <= 0) break;
            }
        }
        finally {
            world.close();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
        }

        long foesAlive = foes.stream().filter(f -> f.hp > 0).count();
        long dragoonsAlive = ours.stream()
            .filter(u -> u.type().equals(AUnitType.Protoss_Dragoon) && u.hp > 0).count();
        assertTrue(foeNexus.hp <= 0,
            "the raid reaches the motionless base and kills the Nexus (foes alive: " + foesAlive + ")");
        assertTrue(foesAlive <= 3,
            "the defenders die holding still (alive: " + foesAlive + ")");
        assertTrue(dragoonsAlive >= 6,
            "the mass survives the trade (dragoons alive: " + dragoonsAlive + "/12)");
    }
}
