package tests.acceptance;

import atlantis.production.GoalBoardAdvisor;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.dynamic.protoss.buildings.ProduceStargate;
import atlantis.production.dynamic.protoss.tech.ResearchSingularityCharge;
import atlantis.production.orders.production.queue.Queue;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NEXT #93 absorption slices 3-5: the Stargate Protoss/Zerg branches and
 * the Singularity trio read posture off the board snapshot. Same numbers,
 * new home - these tests pin the behavior through the public methods.
 */
public class AbsorbedBranchesBoardTest extends WorldStubForTests {

    private boolean fired = false;

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
        Queue.get().allOrders().list().clear();
        Queue.get().clearCache();
        fired = false;
    }

    private FakeUnit[] dragoons(int n) {
        FakeUnit[] units = new FakeUnit[n + 3];
        units[0] = fake(AUnitType.Protoss_Nexus, 20, 20);
        units[1] = fake(AUnitType.Protoss_Pylon, 24, 20);
        units[2] = fake(AUnitType.Protoss_Probe, 21, 21);
        for (int i = 0; i < n; i++) {
            units[3 + i] = fake(AUnitType.Protoss_Dragoon, 30 + i, 20);
        }
        return units;
    }

    @Test
    public void fewDragoonsForbidStargateVsProtoss() {
        world(5, fakeOurs(dragoons(5)), fakeEnemies(
            fake(AUnitType.Protoss_Zealot, 60, 20)
        ), () -> {
            if (atlantis.game.A.now() != 3 || fired) return;

            fired = true;
            enemyRaceInWorld = Race.Protoss;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            currentMinerals = 400;
            currentSupplyUsed = 40;
            currentSupplyTotal = 50;

            ProduceStargate.produce();

            assertTrue(
                Queue.get().allOrders().ofType(AUnitType.Protoss_Stargate).isEmpty(),
                "5 Dragoons vs Protoss: Stargate forbidden, nothing queued");
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
    }

    @Test
    public void carriersOpenStargateVsProtoss() {
        world(5, fakeOurs(dragoons(13)), fakeEnemies(
            fake(AUnitType.Protoss_Carrier, 60, 20)
        ), () -> {
            if (atlantis.game.A.now() != 3 || fired) return;

            fired = true;
            enemyRaceInWorld = Race.Protoss;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            currentMinerals = 400;
            currentSupplyUsed = 40;
            currentSupplyTotal = 50;

            assertTrue(
                GoalBoardAdvisor.snapshot().enemyCarrierTech,
                "a discovered Carrier must read as carrier pressure");

            ProduceStargate.produce();

            assertFalse(
                Queue.get().allOrders().ofType(AUnitType.Protoss_Stargate).isEmpty(),
                "13 Dragoons + Carriers vs Protoss: Stargate allowed, row queued");
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
    }

    @Test
    public void cannonsAndDragoonResearchSingularity() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Cybernetics_Core, 24, 20),
            fake(AUnitType.Protoss_Photon_Cannon, 25, 20),
            fake(AUnitType.Protoss_Photon_Cannon, 26, 20),
            fake(AUnitType.Protoss_Dragoon, 30, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3 || fired) return;

            fired = true;

            currentMinerals = 300;
            currentGas = 250;
            currentSupplyUsed = 40;
            currentSupplyTotal = 60;

            assertTrue(
                new ResearchSingularityCharge().applies(),
                "2 cannons + Dragoon (forge-expand): Singularity must fire");
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
    }

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }
}
