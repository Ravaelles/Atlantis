package tests.unit;

import atlantis.battle.BattleCommander;
import atlantis.battle.FightOrFlight;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeOrderSink;
import tests.fakes.FakeUnit;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Owner: with raiders in the base the cluster ALWAYS fights - even two
 * enemy workers the evaluator reads as a won fight must not pull a
 * FLEE. The only retreat allowed on a breach is per-unit: hp &lt;= 40
 * with cooldown &gt;= 5 walks to the choke-side rally, everything else
 * engages.
 */
public class BreachMeansFightTest extends AbstractTestWithWorld {

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
                BattleCommander.breachForcesFight(),
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
                BattleCommander.breachForcesFight(),
                "a far scout is not a breach - the evaluator decides");
        });
    }

    @Test
    public void criticalZealotRetreatsToRally() {
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
            BattleCommander.act(
                zealot, FightOrFlight.Tactic.FIGHT, null, zealot);

            assertTrue(
                hasOrder(ordersOf(zealot), zealot.id(), "move"),
                "hp 30 on cooldown: the hatch lets this one leave: "
                    + ordersOf(zealot));
            assertFalse(
                hasOrder(ordersOf(zealot), zealot.id(), "attackUnit"),
                "no last stand on cooldown with 30 hp: " + ordersOf(zealot));
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
            HasPosition rally = BattleCommander.breachRallyPoint();
            assertNotNull(rally, "baseless or chokeless maps fall back, never null");
        });
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
