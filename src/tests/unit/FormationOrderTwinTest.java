package tests.unit;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.formation.FormationOrderLatch;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * NEXT #210: the calm needs a twin key. Every formation-order gate
 * decision rows issued/absorbed with its destination (transitions
 * always, 150f heartbeat), so a defending line reads as absorbs and
 * churn as issues - without grepping move orders.
 */
@Tag("combat")
public class FormationOrderTwinTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Override
    public bwapi.Race initEnemyRace() {
        return Race.Zerg;
    }

    @BeforeEach
    public void cleanLog() {
        EventLog.clearForTests();
        FormationOrderLatch.clearForTests();
        atlantis.combat.BattleCommander.clearForTests();
    }

    @AfterEach
    public void cleanLogAfter() {
        EventLog.clearForTests();
        FormationOrderLatch.clearForTests();
        atlantis.combat.BattleCommander.clearForTests();
        for (atlantis.units.AUnit u : Alpha.get().selection().list()) {
            Alpha.get().removeUnit(u);
        }
    }

    private static FakeUnit ling(double tx, double ty) {
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, tx, ty);
        ling.setEnemy();

        return ling;
    }

    @Test
    public void issueAndAbsorbRowWithDest() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 20);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 20, 20);

        world(30, fakeOurs(nexus, dragoon), fakeEnemies(
            ling(60, 20), ling(61, 20), ling(62, 20), ling(60, 21), ling(61, 21), ling(62, 21)
        ), () -> {
            if (atlantis.game.A.now() != 12) return;

            APosition slot = APosition.create(30, 20);
            assertTrue(FormationOrderLatch.shouldIssue(dragoon, slot),
                "first deployment is never delayed");
            assertTrue(EventLog.toJsonLines().contains("\"key\":\"FORMATION_ORDER\"")
                    && EventLog.toJsonLines().contains("\"result\":\"issued\""),
                "an issue rows with its destination: " + EventLog.toJsonLines());

            EventLog.clearForTests();
            assertFalse(FormationOrderLatch.shouldIssue(dragoon, slot),
                "identical re-issue inside the window is absorbed");
            assertTrue(EventLog.toJsonLines().contains("\"result\":\"absorbed\""),
                "an absorb rows instead of vanishing silently: " + EventLog.toJsonLines());
        });
    }

    @Test
    public void steadyAbsorbsHeartbeatInsteadOfFlooding() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 20);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 20, 20);

        world(400, fakeOurs(nexus, dragoon), fakeEnemies(
            ling(60, 20), ling(61, 20), ling(62, 20), ling(60, 21), ling(61, 21), ling(62, 21)
        ), () -> {
            int now = atlantis.game.A.now();
            if (now < 12 || now > 200) return;

            FormationOrderLatch.shouldIssue(dragoon, APosition.create(30, 20));
        });

        String rows = EventLog.toJsonLines();
        int absorbs = rows.split("\"result\":\"absorbed\"", -1).length - 1;
        assertTrue(absorbs >= 10 && absorbs <= 25,
            "absorbs row on every deny-transition (~1 per 10f window) but never per-frame-flood (got "
                + absorbs + ")");
    }
}
