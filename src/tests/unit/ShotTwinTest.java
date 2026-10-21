package tests.unit;


import atlantis.battle.twins.ShotTwin;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Every attack acquisition and every animation hold names itself,
 * so a late-game silence answers whether the guard latched or the
 * order never issued. Both throttle to one row per unit per 30
 * frames on separate clocks.
 */
@Tag("combat")
public class ShotTwinTest extends AbstractTestWithWorld {

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

    @BeforeEach
    public void cleanLog() {
        EventLog.clearForTests();
        ShotTwin.clearForTests();
    }

    @AfterEach
    public void cleanLogAfter() {
        EventLog.clearForTests();
        ShotTwin.clearForTests();
    }

    private static java.util.List<tests.fakes.FakeOrderSink.Record> ordersOf(FakeUnit unit) {
        return ((tests.fakes.FakeOrderSink) unit.orderSink()).orders();
    }

    @Test
    public void attackOrderNamesTheShot() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        dragoon.cooldown = 15;

        // Based on purpose: this pins SHOT attribution, not kiting -
        // baseless duels step off while cooling (NEXT #169).
        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            atlantis.battle.BattleCommander.act(
                dragoon, atlantis.battle.cluster.FightOrFlight.Tactic.FIGHT, null, dragoon);

            boolean attacked = false;
            for (tests.fakes.FakeOrderSink.Record record : ordersOf(dragoon)) {
                if (record.actorId == dragoon.id() && record.operation.equals("attackUnit")) attacked = true;
            }
            assertTrue(attacked, "healthy at 2 tiles: shoot, do not step: " + ordersOf(dragoon));
            assertTrue(EventLog.toJsonLines().contains("\"result\":\"shot-fired\""),
                "got: " + EventLog.toJsonLines());
        });
    }

    @Test
    public void animationHoldNamesItself() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        dragoon.cooldown = 25;

        world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            dragoon.attackUnit(zealot);
            dragoon.attackFrame = true;
            ((tests.fakes.FakeOrderSink) dragoon.orderSink()).clear();
            if (!dragoon.isAttackFrame()) return;

            atlantis.battle.BattleCommander.act(
                dragoon, atlantis.battle.cluster.FightOrFlight.Tactic.FIGHT, null, dragoon);

            assertTrue(ordersOf(dragoon).isEmpty(), "hold the shot, issue nothing");
            assertTrue(EventLog.toJsonLines().contains("\"result\":\"holding-shot\""),
                "got: " + EventLog.toJsonLines());
        });
    }

    @Test
    public void firedAlwaysRowsButHoldThrottles() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);

        world(5, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            if (atlantis.game.A.now() != 3) return;

            ShotTwin.fired(dragoon, zealot);
            ShotTwin.fired(dragoon, zealot);
            ShotTwin.holding(dragoon, zealot);
            ShotTwin.holding(dragoon, zealot);

            String rows = EventLog.toJsonLines();
            assertTrue(countRows(rows, "\"result\":\"shot-fired\"") == 2, "got: " + rows);
            assertTrue(countRows(rows, "\"result\":\"holding-shot\"") == 1, "got: " + rows);
        });
    }

    private static int countRows(String text, String needle) {
        int count = 0;
        int from = 0;
        while (true) {
            int at = text.indexOf(needle, from);
            if (at < 0) return count;
            count++;
            from = at + needle.length();
        }
    }

    @Test
    public void noTwinWithoutFight() {
        world(5, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20)), fakeEnemies(), () -> {
            assertFalse(EventLog.toJsonLines().contains("\"key\":\"SHOT\""));
        });
    }
}
