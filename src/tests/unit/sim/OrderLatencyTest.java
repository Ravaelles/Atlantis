package tests.unit.sim;

import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;
import tests.unit.AbstractTestWithUnits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Order-issue latency (NEXT #231, third Phase 6C gap): live the engine's
 * answer does not move units the tick it is issued (the once-per-minute
 * stack traces the sim never reproduces); the sim applied everything
 * same-tick. A scenario opts into a fixed-frame delay; default zero
 * keeps every existing scenario immediate.
 */
@Tag("combat")
@Tag("full-tier")
public class OrderLatencyTest extends AbstractTestWithUnits {

    private static final int TILE = 32;

    private static FakeUnit dragoon(double tx, double ty) {
        return fake(AUnitType.Protoss_Dragoon, tx, ty);
    }

    @Test
    public void issuedMoveWaitsOutTheLatency() {
        FakeUnit dragoon = dragoon(10, 10);
        SimWorld world = new SimWorld(
            fakeOurs(dragoon), fakeEnemies(), new SimArena(40 * TILE, 40 * TILE));
        try {
            world.setOrderLatencyFrames(3);

            double startX = dragoon.position.x();

            for (int i = 0; i < 2; i++) {
                world.tick(() -> dragoon.move(APosition.create(30, 10), Actions.MOVE_ENGAGE, "t", false));
                assertEquals(startX, dragoon.position.x(), 0.5,
                    "frames 1-2 after issue the answer has not landed yet");
            }

            for (int i = 0; i < 12; i++) {
                world.tick(() -> dragoon.move(APosition.create(30, 10), Actions.MOVE_ENGAGE, "t", false));
            }

            assertTrue(dragoon.position.x() > startX + TILE,
                "from frame 3 the unit marches: " + dragoon.position.x());
        }
        finally {
            world.close();
        }
    }

    @Test
    public void zeroLatencyMovesSameTick() {
        FakeUnit dragoon = dragoon(10, 10);
        SimWorld world = new SimWorld(
            fakeOurs(dragoon), fakeEnemies(), new SimArena(40 * TILE, 40 * TILE));
        try {
            double startX = dragoon.position.x();

            world.tick(() -> dragoon.move(APosition.create(30, 10), Actions.MOVE_ENGAGE, "t", false));
            world.tick(() -> dragoon.move(APosition.create(30, 10), Actions.MOVE_ENGAGE, "t", false));

            assertTrue(dragoon.position.x() > startX,
                "default zero latency keeps immediate response");
        }
        finally {
            world.close();
        }
    }

    @Test
    public void changedOrderRearmsTheWait() {
        FakeUnit dragoon = dragoon(10, 10);
        SimWorld world = new SimWorld(
            fakeOurs(dragoon), fakeEnemies(), new SimArena(40 * TILE, 40 * TILE));
        try {
            world.setOrderLatencyFrames(3);

            world.tick(() -> dragoon.move(APosition.create(30, 10), Actions.MOVE_ENGAGE, "t", false));
            world.tick(() -> dragoon.move(APosition.create(30, 10), Actions.MOVE_ENGAGE, "t", false));
            world.tick(() -> dragoon.move(APosition.create(30, 10), Actions.MOVE_ENGAGE, "t", false));
            double beforeTurn = dragoon.position.x();

            // A new order re-arms: the turn waits its own 3 frames even
            // though the march is underway.
            world.tick(() -> dragoon.move(APosition.create(10, 30), Actions.MOVE_ENGAGE, "t", false));
            assertEquals(beforeTurn, dragoon.position.x(), 1.5,
                "the reversed order does not bite same-tick");
        }
        finally {
            world.close();
        }
    }
}
