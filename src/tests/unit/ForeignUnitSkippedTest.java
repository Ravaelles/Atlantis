package tests.unit;

import atlantis.combat.BattleCommander;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeOrderSink;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The commander never orders a foreign unit (NEXT #199, live 1a2a3a
 * Micro 2: a renegade Zergling transiently ours reached
 * Regroup.march and marched an ADVANCE_FORMATION slot into unwalkable
 * ground - the ### spam with a full stack for a unit we do not own).
 * Ownership can flip mid-frame under UMS renegade mechanics, after
 * the roster cached a slot for it; the funnel re-checks ownership,
 * so a stale cached slot never marches a unit we just lost. Zero
 * behavior for the normal game (every acted unit there is ours).
 */
@Tag("combat")
public class ForeignUnitSkippedTest extends WorldStubForTests {

    private static java.util.List<FakeOrderSink.Record> ordersOf(FakeUnit unit) {
        return ((FakeOrderSink) unit.orderSink()).orders();
    }

    @Test
    public void rosteredFlipGetsNoOrders() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit flipper = fake(AUnitType.Protoss_Zealot, 31, 21);
        FakeUnit mate = fake(AUnitType.Protoss_Zealot, 29, 21);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 40, 20);
        foe.setEnemy();

        world(20, fakeOurs(dragoon, flipper, mate), fakeEnemies(foe), () -> {
            if (atlantis.game.A.now() != 2) return;

            atlantis.battle.cluster.Alpha.get().addUnit(dragoon);
            atlantis.battle.cluster.Alpha.get().addUnit(flipper);
            atlantis.battle.cluster.Alpha.get().addUnit(mate);

            // Rostered and ours: the formation marches it.
            BattleCommander.act(flipper, FightOrFlight.Tactic.FIGHT, null, dragoon);
            assertFalse(
                ordersOf(flipper).isEmpty(),
                "rostered while ours: the commander works");

            // Ownership flips mid-frame (UMS renegade): the cached slot
            // must not march a unit we just lost.
            ordersOf(flipper).clear();
            flipper.setEnemy();
            BattleCommander.act(flipper, FightOrFlight.Tactic.FIGHT, null, dragoon);
            assertTrue(
                ordersOf(flipper).isEmpty(),
                "flipped mid-frame: no orders on a stale slot: " + ordersOf(flipper));
        });
    }

    @Test
    public void plainForeignUnitGetsNoOrders() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 34, 20);
        zealot.setEnemy();

        world(20, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            if (atlantis.game.A.now() != 2) return;

            BattleCommander.act(zealot, FightOrFlight.Tactic.FIGHT, null, dragoon);

            assertTrue(
                ordersOf(zealot).isEmpty(),
                "an enemy unit in our commander gets nothing: " + ordersOf(zealot));
        });
    }
}
