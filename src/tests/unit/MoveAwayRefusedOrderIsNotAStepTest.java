package tests.unit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import atlantis.units.AUnitType;
import atlantis.units.actions.Action;
import atlantis.units.actions.Actions;
import atlantis.units.actions.move.MoveAway;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeOrderSink;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * A refused order is not a step (NEXT #173 lesson, Wine 1Drag_v_1Zeal:
 * steps issued every frame while the unit stood still - the caller must
 * try the next candidate instead of reporting success). {@code MoveAway.from}
 * is first-success: when the engine refuses every aim, it must answer false
 * so the caller falls through (moveToMain, fight, ...) instead of standing
 * still under a "success".
 */
@Tag("combat")
public class MoveAwayRefusedOrderIsNotAStepTest extends WorldStubForTests {

    @Test
    public void refusedOrderOnEveryAimIsNotAStep() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 30, 20);
        FakeUnit zealot = fake(AUnitType.Zerg_Zergling, 32, 20);
        zealot.setEnemy();

        FakeOrderSink refusing = new FakeOrderSink();
        refusing.resultToReturn(false);
        probe.setOrderSink(refusing);

        world(1, fakeOurs(probe), fakeEnemies(zealot), () -> {
            assertFalse(
                MoveAway.from(probe, zealot, 2, Actions.MOVE_AVOID, "t"),
                "every aim refused by the engine: no step happened, report false so the caller tries its fallback"
            );
        });
    }

    @Test
    public void acceptedOrderStillSteps() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 30, 20);
        FakeUnit zealot = fake(AUnitType.Zerg_Zergling, 32, 20);
        zealot.setEnemy();

        world(1, fakeOurs(probe), fakeEnemies(zealot), () -> {
            assertTrue(
                MoveAway.from(probe, zealot, 2, Actions.MOVE_AVOID, "t"),
                "open field, engine accepts: the escape step issues as before"
            );
        });
    }
}
