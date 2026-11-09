package tests.unit;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.behaviors.protoss.corsair.CorsairSoloFlight;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Solo-Corsair driving (owner #201, root cause traced 2026-11-01):
 * Corsairs that never join Alpha used to get no orders and no
 * observe() - invisible in every twin (3 warps in 115405, zero
 * telemetry/death/attack rows). drive() observes them and runs the
 * hunt; the regroup march stays out (solo means solo).
 */
@Tag("combat")
public class CorsairSoloFlightTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @BeforeEach
    public void clean() {
        EventLog.clearForTests();
        atlantis.combat.BattleCommander.clearForTests();
    }

    @AfterEach
    public void cleanAfter() {
        EventLog.clearForTests();
        atlantis.combat.BattleCommander.clearForTests();
        for (atlantis.units.AUnit u : Alpha.get().selection().list()) {
            Alpha.get().removeUnit(u);
        }
    }

    @Test
    public void soloCorsairIsObservedAndHuntsOverlord() {
        enemyRaceInWorld = Race.Zerg;

        FakeUnit corsair = fake(AUnitType.Protoss_Corsair, 30, 30);
        FakeUnit overlord = fake(AUnitType.Zerg_Overlord, 32, 30);
        overlord.setEnemy();

        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            corsair
        ), fakeEnemies(overlord), () -> {
            if (atlantis.game.A.now() != 2) return;

            Alpha.gather();
            CorsairSoloFlight.drive();

            String log = EventLog.toJsonLines();
            assertTrue(log.contains("\"type\":\"Corsair\""),
                "the solo flight is twinned, not invisible: " + log);
            assertTrue(corsair.lastCommand.equals("Move")
                || corsair.lastCommand.equals("AttackUnit"),
                "the hunt drives the Corsair at the Overlord, it does not sit: "
                    + corsair.lastCommand);
        });
    }

    @Test
    public void soloCorsairWithoutTargetIsObservedButUndriven() {
        enemyRaceInWorld = Race.Zerg;

        FakeUnit corsair = fake(AUnitType.Protoss_Corsair, 30, 30);

        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            corsair
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            Alpha.gather();
            CorsairSoloFlight.drive();

            String log = EventLog.toJsonLines();
            assertTrue(log.contains("\"type\":\"Corsair\""),
                "observed even with nothing to hunt - the trace gap closes either way: " + log);
        });
    }
}
