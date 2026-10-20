package tests.unit;

import atlantis.production.dynamic.protoss.tech.ResearchSingularityCharge;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * NEXT #72: zero research lines in three gas-healthy games (gas 274-296,
 * Core standing, 2-3 Dragoons). Replicates that state: with 2 Dragoons and
 * 250 gas the researcher must fire (gas 80+180 rule).
 */
@Tag("production")
public class SingularityChargeFiresWhenGasHealthyTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void firesWithTwoDragoonsAndGas() {
        currentMinerals = 300;
        currentGas = 250;
        currentSupplyUsed = 40;
        currentSupplyTotal = 60;

        world(20, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Cybernetics_Core, 24, 20),
            fake(AUnitType.Protoss_Dragoon, 30, 20),
            fake(AUnitType.Protoss_Dragoon, 31, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 17) return;

            assertTrue(
                new ResearchSingularityCharge().applies(),
                "2 Dragoons + 250 gas + Core standing must research Singularity Charge");
        });
    }
}
