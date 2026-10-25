package tests.unit;

import atlantis.battle.BattleCommander;
import atlantis.battle.cluster.ClusterRally;
import atlantis.battle.cluster.ClusterTactic;
import atlantis.battle.cluster.ClusterMission;
import atlantis.battle.cluster.FightOrFlight;
import atlantis.game.AGame;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeOrderSink;
import tests.fakes.FakeUnit;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * UMS retreat discipline (owner): on custom maps there is no base to
 * run to, so FLEE in the opening just feeds. No retreat for the first
 * 15 seconds; after that a baseless retreat rallies AWAY from the enemy
 * to regroup, never at the enemy base the mission anchor would march it
 * to. And while raiders are in the base, posture holds DEFEND even with
 * an army big enough to march - marching out abandons the workers.
 */
@Tag("combat")
public class UmsRetreatRulesTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @AfterEach
    public void clearUmsFlag() {
        // MockedStatic closes in the harness @AfterEach, so per-test stubs
        // die with the test - this only documents the default.
        aGame.when(AGame::isUms).thenReturn(false);
    }

    @Test
    public void umsOpeningNeverRetreats() {
        aGame.when(AGame::isUms).thenReturn(true);

        world(1, fakeOurs(
            fake(AUnitType.Protoss_Dragoon, 30, 20)
        ), fakeEnemies(
            fake(AUnitType.Protoss_Zealot, 33, 20)
        ), () -> {
                assertTrue(
                ClusterTactic.earlyUmsStandGround(),
                "UMS frame 1: stand and fight, there is nowhere to run to");
        });
    }

    @Test
    public void noUmsFlagNoStandGround() {
        world(1, fakeOurs(
            fake(AUnitType.Protoss_Dragoon, 30, 20)
        ), fakeEnemies(
            fake(AUnitType.Protoss_Zealot, 33, 20)
        ), () -> {
            assertFalse(
                ClusterTactic.earlyUmsStandGround(),
                "ladder games keep the evaluator in charge");
        });
    }

    @Test
    public void standGroundExpiresAfterFifteenSeconds() {
        aGame.when(AGame::isUms).thenReturn(true);

        world(400, fakeOurs(
            fake(AUnitType.Protoss_Dragoon, 30, 20)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 399) return;

            assertFalse(
                ClusterTactic.earlyUmsStandGround(),
                "at 16.6 s the UMS leash comes off: " + atlantis.game.A.now());
        });
    }

    @Test
    public void baselessRallyRunsFromTheEnemy() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 33, 20);

        world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            HasPosition rally = ClusterRally.regroupRallyPoint(dragoon);

            assertNotNull(rally, "open ground offers an escape direction");
            assertTrue(
                rally.distTo(zealot.position()) > dragoon.distTo(zealot),
                "the rally opens distance instead of closing it: "
                    + rally + " vs " + dragoon.position());
        });
    }

    @Test
    public void noThreatMeansNoRally() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit far = fake(AUnitType.Protoss_Zealot, 90, 20);

        world(1, fakeOurs(dragoon), fakeEnemies(far), () -> {
            assertTrue(
                ClusterRally.regroupRallyPoint(dragoon) == null,
                "nothing pressing: hold and fight in place");
        });
    }

    @Test
    public void rallyWaitsForTheSwing() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 33, 20);
        dragoon.cooldown = 25;

        world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            dragoon.attackUnit(zealot);
            dragoon.attackFrame = true;
            ((FakeOrderSink) dragoon.orderSink()).clear();
            if (!dragoon.isAttackFrame()) return;

            BattleCommander.act(
                dragoon, FightOrFlight.Tactic.FLEE, null, dragoon);

            assertTrue(
                ordersOf(dragoon).isEmpty(),
                "mid-swing rally would cancel the shot: " + ordersOf(dragoon));
        });
    }

    @Test
    public void baselessFleeWalksAwayInsteadOfMarching() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 33, 20);

        world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            BattleCommander.act(
                dragoon, FightOrFlight.Tactic.FLEE, null, dragoon);

            assertTrue(
                hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                "baseless FLEE rallies away: " + ordersOf(dragoon));
            assertFalse(
                hasOrder(ordersOf(dragoon), dragoon.id(), "attackUnit"),
                "no standing to trade on the retreat: " + ordersOf(dragoon));
        });
    }

    @Test
    public void baselessUmsNeverFlees() {
        aGame.when(AGame::isUms).thenReturn(true);

        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 33, 20);

        world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            BattleCommander.act(
                dragoon, FightOrFlight.Tactic.FLEE, null, dragoon);

            assertFalse(
                hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                "no base on a UMS map: ClusterFlee would donate the duel: "
                    + ordersOf(dragoon));
            assertTrue(
                hasOrder(ordersOf(dragoon), dragoon.id(), "attackUnit"),
                "no retreat means the fight, not a standstill: " + ordersOf(dragoon));
        });
    }

    @Test
    public void breachHoldsDefendPostureAtAnyArmySize() {
        // Independent hypotheticals: the posture latch is per-game state,
        // one query must not hold the next.
        atlantis.battle.cluster.ClusterMission.resetForTests();
        assertEquals(
            ClusterMission.Posture.DEFEND,
            ClusterMission.postureFor(ClusterMission.ATTACK_ARMY_SIZE + 20, true),
            "raiders in the base pin even a big army home - marching out abandons the workers");
        atlantis.battle.cluster.ClusterMission.resetForTests();
        assertEquals(
            ClusterMission.Posture.ATTACK,
            ClusterMission.postureFor(ClusterMission.ATTACK_ARMY_SIZE + 20, false),
            "clear base, big army: march as before");
        atlantis.battle.cluster.ClusterMission.resetForTests();
        assertEquals(
            ClusterMission.Posture.DEFEND,
            ClusterMission.postureFor(3, false),
            "clear base, small army: defend as before");
    }

    private static boolean hasOrder(List<FakeOrderSink.Record> orders, int unitId, String operation) {
        for (FakeOrderSink.Record record : orders) {
            if (record.actorId == unitId && record.operation.equals(operation)) return true;
        }
        return false;
    }

    private static List<FakeOrderSink.Record> ordersOf(FakeUnit unit) {
        return ((FakeOrderSink) unit.orderSink()).orders();
    }
}
