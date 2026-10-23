package tests.unit;

import atlantis.battle.cluster.Alpha;
import atlantis.battle.cluster.ClusterMission;
import atlantis.battle.cluster.FightOrFlight;
import atlantis.battle.twins.ClusterTwin;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.util.log.EventLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Owner 2026-10-23 (Stardust-style: log the transition target, not every
 * step): the CLUSTER twin carries the leader's current move dest, so the
 * next retreat names the rally it actually walks (standoff vs base vs
 * anchor) instead of leaving it to center forensics.
 */
@Tag("combat")
public class ClusterTwinDestTest extends WorldStubForTests {

    @BeforeEach
    public void cleanLogBefore() {
        EventLog.clearForTests();
    }

    @AfterEach
    public void cleanLogAfter() {
        EventLog.clearForTests();
        for (atlantis.units.AUnit u : Alpha.get().selection().list()) Alpha.get().removeUnit(u);
    }

    @Test
    public void leaderDestRowsWhenOrdered() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 30);

        world(32, fakeOurs(nexus, dragoon), new FakeUnit[]{}, () -> {
            if (atlantis.game.A.now() == 1) {
                Alpha.gather();
                dragoon.move(atlantis.map.position.APosition.create(98, 96), Actions.MOVE_FORMATION, "test");
            }
            if (atlantis.game.A.now() == 31) {
                ClusterTwin.sample(ClusterMission.Posture.DEFEND, FightOrFlight.Tactic.FLEE, nexus.position());
            }
        });

        String rows = EventLog.toJsonLines();
        assertTrue(rows.contains("\"key\":\"CLUSTER\""), "a CLUSTER row was sampled: " + rows);
        assertTrue(rows.contains("\"dtx\":98") && rows.contains("\"dty\":96"),
            "the leader's dest rides the row: " + rows);
    }

    @Test
    public void noOrdersRowsMinusOne() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 30);

        world(32, fakeOurs(nexus, dragoon), new FakeUnit[]{}, () -> {
            if (atlantis.game.A.now() == 31) {
                Alpha.gather();
                ClusterTwin.sample(ClusterMission.Posture.DEFEND, FightOrFlight.Tactic.FIGHT, nexus.position());
            }
        });

        String rows = EventLog.toJsonLines();
        assertTrue(rows.contains("\"key\":\"CLUSTER\""), "a CLUSTER row was sampled: " + rows);
        assertTrue(rows.contains("\"dtx\":-1") && rows.contains("\"dty\":-1"),
            "no orders reads -1, never a stale dest: " + rows);
    }

    @Test
    public void nearestFoePositionRowsWithDistance() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 30);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 40, 30);
        foe.setEnemy();

        world(32, fakeOurs(nexus, dragoon), fakeEnemies(foe), () -> {
            if (atlantis.game.A.now() == 31) {
                Alpha.gather();
                ClusterTwin.sample(ClusterMission.Posture.DEFEND, FightOrFlight.Tactic.FIGHT, nexus.position());
            }
        });

        String rows = EventLog.toJsonLines();
        assertTrue(rows.contains("\"key\":\"CLUSTER\""), "a CLUSTER row was sampled: " + rows);
        assertTrue(rows.contains("\"fx\":40") && rows.contains("\"fy\":30"),
            "the nearest foe's tile rides next to its distance: " + rows);
    }

    @Test
    public void noFoeRowsMinusOne() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 30);

        world(32, fakeOurs(nexus, dragoon), new FakeUnit[]{}, () -> {
            if (atlantis.game.A.now() == 31) {
                Alpha.gather();
                ClusterTwin.sample(ClusterMission.Posture.DEFEND, FightOrFlight.Tactic.FIGHT, nexus.position());
            }
        });

        String rows = EventLog.toJsonLines();
        assertTrue(rows.contains("\"key\":\"CLUSTER\""), "a CLUSTER row was sampled: " + rows);
        assertTrue(rows.contains("\"fx\":-1") && rows.contains("\"fy\":-1"),
            "no foe reads -1, never a stale position: " + rows);
    }
}
