package tests.unit;

import atlantis.battle.BattleCommander;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * A cooling-down Dragoon with the enemy on top of it backs off instead
 * of trading at point blank (owner 2026-10-19, Wine 1Drag_v_1Zeal:
 * the Dragoon stood still under a Zealot with DoNothing and died -
 * no kite existed at all). Enemy closer than 3.5 tiles + cooldown at
 * least 10 frames = step back; shooting resumes once the weapon is
 * ready. Dragoon-only: Zealots want contact.
 */
public class DragoonKiteTest extends AbstractTestWithWorld {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Override
    public FakeUnit[] generateOur() {
        return null;
    }

    @Override
    public FakeUnit[] generateEnemies() {
        return null;
    }

    @Test
    public void coolingDragoonBacksOffFromCloseZealot() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        dragoon.cooldown = 15;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            assertTrue(BattleCommander.shouldKite(dragoon, zealot));
        });
    }

    @Test
    public void readyWeaponKeepsShooting() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        dragoon.cooldown = 5;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            assertFalse(BattleCommander.shouldKite(dragoon, zealot),
                "weapon almost ready: stand and fire instead of stepping back");
        });
    }

    @Test
    public void distantEnemyIsApproachedNotKited() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 36, 20);
        dragoon.cooldown = 20;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            assertFalse(BattleCommander.shouldKite(dragoon, zealot),
                "6 tiles away is approach territory, not the 3.5-tile envelope");
        });
    }

    @Test
    public void zealotsNeverKite() {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 30, 20);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 32, 20);
        zealot.cooldown = 15;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), zealot), fakeEnemies(foe), () -> {
            assertFalse(BattleCommander.shouldKite(zealot, foe), "Zealots want contact");
            assertFalse(BattleCommander.shouldKite(null, foe));
            assertFalse(BattleCommander.shouldKite(zealot, null));
        });
    }

    private static java.util.List<tests.fakes.FakeOrderSink.Record> ordersOf(FakeUnit unit) {
        return ((tests.fakes.FakeOrderSink) unit.orderSink()).orders();
    }

    private static boolean hasOrder(java.util.List<tests.fakes.FakeOrderSink.Record> orders,
                                    int unitId, String operation) {
        for (tests.fakes.FakeOrderSink.Record record : orders) {
            if (record.actorId == unitId && record.operation.equals(operation)) return true;
        }
        return false;
    }

    @Test
    public void fleeingWithoutAnchorKitesInsteadOfIdling() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        dragoon.cooldown = 15;

        // No Nexus: defendAnchor() is null, so FLEE has nowhere to go.
        world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            atlantis.battle.BattleCommander.act(
                dragoon, atlantis.battle.FightOrFlight.Tactic.FLEE, null, dragoon);

            assertTrue(
                hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                "nowhere to run to must still move (kite), never DoNothing: "
                    + ordersOf(dragoon));
            assertFalse(
                hasOrder(ordersOf(dragoon), dragoon.id(), "attackUnit"),
                "a cooling Dragoon steps back instead of trading: " + ordersOf(dragoon));
        });
    }

    @Test
    public void fleeingWithAnchorRunsThere() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        dragoon.cooldown = 15;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            atlantis.battle.BattleCommander.act(
                dragoon, atlantis.battle.FightOrFlight.Tactic.FLEE, null, null);

            assertTrue(
                hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                "a real anchor still means run: " + ordersOf(dragoon));
        });
    }

    @Test
    public void midAnimationShotIsNeverInterrupted() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        dragoon.cooldown = 25;

        // Baseless world, FLEE tactic, close cooling threat.
        world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            // A shot already in flight: attack first (sets lastCommand
            // exactly like the real order path does), arm the animation
            // flag, then forget the setup order and ask for the next act.
            dragoon.attackUnit(zealot);
            dragoon.attackFrame = true;
            ((tests.fakes.FakeOrderSink) dragoon.orderSink()).clear();
            if (!dragoon.isAttackFrame()) return;

            atlantis.battle.BattleCommander.act(
                dragoon, atlantis.battle.FightOrFlight.Tactic.FLEE, null, dragoon);

            assertTrue(ordersOf(dragoon).isEmpty(),
                "mid-animation: hold the shot, issue nothing");
        });
    }

    @Test
    public void readyDragoonAttacksInsteadOfKiting() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        dragoon.cooldown = 5;

        world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            atlantis.battle.BattleCommander.act(
                dragoon, atlantis.battle.FightOrFlight.Tactic.FIGHT, null, dragoon);

            assertTrue(
                hasOrder(ordersOf(dragoon), dragoon.id(), "attackUnit"),
                "weapon almost ready: shoot, do not step back: " + ordersOf(dragoon));
        });
    }
}
