package tests.unit;

import atlantis.production.constructions.builders.BuilderManager;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * A builder that cannot take orders must be replaced, not held.
 *
 * <p>NEXT #63: an assigned builder the engine reports as not completed takes
 * no order at all,measured holding a Gateway site with {@code lastCommand=NO_COMMAND}.
 * The old rule only replaced stuck builders; an uncompleted one is never
 * "stuck" (stuck detection needs unfulfilled move orders, and none can be
 * issued), so it held until CancelTooLong killed the construction.</p>
 */
public class BuilderManagerShouldReplaceTest {

    @Test
    public void uncompletedBuilderIsReplacedEvenWhenNotStuck() {
        FakeUnit trainee = fake(AUnitType.Protoss_Probe, 10);
        trainee.setCompleted(false);

        assertTrue(BuilderManager.shouldReplaceBuilder(trainee));
    }

    @Test
    public void healthyBuilderIsKept() {
        FakeUnit worker = fake(AUnitType.Protoss_Probe, 10);

        assertFalse(BuilderManager.shouldReplaceBuilder(worker));
    }
}
