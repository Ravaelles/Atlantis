package tests.acceptance;

import atlantis.game.A;
import atlantis.util.GameClock;
import org.junit.jupiter.api.Test;

import static atlantis.units.AUnitType.Terran_Marine;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The two views of the frame counters must never disagree (GLM review 2026-10-04,
 * F-1).
 *
 * <p>{@code A.s} / {@code A.now} are the legacy public fields that four production
 * classes still read; {@link GameClock} is what the kernel reads, because
 * {@code atlantis.util} may not depend on {@code atlantis.game}. The acceptance tier
 * wrote the fields itself in {@code onFrameStart} instead of going through
 * {@code A.setNow}, so it was one edit away from the two views disagreeing - which is
 * what {@link #setNowMovesBothViewsTogether()} pins, and what
 * {@link #theKernelClockIsOnTheFrameTheWorldIsOn()} checks every frame of a world.</p>
 *
 * <p>Measured, for the record: the drift the review predicted does <em>not</em>
 * reproduce. The loop is {@code onFrameStart(f) -> onFrameEnd(f)}, and
 * {@code onFrameEnd} publishes frame {@code f} through {@code useFakeTime} before it
 * runs the frame body, so no reader ever saw a stale frame. That is luck of the
 * ordering, not a design, which is why the two views now have one writer.</p>
 */
public class GameClockAgreementTest extends WorldStubForTests {

    @Test
    public void theKernelClockIsOnTheFrameTheWorldIsOn() {
        world(3, units(fake(Terran_Marine, 10)), fakeEnemies(), () -> {
            assertEquals(A.now(), GameClock.frames(),
                "GameClock is one frame behind A.now() - a writer bypassed A.setNow");
            assertEquals(A.s, GameClock.seconds(),
                "GameClock seconds disagree with A.s - a writer bypassed A.setNow");
        });
    }

    @Test
    public void setNowMovesBothViewsTogether() {
        int framesBefore = A.now;
        int secondsBefore = A.s;

        try {
            A.setNow(123, 4);

            assertEquals(123, A.now);
            assertEquals(4, A.s);
            assertEquals(123, GameClock.frames(), "the kernel view moved with the fields");
            assertEquals(4, GameClock.seconds(), "the kernel view moved with the fields");
        }
        finally {
            A.setNow(framesBefore, secondsBefore);
        }
    }

    @Test
    public void everyNthFrameAgreesWithThePublishedClock() {
        world(4, units(fake(Terran_Marine, 10)), fakeEnemies(), () -> {
            assertEquals(A.everyNthGameFrame(2), GameClock.everyNthFrame(2),
                "the throttle the kernel uses and the one A exposes must be the same question");
        });
    }
}