package tests.unit;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.cluster.ArmyMission;
import atlantis.combat.cluster.ClusterMission;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Shadow wiring (owner #232, slice 1b): the live snapshot feeds
 * decide() and the answer is twinned, steering nothing. The stub map's
 * main choke sits at [13,25] (FakeChokes), the natural at [28,18].
 */
@Tag("combat")
public class ArmyMissionShadowTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @BeforeEach
    public void cleanLog() {
        EventLog.clearForTests();
        atlantis.combat.BattleCommander.clearForTests();
    }

    @AfterEach
    public void cleanLogAfter() {
        EventLog.clearForTests();
        atlantis.combat.BattleCommander.clearForTests();
        for (atlantis.units.AUnit u : Alpha.get().selection().list()) {
            Alpha.get().removeUnit(u);
        }
    }

    @Test
    public void lineAtMainTwinsHoldMain() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 30);

        world(3, fakeOurs(nexus, dragoon), new FakeUnit[0], () -> {
            if (atlantis.game.A.now() != 2) return;

            Alpha.gather();
            ArmyMission.sampleShadow(ClusterMission.Posture.DEFEND);

            assertTrue(EventLog.toJsonLines().contains("\"key\":\"MISSION_SHADOW\""),
                "the shadow answers every frame: " + EventLog.toJsonLines());
            assertTrue(EventLog.toJsonLines().contains("\"mission\":\"HOLD_MAIN\""),
                "a line at the main with no contact holds the main: " + EventLog.toJsonLines());
        });
    }

    @Test
    public void lineAtMainChokeTwinsHoldMainChoke() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 14, 25);

        world(3, fakeOurs(nexus, dragoon), new FakeUnit[0], () -> {
            if (atlantis.game.A.now() != 2) return;

            Alpha.gather();
            ArmyMission.sampleShadow(ClusterMission.Posture.DEFEND);

            assertTrue(EventLog.toJsonLines().contains("\"mission\":\"HOLD_MAIN_CHOKE\""),
                "a line dressed at the stub main choke holds the mouth: " + EventLog.toJsonLines());
        });
    }
}
