package tests.unit;

import atlantis.game.A;
import atlantis.units.AUnitType;
import atlantis.units.BaseUnderAttack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NEXT.md #36: BaseUnderAttack needs a seam (cached check or injectable answer).
 */
@Tag("map")
public class BaseUnderAttackTest extends AbstractTestWithWorld {

    @AfterEach
    public void clearSeam() {
        BaseUnderAttack.clearForTests();
    }

    @Test
    public void noBaseMeansNoAttack() {
        world(1, fakeOurs(fake(AUnitType.Protoss_Probe, 10)), fakeEnemies(), () -> {
            BaseUnderAttack.clearForTests();
            assertFalse(BaseUnderAttack.check());
        });
    }

    @Test
    public void forcedAnswerOverridesLiveReading() {
        world(1, fakeOurs(fake(AUnitType.Protoss_Probe, 10)), fakeEnemies(), () -> {
            BaseUnderAttack.clearForTests();
            BaseUnderAttack.setForcedAnswerForTests(true);
            assertTrue(BaseUnderAttack.check());
            BaseUnderAttack.setForcedAnswerForTests(false);
            assertFalse(BaseUnderAttack.check());
            BaseUnderAttack.setForcedAnswerForTests(null);
            assertFalse(BaseUnderAttack.check());
        });
    }

    @Test
    public void cacheInvalidatesOnNewFrame() {
        world(1, fakeOurs(fake(AUnitType.Protoss_Probe, 10)), fakeEnemies(), () -> {
            BaseUnderAttack.clearForTests();
            assertFalse(BaseUnderAttack.check());
            A.setNow(A.now() + 1, A.now() / 30);
            assertFalse(BaseUnderAttack.check());
        });
    }

    @Override
    protected FakeUnit[] generateOur() {
        return fakeOurs();
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return fakeEnemies();
    }
}
