package tests.unit;

import atlantis.battle.cluster.Missions;
import atlantis.combat.BattleCommander;
import atlantis.combat.FightTarget;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.combat.targeting.ChaseVeto;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Zealot chase veto (owner 2026-10-30, in words): a zealot holding home
 * does not march out after a far target. Vetoed chases forget the
 * contact (the ghost creep would re-chase next frame), allowed ones
 * keep it. Words, not numbers: CloseToBuilding, the recall line, the
 * base-ward exception, toe-to-toe contact, ongoing fights.
 */
@Tag("combat")
public class ChaseVetoTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @BeforeEach
    public void defendByDefault() {
        Missions.followPosture(atlantis.combat.cluster.ClusterMission.Posture.DEFEND);
        atlantis.util.log.EventLog.clearForTests();
        BattleCommander.clearForTests();
    }

    @AfterEach
    public void restoreDefend() {
        Missions.followPosture(atlantis.combat.cluster.ClusterMission.Posture.DEFEND);
        atlantis.util.log.EventLog.clearForTests();
        BattleCommander.clearForTests();
    }

    private static FakeUnit zealot(double tx, double ty) {
        return fake(AUnitType.Protoss_Zealot, tx, ty);
    }

    private static FakeUnit foe(double tx, double ty) {
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, tx, ty);
        foe.setEnemy();

        return foe;
    }

    @Test
    public void farOutwardChaseIsVetoedAndForgotten() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit ours = zealot(27, 20);
        FakeUnit enemy = foe(36, 20);

        world(3, fakeOurs(nexus, ours), fakeEnemies(enemy), () -> {
            if (atlantis.game.A.now() != 2) return;

            BattleCommander.act(ours, FightOrFlight.Tactic.FIGHT, null, ours);

            assertTrue(ours.lastCommand == null || !ours.lastCommand.equals("AttackUnit"),
                "a 9-tile march out is refused, not chased: " + ours.lastCommand);
            assertNull(FightTarget.lastContact(ours),
                "the refused chase forgets the contact instead of feeding the ghost creep");
            assertTrue(atlantis.util.log.EventLog.toJsonLines().contains("\"key\":\"CHASE_VETO\""),
                "the refusal rows a twin (transitions always row): "
                    + atlantis.util.log.EventLog.toJsonLines());
        });
    }

    @Test
    public void fallingBackOntoABasewardFoeIsAllowed() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit ours = zealot(38, 20);
        FakeUnit enemy = foe(36, 20);

        world(3, fakeOurs(nexus, ours), fakeEnemies(enemy), () -> {
            if (atlantis.game.A.now() != 2) return;

            BattleCommander.act(ours, FightOrFlight.Tactic.FIGHT, null, ours);

            assertEquals("AttackUnit", ours.lastCommand,
                "the foe sits between us and the base: falling back onto it is allowed");
            assertTrue(FightTarget.lastContact(ours) != null,
                "an allowed chase keeps its contact");
        });
    }

    @Test
    public void toeToToeIsFightingNotChasing() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit ours = zealot(36, 20);
        FakeUnit enemy = foe(37, 20);

        world(3, fakeOurs(nexus, ours), fakeEnemies(enemy), () -> {
            if (atlantis.game.A.now() != 2) return;

            assertFalse(ChaseVeto.veto(ours, enemy),
                "one tile apart past the line is contact, not a chase");
        });
    }

    @Test
    public void attackMissionChasesFreely() {
        Missions.followPosture(atlantis.combat.cluster.ClusterMission.Posture.ATTACK);

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit ours = zealot(27, 20);
        FakeUnit enemy = foe(36, 20);

        world(3, fakeOurs(nexus, ours), fakeEnemies(enemy), () -> {
            if (atlantis.game.A.now() != 2) return;

            BattleCommander.act(ours, FightOrFlight.Tactic.FIGHT, null, ours);

            assertTrue(FightTarget.lastContact(ours) != null,
                "on ATTACK the same geometry keeps its contact - the veto guards home");
        });
    }

    @Test
    public void dragoonsIgnoreTheVeto() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 27, 20);
        FakeUnit enemy = foe(36, 20);

        world(3, fakeOurs(nexus, dragoon), fakeEnemies(enemy), () -> {
            if (atlantis.game.A.now() != 2) return;

            assertFalse(ChaseVeto.veto(dragoon, enemy),
                "the veto is zealot-only (small-army generalization is open)");
        });
    }

    @Test
    public void ongoingFightPersists() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit ours = zealot(27, 20);
        FakeUnit enemy = foe(28, 20);

        world(3, fakeOurs(nexus, ours), fakeEnemies(enemy), () -> {
            if (atlantis.game.A.now() != 2) return;

            ours.attackUnit(enemy);
            enemy.position = atlantis.map.position.APosition.createFromPixels(36 * 32, 20 * 32);

            assertFalse(ChaseVeto.veto(ours, enemy),
                "the veto refuses new chases, never interrupts a brawl in progress");
        });
    }
}
