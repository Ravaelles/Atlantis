package tests.unit;

import atlantis.combat.BattleCommander;
import atlantis.combat.FightTarget;
import atlantis.combat.strategy.BattleStrategy;
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

/**
 * A refused kite order is not "nowhere to step" (NEXT #188 follow-up,
 * fight side): validation passed, the engine said no. The twin names
 * the BWAPI state (canCmd/canMove) at refuse time so the coming
 * unstick/run-escalation decision reads data, not forensics.
 */
@Tag("combat")
public class KiteRefusedOrderTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @BeforeEach
    public void cleanLog() {
        EventLog.clearForTests();
        BattleCommander.clearForTests();
    }

    @AfterEach
    public void cleanLogAfter() {
        EventLog.clearForTests();
        BattleCommander.clearForTests();
    }

    @Test
    public void refusedKiteOrderRowsRefusalState() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        zealot.setEnemy();
        dragoon.cooldown = 10;
        dragoon.canCommand = false;
        dragoon.canMove = false;
        ((FakeOrderSink) dragoon.orderSink()).resultToReturn(false);

        world(5, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            if (atlantis.game.A.now() != 3) return;

            FightTarget.fight(dragoon, zealot, BattleStrategy.OPEN_FIELD, null, null);
            String rows = EventLog.toJsonLines();
            assertTrue(rows.contains("\"result\":\"stuck-order-refused\""),
                "a validated step the engine refuses is not nowhere-to-step: " + rows);
            assertTrue(rows.contains("\"canCmd\":false") && rows.contains("\"canMove\":false"),
                "the refusal names the BWAPI state at refuse time: " + rows);
            assertFalse(rows.contains("\"result\":\"stuck-no-step\""),
                "no validation failure happened here: " + rows);
        });
    }

    @Test
    public void commandableUnitRowsTrueOnRefusal() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        zealot.setEnemy();
        dragoon.cooldown = 10;
        ((FakeOrderSink) dragoon.orderSink()).resultToReturn(false);

        world(5, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            if (atlantis.game.A.now() != 3) return;

            FightTarget.fight(dragoon, zealot, BattleStrategy.OPEN_FIELD, null, null);
            String rows = EventLog.toJsonLines();
            assertTrue(rows.contains("\"result\":\"stuck-order-refused\""), "got: " + rows);
            assertTrue(rows.contains("\"canCmd\":true") && rows.contains("\"canMove\":true"),
                "commandable default reads true, same as the live reader contract: " + rows);
        });
    }
}
