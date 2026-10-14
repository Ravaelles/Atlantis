package tests.unit;

import atlantis.production.dynamic.protoss.ProtossDynamicBuildingsCommander;
import atlantis.production.dynamic.protoss.buildings.ProduceCyberneticsCore;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;
import static tests.fakes.FakeUnitHelper.merge;

/**
 * NEXT #58 follow-up (game 152437): after a cancelled Core, zero Core lines
 * (ADDED/NOT_READY/QUEUE_BASE) appeared for 200+ seconds while the bank
 * swung 100-440 - either the buildings commander never ran or the Core
 * producer never said yes. Replicates that late-game state: 48 supply,
 * 300 bank, 10 Zealots, 2 Gateways, 5 Pylons, no Core, Zerg enemy.
 */
public class CoreRequeueAfterCancelTest extends AbstractTestWithWorld {

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
    public void coreProducerSaysYesWhenCoreMissing() {
        currentMinerals = 300;
        currentSupplyUsed = 48;
        currentSupplyTotal = 60;

        FakeUnit[] zealots = new FakeUnit[10];
        for (int i = 0; i < 10; i++) zealots[i] = fake(AUnitType.Protoss_Zealot, 30 + i, 20);

        world(20, merge(
            fakeOurs(
                fake(AUnitType.Protoss_Nexus, 20, 20),
                fake(AUnitType.Protoss_Pylon, 24, 20),
                fake(AUnitType.Protoss_Pylon, 26, 20),
                fake(AUnitType.Protoss_Gateway, 28, 20),
                fake(AUnitType.Protoss_Gateway, 30, 20)
            ),
            zealots
        ), fakeEnemies(fake(AUnitType.Zerg_Zergling, 60, 20)), () -> {
            // The commander is throttled to every 17th frame - assert there.
            if (atlantis.game.A.now() != 17) return;

            assertTrue(
                new ProtossDynamicBuildingsCommander().applies(),
                "commander must run in this state");
            assertTrue(
                ProduceCyberneticsCore.produce(),
                "Core missing, bank 300, 10 Zealots vs Zerg - producer must say yes");
        });
    }
}
