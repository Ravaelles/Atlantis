package tests.unit;

import atlantis.combat.BattleCommander;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.units.AUnitType;
import bwapi.Race;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeOrderSink;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Anti-flap: a just-issued retreat move must execute instead of being
 * cancelled by the next frame's attack. After a retreat, the fight path
 * suppresses re-attack for 120 frames unless combat eval says winning.
 */
@Tag("combat")
public class RetreatReengageTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @AfterEach
    public void resetRetreat() {
        BattleCommander.clearForTests();
        Env.setLocalForTest(false);
    }

    @Test
    public void retreatSuppressesReattackFor120Frames() {
        enemyRaceInWorld = Race.Zerg;

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 7, 44);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 30);
        dragoon.cooldown = 0;
        FakeUnit l1 = fake(AUnitType.Zerg_Zergling, 31, 30);
        FakeUnit l2 = fake(AUnitType.Zerg_Zergling, 30, 31);
        FakeUnit l3 = fake(AUnitType.Zerg_Zergling, 31, 31);
        FakeUnit l4 = fake(AUnitType.Zerg_Zergling, 29, 30);

        world(3, fakeOurs(nexus, dragoon), fakeEnemies(l1, l2, l3, l4), () -> {
            if (A.now() != 1) return;
            Env.setLocalForTest(true);
            BattleCommander.clearForTests();

            ordersOf(dragoon).clear();
            BattleCommander.act(dragoon, FightOrFlight.Tactic.FLEE, null, null);
            assertTrue(hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                "FLEE issues a retreat move: " + ordersOf(dragoon));

            ordersOf(dragoon).clear();
            BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
            assertFalse(hasOrder(ordersOf(dragoon), dragoon.id(), "attackUnit"),
                "fresh retreat suppresses re-attack for 120f: " + ordersOf(dragoon));
        });
    }

    @Test
    public void reattackAllowedAfterCooldownExpiry() {
        enemyRaceInWorld = Race.Zerg;

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 7, 44);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 30);
        dragoon.cooldown = 0;
        FakeUnit l1 = fake(AUnitType.Zerg_Zergling, 31, 30);
        FakeUnit l2 = fake(AUnitType.Zerg_Zergling, 30, 31);
        FakeUnit l3 = fake(AUnitType.Zerg_Zergling, 31, 31);
        FakeUnit l4 = fake(AUnitType.Zerg_Zergling, 29, 30);

        world(3, fakeOurs(nexus, dragoon), fakeEnemies(l1, l2, l3, l4), () -> {
            if (A.now() != 1) return;
            Env.setLocalForTest(true);
            BattleCommander.clearForTests();

            ordersOf(dragoon).clear();
            BattleCommander.act(dragoon, FightOrFlight.Tactic.FLEE, null, null);
            assertTrue(hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                "FLEE issues a retreat move: " + ordersOf(dragoon));

            int later = A.now() + 131;
            A.setNow(later, later / 30);

            ordersOf(dragoon).clear();
            BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
            assertTrue(hasOrder(ordersOf(dragoon), dragoon.id(), "attackUnit"),
                "past the 120f window the fight re-arms: " + ordersOf(dragoon));
        });
    }

    @Test
    public void winningEvalExemptsFromSuppression() {
        enemyRaceInWorld = Race.Zerg;

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 7, 44);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 30);
        dragoon.cooldown = 0;
        FakeUnit mate = fake(AUnitType.Protoss_Dragoon, 29, 30);
        mate.cooldown = 0;
        FakeUnit l1 = fake(AUnitType.Zerg_Zergling, 32, 30);

        world(3, fakeOurs(nexus, dragoon, mate), fakeEnemies(l1), () -> {
            if (A.now() != 1) return;
            Env.setLocalForTest(true);
            BattleCommander.clearForTests();

            double eval = dragoon.eval();
            assertTrue(eval > 1.1, "exemption needs a winning read, got " + eval);

            ordersOf(dragoon).clear();
            BattleCommander.act(dragoon, FightOrFlight.Tactic.FLEE, null, null);
            assertTrue(hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                "FLEE issues a retreat move: " + ordersOf(dragoon));

            ordersOf(dragoon).clear();
            BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
            assertTrue(hasOrder(ordersOf(dragoon), dragoon.id(), "attackUnit"),
                "winning eval re-attacks despite the fresh retreat: " + ordersOf(dragoon));
        });
    }

    private static List<FakeOrderSink.Record> ordersOf(FakeUnit unit) {
        return ((FakeOrderSink) unit.orderSink()).orders();
    }

    private static boolean hasOrder(List<FakeOrderSink.Record> orders, int unitId, String operation) {
        for (FakeOrderSink.Record record : orders) {
            if (record.actorId == unitId && record.operation.equals(operation)) return true;
        }

        return false;
    }
}
