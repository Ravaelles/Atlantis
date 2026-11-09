package tests.unit;

import atlantis.combat.cluster.ArmyMission;
import atlantis.combat.cluster.ClusterMission;
import atlantis.combat.strategy.BattleStrategy;
import atlantis.combat.twins.ApproachTwin;
import atlantis.util.log.EventLog;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Approach twin (owner 2026-11-09, approach narrowing): the shadow
 * rule's answer rowed next to the live pick answer, per unit.
 * Transitions always row, steady approaches heartbeat - twin before
 * gate, so the shadow proves itself live and stable (and its
 * agreement with the pick measurable) before the commander port.
 */
@Tag("combat")
public class ApproachTwinTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @BeforeEach
    public void cleanBefore() {
        EventLog.clearForTests();
        ApproachTwin.clearForTests();
        ArmyMission.clearForTests();
    }

    @AfterEach
    public void cleanAfter() {
        EventLog.clearForTests();
        ApproachTwin.clearForTests();
        ArmyMission.clearForTests();
    }

    private static int countRows(String text) {
        int count = 0;
        int from = 0;
        while (true) {
            int at = text.indexOf("\"key\":\"APPROACH\"", from);
            if (at < 0) return count;

            count++;
            from = at + 1;
        }
    }

    @Test
    public void repeatSilentAndLiveChangeTransitions() {
        FakeUnit nexus = fake(atlantis.units.AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit zealot = fake(atlantis.units.AUnitType.Protoss_Zealot, 27, 20);

        world(3, fakeOurs(nexus, zealot), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            atlantis.battle.cluster.Alpha.get().addUnit(zealot);
            try {
                ArmyMission.sampleShadow(ClusterMission.Posture.DEFEND);

                ApproachTwin.sample(zealot, null, BattleStrategy.OPEN_FIELD);
                ApproachTwin.sample(zealot, null, BattleStrategy.OPEN_FIELD);
                ApproachTwin.sample(zealot, null, BattleStrategy.CHOKE_HOLD);

                String rows = EventLog.toJsonLines();
                assertTrue(rows.contains("\"key\":\"APPROACH\""), "got: " + rows);
                assertTrue(rows.contains("\"approach\":"), "got: " + rows);
                assertTrue(rows.contains("\"live\":"), "got: " + rows);
                assertEquals(2, countRows(rows),
                    "one row for the shadow, one for the live change, repeats silent: " + rows);
            } finally {
                atlantis.battle.cluster.Alpha.get().removeUnit(zealot);
                ArmyMission.clearForTests();
            }
        });
    }

    @Test
    public void steadyStateHeartbeats() {
        FakeUnit nexus = fake(atlantis.units.AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit zealot = fake(atlantis.units.AUnitType.Protoss_Zealot, 27, 20);

        world(ApproachTwin.HEARTBEAT_FRAMES + 5, fakeOurs(nexus, zealot), fakeEnemies(), () -> {
            if (atlantis.game.A.now() < 2) return;

            atlantis.battle.cluster.Alpha.get().addUnit(zealot);
            try {
                ArmyMission.sampleShadow(ClusterMission.Posture.DEFEND);
                ApproachTwin.sample(zealot, null, BattleStrategy.OPEN_FIELD);
            } finally {
                atlantis.battle.cluster.Alpha.get().removeUnit(zealot);
            }
        });
        ArmyMission.clearForTests();

        assertEquals(2, countRows(EventLog.toJsonLines()),
            "transition plus one heartbeat over the window");
    }
}
