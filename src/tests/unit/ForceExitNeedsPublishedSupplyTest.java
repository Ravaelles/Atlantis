package tests.unit;

import atlantis.config.env.Env;
import atlantis.information.decisions.ForceExitWhenInvalidState;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * M&M UMS, 2026-10-20: the bot attached, analyzed the map, printed
 * HELLO - and suicided at frame 2. The tripwire read supply 0/0 as
 * "no army" on a map with a standing 30-unit army and bank 10000:
 * the engine had published no supply at all, which is absent data,
 * not an empty army. Only a PUBLISHED zero may trip the exit.
 */
@Tag("production")
public class ForceExitNeedsPublishedSupplyTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @AfterEach
    public void resetEnv() {
        Env.setLocalForTest(false);
    }

    @Test
    public void unpublishedSupplyNeverTripsTheExit() {
        currentMinerals = 10000;
        currentSupplyUsed = 0;
        currentSupplyTotal = 0;
        Env.setLocalForTest(true);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Dragoon, 20, 20)
        ), fakeEnemies(), () -> {
            assertFalse(
                new ForceExitWhenInvalidState().applies(),
                "0/0 published supply with bank 10000 is absent data (M&M), not a stuck game");
        });
    }

    @Test
    public void publishedHoardStillTripsTheExit() {
        currentMinerals = 1500;
        currentSupplyUsed = 4;
        currentSupplyTotal = 40;
        Env.setLocalForTest(true);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Dragoon, 20, 20)
        ), fakeEnemies(), () -> {
            assertTrue(
                new ForceExitWhenInvalidState().applies(),
                "banked 1500 at 4/40 supply is still the stuck shape the guard exists for");
        });
    }
}
