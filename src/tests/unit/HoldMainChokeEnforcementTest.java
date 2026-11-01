package tests.unit;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.BattleCommander;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.combat.cluster.ArmyMission;
import atlantis.combat.cluster.ClusterMission;
import atlantis.combat.cluster.MissionHold;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Slice 3a (owner #232): HOLD_MAIN_CHOKE forbids the out-march. A line
 * dressed at the stub main choke holds its station (no ghost creep, no
 * collapse march to a far anchor) and twins MISSION_ENFORCED; the same
 * line at the main marches. Fights and FLEE rallies are untouched by
 * construction (the gate only veers the no-target FIGHT fallthrough).
 */
@Tag("combat")
public class HoldMainChokeEnforcementTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @BeforeEach
    public void clean() {
        EventLog.clearForTests();
        BattleCommander.clearForTests();
        ArmyMission.clearForTests();
        MissionHold.clearForTests();
    }

    @AfterEach
    public void cleanAfter() {
        EventLog.clearForTests();
        BattleCommander.clearForTests();
        ArmyMission.clearForTests();
        MissionHold.clearForTests();
        for (atlantis.units.AUnit u : Alpha.get().selection().list()) {
            Alpha.get().removeUnit(u);
        }
    }

    @Test
    public void chokeLineHoldsInsteadOfMarchingOut() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 14, 25);

        world(3, fakeOurs(nexus, dragoon), new FakeUnit[0], () -> {
            if (atlantis.game.A.now() != 2) return;

            Alpha.gather();
            ArmyMission.sampleShadow(ClusterMission.Posture.DEFEND);
            assertTrue(ArmyMission.forbidsOutMarch(),
                "dressed at the stub choke, the mission forbids leaving");

            APosition farAnchor = APosition.create(28, 28);
            BattleCommander.act(
                dragoon, FightOrFlight.Tactic.FIGHT, farAnchor, dragoon);

            String log = EventLog.toJsonLines();
            assertTrue(log.contains("\"key\":\"MISSION_ENFORCED\""),
                "the veto is twinned, not silent: " + log);
            assertTrue(log.contains("\"mission\":\"HOLD_MAIN_CHOKE\""),
                "the twin names the enforcing mission: " + log);
            assertFalse(
                dragoon.lastCommand.equals("Move")
                    && dragoon.targetPosition() != null
                    && dragoon.targetPosition().equals(farAnchor.position()),
                "no march to the far anchor under enforcement, holds the choke");
        });
    }

    /**
     * Slice 3b (mirror of 3a): a line dressed at the stub natural choke
     * holds its own standoff under HOLD_NATURAL_CHOKE instead of marching
     * to the far anchor, and twins MISSION_ENFORCED with its mission.
     */
    @Test
    public void naturalLineHoldsInsteadOfMarchingOut() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 28, 19);

        world(3, fakeOurs(nexus, dragoon), new FakeUnit[0], () -> {
            if (atlantis.game.A.now() != 2) return;

            Alpha.gather();
            ArmyMission.sampleShadow(ClusterMission.Posture.DEFEND);
            assertTrue(ArmyMission.forbidsOutMarch(),
                "dressed at the stub natural choke, the mission forbids leaving");

            APosition farAnchor = APosition.create(60, 60);
            BattleCommander.act(
                dragoon, FightOrFlight.Tactic.FIGHT, farAnchor, dragoon);

            String log = EventLog.toJsonLines();
            assertTrue(log.contains("\"key\":\"MISSION_ENFORCED\""),
                "the veto is twinned, not silent: " + log);
            assertTrue(log.contains("\"mission\":\"HOLD_NATURAL_CHOKE\""),
                "the twin names the enforcing mission: " + log);
            assertFalse(
                dragoon.lastCommand.equals("Move")
                    && dragoon.targetPosition() != null
                    && dragoon.targetPosition().equals(farAnchor.position()),
                "no march to the far anchor under enforcement, holds the natural standoff");
        });
    }

    @Test
    public void mainLineMarchesWithoutEnforcement() {        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 10, 11);

        world(3, fakeOurs(nexus, dragoon), new FakeUnit[0], () -> {
            if (atlantis.game.A.now() != 2) return;

            Alpha.gather();
            ArmyMission.sampleShadow(ClusterMission.Posture.DEFEND);
            assertFalse(ArmyMission.forbidsOutMarch(),
                "at the main the mission is HOLD_MAIN, nothing is forbidden");

            APosition farAnchor = APosition.create(28, 28);
            BattleCommander.act(
                dragoon, FightOrFlight.Tactic.FIGHT, farAnchor, dragoon);

            assertTrue(dragoon.lastCommand.equals("Move"),
                "without enforcement the fallthrough marches: "
                    + dragoon.lastCommand);
            assertFalse(EventLog.toJsonLines().contains("\"key\":\"MISSION_ENFORCED\""),
                "no veto, no twin: " + EventLog.toJsonLines());
        });
    }

    @Test
    public void vetoTwinsOncePerFrame() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 14, 25);

        world(3, fakeOurs(nexus, dragoon), new FakeUnit[0], () -> {
            if (atlantis.game.A.now() != 2) return;

            Alpha.gather();
            ArmyMission.sampleShadow(ClusterMission.Posture.DEFEND);

            APosition farAnchor = APosition.create(28, 28);
            BattleCommander.act(
                dragoon, FightOrFlight.Tactic.FIGHT, farAnchor, dragoon);
            BattleCommander.act(
                dragoon, FightOrFlight.Tactic.FIGHT, farAnchor, dragoon);

            String log = EventLog.toJsonLines();
            int rows = log.split("\"key\":\"MISSION_ENFORCED\"", -1).length - 1;
            assertEquals(1, rows,
                "change-only: one veto row per frame, not per unit per call: " + log);
        });
    }
}
