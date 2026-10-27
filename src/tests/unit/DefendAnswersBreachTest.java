package tests.unit;

import atlantis.combat.BattleCommander;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A DEFEND unit whose 9-tile reach misses the raiders chewing our workers
 * must still answer a breached base instead of bunching on the anchor
 * (measured: UMS 7th_rav, workers under attack, DEFEND zealots idle).
 * Ad-hoc fallback per owner direction (see the // @ToDo on
 * {@code BattleCommander.breachedBaseTarget}): the proper fix is
 * threat-aware assignment owned by the cluster.
 */
@Tag("combat")
public class DefendAnswersBreachTest extends AbstractTestWithWorld {

    private static boolean hasOrder(java.util.List<tests.fakes.FakeOrderSink.Record> orders,
                                    int unitId, String operation) {
        for (tests.fakes.FakeOrderSink.Record record : orders) {
            if (record.actorId == unitId && record.operation.equals(operation)) return true;
        }
        return false;
    }

    private static java.util.List<tests.fakes.FakeOrderSink.Record> ordersOf(FakeUnit unit) {
        return ((tests.fakes.FakeOrderSink) unit.orderSink()).orders();
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
    public void defendZealotEngagesBreachingRaiderOutOfNormalReach() {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 22, 21);
        // 12 tiles from our zealot (out of the 9-tile engage envelope)
        // but 14 from our Nexus (inside the 15-tile breach radius).
        FakeUnit raider = fake(AUnitType.Protoss_Zealot, 34, 21);

        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            zealot
        ), fakeEnemies(raider), () -> {
            BattleCommander.act(
                zealot, FightOrFlight.Tactic.FIGHT, null, zealot);

            assertTrue(
                hasOrder(ordersOf(zealot), zealot.id(), "attackUnit"),
                "melee has no firing position to glide to - the engine chases on one attack order: "
                    + ordersOf(zealot));
            assertFalse(
                hasOrder(ordersOf(zealot), zealot.id(), "move"),
                "no glide stutter for melee: " + ordersOf(zealot));
        });
    }

    @Test
    public void noBreachMeansNoCharge() {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 22, 21);
        // Out of reach AND out of the base radius: no breach, bunching only.
        FakeUnit lurker = fake(AUnitType.Protoss_Zealot, 40, 21);

        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            zealot
        ), fakeEnemies(lurker), () -> {
            BattleCommander.act(
                zealot, FightOrFlight.Tactic.FIGHT, null, zealot);

            assertFalse(
                hasOrder(ordersOf(zealot), zealot.id(), "attackUnit"),
                "no breach, no charge: " + ordersOf(zealot));
            assertFalse(
                hasOrder(ordersOf(zealot), zealot.id(), "move"),
                "nothing to engage and nowhere to bunch (null anchor, self leader): "
                    + ordersOf(zealot));
        });
    }
}
