package tests.acceptance;

import atlantis.units.AUnitType;
import atlantis.units.workers.gather.GatherResources;
import atlantis.units.workers.IdleWorker;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * A worker the engine still reports as not completed (training in the Nexus)
 * cannot take any order - JBWAPI {@code canCommand} is false until completion.
 *
 * <p>Measured on OpenBW (NEXT #63): one such Probe stood from frame 30 to the
 * end of the game with {@code lastCommand=NO_COMMAND}, while our own
 * GATHER_MINERALS action flag claimed it was gathering. Both mining managers
 * must decline it instead of burning refused orders and poisoning the flag.</p>
 */
public class UncompletedWorkerIsLeftAloneTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void uncompletedWorkerIsDeclinedByBothMiningManagers() {
        FakeUnit trainee = fake(AUnitType.Protoss_Probe, 10);
        trainee.setCompleted(false);

        world(1, units(trainee), units(), () -> {
            assertFalse(
                new GatherResources(trainee).applies(),
                "an uncompleted worker cannot gather - every order is refused"
            );
            assertFalse(
                new IdleWorker(trainee).applies(),
                "the idle fallback must not claim an uncompleted worker either"
            );
        });
    }
}
