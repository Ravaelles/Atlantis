package tests.unit.sim;

import atlantis.battle.cluster.Alpha;
import atlantis.battle.cluster.Missions;
import atlantis.combat.BattleCommander;
import atlantis.combat.cluster.FightOrFlight;
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
 * Split-cluster reinforcement off-map (live 2026-11-07 vs Steamhammer,
 * F591FAC2 8:02-8:40): mission ATTACK, ongoing fight at the enemy, idle
 * Dragoons with no target dressed at the main choke instead of joining -
 * half the army fought while the other half waited. The reinforce leg
 * used to be UMS-only, so off-map the march never came.
 *
 * <p>Flat arena on purpose: the decision reads only last-fight address,
 * unit position, tactic and the ongoing flag - no choke, region or fog
 * takes part, so geometry cannot change the answer. The sim proves the
 * wiring once: the idle Dragoon must close on the fight over frames,
 * tagged reinforce, not dressed home.</p>
 */
@Tag("combat")
@Tag("full-tier")
public class ReinforceJoinsOngoingFightSimTest extends AbstractTestWithUnits {

    @Test
    public void idleDragoonMarchesToTheOngoingFight() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 5, 5);
        FakeUnit fighter = fake(AUnitType.Protoss_Zealot, 88, 20);
        fighter.hp = 3000;
        FakeUnit idle = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit foe = fake(AUnitType.Zerg_Zergling, 89, 20);
        foe.setEnemy();
        foe.hp = 3000;

        SimArena arena = new SimArena(110 * 32, 40 * 32);
        SimWorld world = new SimWorld(
            fakeOurs(nexus, fighter, idle), fakeEnemies(foe), arena);

        try {
            BattleCommander.clearForTests();
            EventLog.clearForTests();
            Missions.forceGlobalMissionAttack("reinforce-repro");
            Alpha.gather();

            double startDist = idle.distTo(foe);
            for (int i = 0; i < 900; i++) {
                final int tick = i;
                world.tick(() -> {
                    if (!fighter.isAlive()) return;
                    BattleCommander.act(fighter, FightOrFlight.Tactic.FIGHT, null, fighter);
                    if (!idle.isAlive()) return;
                    BattleCommander.act(idle, FightOrFlight.Tactic.FIGHT, null, idle);
                });
                if (tick == 0) {
                    assertTrue(startDist > 60,
                        "repro needs the idle far from the fight: " + (int) startDist);
                }
            }
            double endDist = idle.distTo(foe);

            assertTrue(endDist < startDist - 20,
                "the idle Dragoon must close on the ongoing fight: start "
                    + (int) startDist + " end " + (int) endDist);
            assertTrue(idle.commandHistory().toString().contains("ClusterReinforce"),
                "the march is tagged reinforce, not dressed home: " + idle.commandHistory());
        }
        finally {
            world.close();
            BattleCommander.clearForTests();
            Missions.forceGlobalMissionDefend("test-cleanup");
        }
    }
}
