package tests.unit;

import atlantis.combat.BattleCommander;
import atlantis.combat.cluster.ClusterBreach;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.combat.cluster.LeaveBreachedBase;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeOrderSink;
import tests.fakes.FakeUnit;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Owner: with raiders in the base the cluster ALWAYS fights - even two
 * enemy workers the evaluator reads as a won fight must not pull a
 * FLEE. The only retreat allowed on a breach is per-unit: hp &lt;= 40
 * with cooldown &gt;= 5 AND far from home walks to the choke-side
 * rally, everything else engages. Close to home the hatch stays shut
 * (owner 2026-10-26, tested loss vs Protoss): fleeing next to the base
 * just shuffles the unit among our own buildings.
 */
@Tag("combat")
public class BreachMeansFightTest extends AbstractTestWithWorld {

    private static boolean hasOrder(List<FakeOrderSink.Record> orders, int unitId, String operation) {
        for (FakeOrderSink.Record record : orders) {
            if (record.actorId == unitId && record.operation.equals(operation)) return true;
        }
        return false;
    }

    private static List<FakeOrderSink.Record> ordersOf(FakeUnit unit) {
        return ((FakeOrderSink) unit.orderSink()).orders();
    }

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @Test
    public void breachForcesFight() {
        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Zealot, 22, 21)
        ), fakeEnemies(
            fake(AUnitType.Protoss_Probe, 25, 21),
            fake(AUnitType.Protoss_Probe, 26, 22)
        ), () -> {
            assertTrue(
                ClusterBreach.breachForcesFight(),
                "two enemy workers at the base force FIGHT before any evaluation");
        });
    }

    @Test
    public void noBreachNoForce() {
        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Zealot, 22, 21)
        ), fakeEnemies(
            fake(AUnitType.Protoss_Probe, 60, 21)
        ), () -> {
            assertFalse(
                ClusterBreach.breachForcesFight(),
                "a far scout is not a breach - the evaluator decides");
        });
    }

    @Test
    public void criticalZealotRetreatsToRally() {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 70, 70);
        zealot.setHp(30);
        zealot.cooldown = 5;

        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            zealot
        ), fakeEnemies(
            fake(AUnitType.Protoss_Probe, 25, 21),
            fake(AUnitType.Protoss_Probe, 26, 22)
        ), () -> {
            BattleCommander.act(
                zealot, FightOrFlight.Tactic.FIGHT, null, zealot);

            assertTrue(
                hasOrder(ordersOf(zealot), zealot.id(), "move"),
                "hp 30 on cooldown, caught far from home: the hatch lets this one leave: "
                    + ordersOf(zealot));
            assertFalse(
                hasOrder(ordersOf(zealot), zealot.id(), "attackUnit"),
                "no last stand on cooldown with 30 hp: " + ordersOf(zealot));
        });
    }

    @Test
    public void closeZealotHoldsTheLine() {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 22, 21);
        zealot.setHp(30);
        zealot.cooldown = 5;

        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            zealot
        ), fakeEnemies(
            fake(AUnitType.Protoss_Probe, 25, 21),
            fake(AUnitType.Protoss_Probe, 26, 22)
        ), () -> {
            assertFalse(
                LeaveBreachedBase.leave(zealot),
                "this close to home the hatch stays shut even on critical hp");
            assertFalse(
                hasOrder(ordersOf(zealot), zealot.id(), "move"),
                "no shuffling among our own buildings: " + ordersOf(zealot));
        });
    }

    @Test
    public void hatchWaitsForTheSwing() {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 22, 21);
        FakeUnit raider = fake(AUnitType.Protoss_Probe, 25, 21);
        zealot.setHp(30);
        zealot.cooldown = 5;

        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            zealot
        ), fakeEnemies(
            raider,
            fake(AUnitType.Protoss_Probe, 26, 22)
        ), () -> {
            zealot.attackUnit(raider);
            zealot.attackFrame = true;
            ((tests.fakes.FakeOrderSink) zealot.orderSink()).clear();
            if (!zealot.isAttackFrame()) return;

            BattleCommander.act(
                zealot, FightOrFlight.Tactic.FIGHT, null, zealot);

            assertTrue(
                ordersOf(zealot).isEmpty(),
                "mid-swing retreat would cancel the shot, hatch or not: "
                    + ordersOf(zealot));
        });
    }

    @Test
    public void healthyZealotEngagesBreachers() {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 22, 21);
        zealot.setHp(100);
        zealot.cooldown = 0;

        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            zealot
        ), fakeEnemies(
            fake(AUnitType.Protoss_Probe, 25, 21),
            fake(AUnitType.Protoss_Probe, 26, 22)
        ), () -> {
            BattleCommander.act(
                zealot, FightOrFlight.Tactic.FIGHT, null, zealot);

            assertTrue(
                hasOrder(ordersOf(zealot), zealot.id(), "attackUnit"),
                "healthy on a breach: engage: " + ordersOf(zealot));
            assertFalse(
                hasOrder(ordersOf(zealot), zealot.id(), "move"),
                "no retreat for the healthy: " + ordersOf(zealot));
        });
    }

    @Test
    public void rallyIsNeverNull() {
        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Zealot, 22, 21)
        ), fakeEnemies(), () -> {
            HasPosition rally = ClusterBreach.breachRallyPoint();
            assertNotNull(rally, "baseless or chokeless maps fall back, never null");
        });
    }
}
