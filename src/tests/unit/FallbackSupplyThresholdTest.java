package tests.unit;

import atlantis.production.dynamic.supply.ProduceFallbackPylonWhenSupplyLow;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Fallback Pylons go out EARLY, not at the cap (owner ask: much earlier,
 * like the old ASupplyManager's 2/4/5/8/10/14/19 bands - the interim table
 * capped games the old one rode through).
 */
public class FallbackSupplyThresholdTest extends WorldStubForTests {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @Test
    public void holdsPylonWhenPlentyFree() {
        initSupply(30, 40);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            assertFalse(
                new ProduceFallbackPylonWhenSupplyLow().shouldProduce(),
                "10 free at 30/40 is plenty - ordering now would be spam");
        });
    }

    @Test
    public void ordersPylonWhenFreeSupplyFallsToEight() {
        initSupply(38, 40);

        world(20, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            // shouldProduce acts only on its own frame (every 19th).
            if (atlantis.game.A.now() != 19) return;

            assertTrue(
                new ProduceFallbackPylonWhenSupplyLow().shouldProduce(),
                "2 free at 38/40 in the 46-band (trigger 8): the Pylon must go"
                    + " out BEFORE the cap, not at it");
        });
    }
}
