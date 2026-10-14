package tests.unit;

import atlantis.map.scout.ScoutState;
import atlantis.map.scout.enemy.ScoutApproachEnemyBase;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * The enemy-base approach is the ultimate fallback: it fires only while at
 * most two scouts died. Beyond that the scout stays with the rest of the
 * chain instead of feeding kills.
 */
public class ScoutApproachEnemyBaseTest {

    @AfterEach
    public void resetKillCounter() {
        ScoutState.scoutsKilledCount = 0;
    }

    @Test
    public void noApproachAfterThreeDeadScouts() {
        ScoutState.scoutsKilledCount = 3;
        FakeUnit scout = fake(AUnitType.Protoss_Probe, 10);

        assertFalse(
            new ScoutApproachEnemyBase(scout).applies(),
            "the kill gate is checked first, so this holds with no map state at all");
    }
}
