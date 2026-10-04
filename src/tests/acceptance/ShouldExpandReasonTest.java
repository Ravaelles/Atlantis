package tests.acceptance;

import atlantis.production.dynamic.expansion.decision.ShouldExpand;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import bwapi.Race;

import static atlantis.units.AUnitType.Protoss_Gateway;
import static atlantis.units.AUnitType.Protoss_Nexus;
import static atlantis.units.AUnitType.Protoss_Probe;
import static atlantis.units.AUnitType.Terran_Marine;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Every way out of the expansion decision has to leave a reason behind.
 *
 * <p>The reason is not decoration: it is what the queue log prints next to a queued base
 * ("Nexus ADDED TO QUEUE, min=778/ sup=13 / LimitedBases"), which is how B-22's
 * expansion chain was read out of a game. Two exits used to return without setting it -
 * the Protoss "we are still probing minerals" gate and the unknown-race fallback - so
 * the log carried whatever the previous frame decided, for as long as the bot was
 * saving. That is the same defect class as B-22, where twelve gates answered
 * {@code false} and none of them said which.
 */
public class ShouldExpandReasonTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void savingForABaseSaysSoInsteadOfRepeatingTheLastFrame() {
        currentMinerals = 120;

        world(2, ourWorld(), enemies(), () -> {
            assertFalse(ShouldExpand.shouldExpand(), "120 minerals is not an expansion");

            assertEquals("NotEnoughMinerals", ShouldExpand.reason,
                "a 'no' with no reason leaves the previous frame's string standing in the log");
        });
    }

    @Test
    public void everyAnswerCarriesTheRuleThatGaveIt() {
        currentMinerals = 800;

        world(2, ourWorld(), enemies(), () -> {
            boolean expand = ShouldExpand.shouldExpand();

            if (expand) {
                assertEquals("LimitedBases", ShouldExpand.reason,
                    "800 minerals with one base planned is the LimitedBases shortcut, and a yes "
                        + "that does not say which rule said it is the defect this test is about");
            }
            else {
                assertNotEquals("_NO_EXPAND_REASON_", ShouldExpand.reason,
                    "a 'no' must name the gate, not leave the previous frame's string standing");
            }
        });
    }

    private FakeUnit[] ourWorld() {
        return fakeOurs(
            fake(Protoss_Nexus, 10, 10),
            fake(Protoss_Gateway, 11, 10),
            fake(Protoss_Probe, 12, 10)
        );
    }

    private FakeUnit[] enemies() {
        return fakeEnemies(fakeEnemy(Terran_Marine, 60, 10));
    }
}