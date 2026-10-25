package tests.unit;

import atlantis.combat.BattleCommander;
import atlantis.combat.IdleWatch;
import atlantis.battle.cluster.Alpha;
import atlantis.combat.cluster.ClusterMission;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.combat.twins.ClusterTwin;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Owner, Drag_v_LingsUpg2: a combat unit that does not displace while
 * no enemy is near and its weapon is ready is the post-fight freeze
 * signature - healthy play always has an address. The twin names it
 * (per-unit IDLE rows, one HIGH army STALL row), never auto-fixing.
 */
@Tag("combat")
public class IdleWatchTest extends WorldStubForTests {

    @BeforeEach
    public void cleanLogBefore() {
        EventLog.clearForTests();
        IdleWatch.clearForTests();
    }

    @AfterEach
    public void cleanLogAfter() {
        EventLog.clearForTests();
        IdleWatch.clearForTests();
        ClusterTwin.clearForTests();
        for (atlantis.units.AUnit u : Alpha.get().selection().list()) Alpha.get().removeUnit(u);
    }

    @Test
    public void stillWithNoFoeAndReadyWeaponLogsIdle() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 30);
        dragoon.cooldown = 0;

        world(150, fakeOurs(dragoon), new FakeUnit[]{}, () -> {
            int now = atlantis.game.A.now();
            if (now >= 1) {
                BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
            }
        });

        String rows = EventLog.toJsonLines();
        assertTrue(rows.contains("\"key\":\"IDLE\""),
            "a still-ready-lonely dragoon must be named: " + rows);
        assertTrue(rows.contains("\"unit\":" + dragoon.id()),
            "the row names the unit: " + rows);
    }

    @Test
    public void displacingUnitLogsNothing() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 30);
        dragoon.cooldown = 0;

        world(150, fakeOurs(dragoon), new FakeUnit[]{}, () -> {
            int now = atlantis.game.A.now();
            if (now >= 1) {
                dragoon.position = APosition.create(30 + now / 30, 30);
                BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
            }
        });

        assertFalse(EventLog.toJsonLines().contains("\"key\":\"IDLE\""),
            "a displacing unit is marching, not frozen");
    }

    @Test
    public void foeNearLogsNothing() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 30);
        dragoon.cooldown = 0;
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 31, 30);
        ling.setEnemy();

        world(150, fakeOurs(dragoon), fakeEnemies(ling), () -> {
            int now = atlantis.game.A.now();
            if (now >= 1) {
                BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
            }
        });

        assertFalse(EventLog.toJsonLines().contains("\"key\":\"IDLE\""),
            "a nearby enemy explains standing still");
    }

    @Test
    public void coolingDownLogsNothing() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 30);
        dragoon.cooldown = 25;

        world(150, fakeOurs(dragoon), new FakeUnit[]{}, () -> {
            int now = atlantis.game.A.now();
            if (now >= 1) {
                BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
            }
        });

        assertFalse(EventLog.toJsonLines().contains("\"key\":\"IDLE\""),
            "a cooling weapon explains standing still");
    }

    @Test
    public void parkedArmyLogsStall() {
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 30, 30);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 31, 30);
        a.cooldown = 0;
        b.cooldown = 0;

        world(300, fakeOurs(a, b), new FakeUnit[]{}, () -> {
            int now = atlantis.game.A.now();
            if (now >= 1) {
                Alpha.gather();
                BattleCommander.act(a, FightOrFlight.Tactic.FIGHT, null, a);
                BattleCommander.act(b, FightOrFlight.Tactic.FIGHT, null, b);
                ClusterTwin.sample(ClusterMission.Posture.DEFEND, FightOrFlight.Tactic.FIGHT, null);
            }
        });

        String rows = EventLog.toJsonLines();
        assertTrue(rows.contains("\"key\":\"STALL\""),
            "a ten-second parked army must raise one HIGH row: " + rows);
    }
}
