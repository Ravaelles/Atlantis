package tests.unit;

import atlantis.production.dynamic.protoss.buildings.ProduceGateway;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Game 162906: one Gateway all game (single ADDED at 3:44), bank 100-603,
 * army stalled at 5 units. Replicates that mid-state: 1 idle Gateway,
 * 400 bank, Core standing, supply 35, Zerg enemy. The second Gateway must
 * be requested - no silent refusal.
 */
@Tag("production")
public class SecondGatewayQueuedTest extends AbstractTestWithWorld {

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
    public void secondGatewayRequestedWhenOneIdlesAndBankGrows() {
        currentMinerals = 400;
        currentSupplyUsed = 35;
        currentSupplyTotal = 49;

        world(20, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 24, 20),
            fake(AUnitType.Protoss_Gateway, 28, 20),
            fake(AUnitType.Protoss_Cybernetics_Core, 30, 20),
            fake(AUnitType.Protoss_Zealot, 32, 20),
            fake(AUnitType.Protoss_Zealot, 33, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(fake(AUnitType.Zerg_Zergling, 60, 20)), () -> {
            // Producer runs on its own cadence, not every frame.
            if (atlantis.game.A.now() != 17) return;

            assertTrue(
                ProduceGateway.produce(),
                "1 idle Gateway + 400 bank + Core standing must queue Gateway #2");
        });
    }
}
