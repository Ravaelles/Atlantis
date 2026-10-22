package tests.acceptance;

import atlantis.production.constructions.commanders.CancelTooLongConstructions;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Overdue requeue must not duplicate covered tech.
 *
 * <p><b>Measured (OpenBW `20261016_162455_long`):</b> three standing
 * Cybernetics Cores - every stale duplicate's overdue death re-ordered
 * while a sibling stood or warped. The truly lost case (nothing
 * standing, nothing warping) still requeues; multi-want types never
 * take this exit.</p>
 */
public class TechCoveredSkipsRequeueTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void standingTechSkipsRequeue() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 7, 44),
            fake(AUnitType.Protoss_Cybernetics_Core, 20, 20)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            assertTrue(
                CancelTooLongConstructions.techAlreadyCovered(AUnitType.Protoss_Cybernetics_Core),
                "a standing Core covers the demand - re-ordering builds a duplicate");
        });
    }

    @Test
    public void warpingTechSkipsRequeue() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 7, 44),
            warpingCore()
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            assertTrue(
                CancelTooLongConstructions.techAlreadyCovered(AUnitType.Protoss_Cybernetics_Core),
                "a warping Core covers the demand too - it completes or the pump re-orders");
        });
    }

    private static FakeUnit warpingCore() {
        FakeUnit warping = fake(AUnitType.Protoss_Cybernetics_Core, 20, 20);
        warping.setCompleted(false);

        return warping;
    }

    @Test
    public void lostTechAndMultiWantProceed() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 7, 44),
            fake(AUnitType.Protoss_Probe, 8, 44)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            assertFalse(
                CancelTooLongConstructions.techAlreadyCovered(AUnitType.Protoss_Cybernetics_Core),
                "nothing standing, nothing warping - the truly lost case still requeues");

            assertFalse(
                CancelTooLongConstructions.techAlreadyCovered(AUnitType.Protoss_Pylon),
                "multi-want types never take this exit, Pylon blocks included");
        });
    }

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }
}
