package tests.acceptance;

import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.dynamic.protoss.buildings.ProduceCyberneticsCore;
import atlantis.production.orders.production.queue.Queue;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Owner 2026-10-18: one Cybernetics Core per game, never two coming.
 * Measured (OpenBW 225045): a dynamic Core row warped 16 s after the BO
 * Core - the in-flight guards (completed / inProduction / queue-top-3)
 * are all blind to a picked-early PREPARING site, so a rich bank
 * re-ordered what was already coming.
 */
public class SecondCoreBlockedWhileOneComingTest extends WorldStubForTests {

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

    @Test
    public void preparingCoreBlocksDynamicSecond() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 24, 20),
            fake(AUnitType.Protoss_Gateway, 28, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3 || fired) return;

            fired = true;
            ConstructionRequests.constructions.clear();
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            currentMinerals = 600;
            currentSupplyUsed = 55;
            currentSupplyTotal = 60;

            // Picked-early site, no builder yet - invisible to every
            // in-flight guard the producer has.
            Construction site = new Construction(AUnitType.Protoss_Cybernetics_Core);
            site.setStatus(atlantis.production.constructions.ConstructionOrderStatus.PREPARING);
            ConstructionRequests.constructions.add(site);

            assertFalse(
                ProduceCyberneticsCore.produce(),
                "a Core is coming (PREPARING site) - no dynamic second");
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
