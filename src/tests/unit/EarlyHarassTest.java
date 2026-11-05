package tests.unit;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.BattleCommander;
import atlantis.combat.cluster.ArmyMission;
import atlantis.combat.cluster.ClusterMission;
import atlantis.combat.cluster.FightOrFlight;
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
import tests.fakes.FakeOrderSink;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Early harass override (owner 2026-11-07): a healthy first Dragoon
 * meets lone Zerglings/Zealots instead of holding formation against
 * them (FormHold) or standing off from them (ClusterStandoff). The
 * veto lives below the kite gates (kiting still outranks) and needs
 * melee intel only, health, and the first-Dragoon window.
 */
@Tag("combat")
public class EarlyHarassTest extends WorldStubForTests {

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

    private static FakeUnit ours(AUnitType type, int tx, int ty) {
        return fake(type, tx, ty);
    }

    private static FakeUnit foe(AUnitType type, int tx, int ty) {
        FakeUnit enemy = fake(type, tx, ty);
        enemy.setEnemy();

        return enemy;
    }

    private static boolean attacked(FakeUnit unit) {
        for (FakeOrderSink.Record record : ((FakeOrderSink) unit.orderSink()).orders()) {
            if (record.operation.contains("ttack")) return true;
        }

        return false;
    }

    @Test
    public void healthyFirstDragoonMeetsLingsInsteadOfHolding() {
        FakeUnit dragoon = ours(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit ling = foe(AUnitType.Zerg_Zergling, 37, 20);

        world(60, fakeOurs(dragoon), fakeEnemies(ling), () -> {
            if (atlantis.game.A.now() < 2) return;

            Alpha.gather();
            BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
        });

        assertTrue(attacked(dragoon),
            "healthy, first, melee intel: the harass vetoes the hold, the gun closes and fires: "
                + ((FakeOrderSink) dragoon.orderSink()).orders());
        assertTrue(EventLog.toJsonLines().contains("\"verdict\":\"HARASS\""),
            "the release is twinned, not silent");
    }

    @Test
    public void woundedDragoonStillHolds() {
        FakeUnit dragoon = ours(AUnitType.Protoss_Dragoon, 30, 20);
        dragoon.setHp((int) (dragoon.maxHp() * 0.5));
        FakeUnit ling = foe(AUnitType.Zerg_Zergling, 37, 20);

        world(3, fakeOurs(dragoon), fakeEnemies(ling), () -> {
            if (atlantis.game.A.now() != 2) return;

            Alpha.gather();
            BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);

            assertFalse(attacked(dragoon),
                "hurt: no harass, the hold stands: "
                    + ((FakeOrderSink) dragoon.orderSink()).orders());
        });
    }

    @Test
    public void harassMarchesOutInsteadOfStandingOff() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit dragoon = ours(AUnitType.Protoss_Dragoon, 14, 25);
        FakeUnit ling = foe(AUnitType.Zerg_Zergling, 24, 25);

        world(3, fakeOurs(nexus, dragoon), fakeEnemies(ling), () -> {
            if (atlantis.game.A.now() != 2) return;

            Alpha.gather();
            ArmyMission.sampleShadow(ClusterMission.Posture.DEFEND);
            assertTrue(ArmyMission.forbidsOutMarch(), "the mission forbids leaving");

            APosition farAnchor = APosition.create(28, 28);
            BattleCommander.act(
                dragoon, FightOrFlight.Tactic.FIGHT, farAnchor, dragoon);

            String log = EventLog.toJsonLines();
            assertFalse(log.contains("\"key\":\"MISSION_ENFORCED\""),
                "harass vetoes the standoff instead of holding it: " + log);
            assertTrue(dragoon.lastCommand.equals("Move"),
                "no target in reach, melee ahead: march out, don't stand: " + dragoon.lastCommand);
        });
    }
}
