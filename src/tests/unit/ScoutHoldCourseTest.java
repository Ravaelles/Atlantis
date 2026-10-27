package tests.unit;

import atlantis.map.position.APosition;
import atlantis.map.scout.ScoutHoldCourse;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Scout course ride (owner: the early worker circled between the enemy
 * second and third base, a different manager's destination winning every
 * frame, and never saw anything). While the scout moves under a scout
 * order the movement managers stay out - only the avoid block above may
 * interrupt. Arrival, a wall push and the ride cap hand control back.
 */
@Tag("map")
public class ScoutHoldCourseTest extends WorldStubForTests {

    @Test
    public void freshOrderRides() {
        assertTrue(ScoutHoldCourse.shouldRide(true, true, 0, 0));
        assertTrue(ScoutHoldCourse.shouldRide(true, true, ScoutHoldCourse.SETTLE_FRAMES, 500));
    }

    @Test
    public void movingWithProgressAndFreshCourseRides() {
        assertTrue(ScoutHoldCourse.shouldRide(true, true, 200, 5));
    }

    @Test
    public void idleOrNonScoutNeverRides() {
        assertFalse(ScoutHoldCourse.shouldRide(true, false, 0, 0));
        assertFalse(ScoutHoldCourse.shouldRide(false, true, 0, 0));
        assertFalse(ScoutHoldCourse.shouldRide(false, false, 0, 0));
    }

    @Test
    public void wallPushHandsControlBack() {
        assertFalse(ScoutHoldCourse.shouldRide(
            true, true, 200, ScoutHoldCourse.STUCK_FRAMES + 1));
    }

    @Test
    public void staleCourseReevaluates() {
        assertFalse(ScoutHoldCourse.shouldRide(
            true, true, ScoutHoldCourse.RIDE_MAX_FRAMES + 1, 5));
    }

    @Test
    public void scoutEnRouteIsNotRetasked() {
        FakeUnit worker = fake(AUnitType.Protoss_Probe, 30, 20);

        world(2, fakeOurs(worker), fakeEnemies(), () -> {
            if (atlantis.game.A.now() == 1) {
                worker.move(APosition.create(60, 20), Actions.MOVE_SCOUT, "Explore", true);

                return;
            }
            assertTrue(new ScoutHoldCourse(worker).applies(),
                "in-flight scout course rides, no new order");
        });
    }

    @Test
    public void idleScoutIsFreeForAnOrder() {
        FakeUnit worker = fake(AUnitType.Protoss_Probe, 30, 20);

        world(1, fakeOurs(worker), fakeEnemies(), () -> {
            assertFalse(new ScoutHoldCourse(worker).applies(),
                "standing scout with no scout order takes one");
        });
    }

    @Test
    public void fleeInterruptsTheCourse() {
        FakeUnit worker = fake(AUnitType.Protoss_Probe, 30, 20);

        world(2, fakeOurs(worker), fakeEnemies(), () -> {
            if (atlantis.game.A.now() == 1) {
                worker.move(APosition.create(60, 20), Actions.MOVE_SCOUT, "Explore", true);
                worker.setAction(Actions.MOVE_AVOID);

                return;
            }
            assertFalse(new ScoutHoldCourse(worker).applies(),
                "flee action is not a scout course");
        });
    }

    @Test
    public void motionlessCourseReleasesAfterStuckTimeout() {
        FakeUnit worker = fake(AUnitType.Protoss_Probe, 30, 20);

        world(ScoutHoldCourse.STUCK_FRAMES + 25, fakeOurs(worker), fakeEnemies(), () -> {
            if (atlantis.game.A.now() == 1) {
                worker.move(APosition.create(60, 20), Actions.MOVE_SCOUT, "Explore", true);
                worker.targetPosition = null;

                return;
            }
            if (atlantis.game.A.now() != ScoutHoldCourse.STUCK_FRAMES + 25) return;

            assertFalse(new ScoutHoldCourse(worker).applies(),
                "pushing nowhere for 100+ frames re-picks instead of riding");
        });
    }
}
