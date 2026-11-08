package tests.unit;

import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.NewConstructionRequest;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Single-door Pylon cap (live: two Pylons minted out of one 25/25 supply
 * block - a V2 supply Pylon plus a legacy-door one, each firing alone
 * correctly and doubling together while a Gateway builder spun on an
 * occupied site). The V2 mint door refuses a second unfinished Pylon
 * below 40 supply; the legacy door ({@code NewConstructionRequest},
 * reached via CancelTooLong re-requests, sited requests and wall
 * pieces) must refuse the same way instead of minting behind its back.
 *
 * <p>Stub-state pins (no map, no game): a live V2 row plus one legacy
 * request, and the request comes back empty.</p>
 */
@Tag("production")
public class PylonConcurrencyCapSimTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
    }

    @Test
    public void legacyDoorRefusesSecondPylonWhileOneIsInFlight() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            probe,
            fake(AUnitType.Protoss_Probe, 22, 22)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 1) return;
            currentMinerals = 100;

            // The V2 door already minted this supply block's Pylon.
            Construction v2row = new Construction(AUnitType.Protoss_Pylon);
            v2row.setPositionToBuild(atlantis.map.position.APosition.create(10, 10));
            ConstructionRequests.addConstruction(v2row);

            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
            assertNull(
                NewConstructionRequest.requestConstructionOf(
                    AUnitType.Protoss_Pylon, null, order),
                "one unfinished Pylon below 40 supply is the cap - the legacy"
                    + " door must refuse, not mint a second one behind V2's back");
        });
    }

    @Test
    public void builderlessInProgressPylonStillCounts() {
        // Live 2026-11-07 (EC1669F3, GAME_761B4B7F): a Pylon adopted then
        // builder lost sits IN_PROGRESS with no linked building - neither
        // warping (no unit) nor not-started - and a second Pylon minted
        // behind it at 24/25 with ~200 bank, delaying the Core past the
        // Dragoon timing. The cap must see the adopted-but-unitless row.
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            probe,
            fake(AUnitType.Protoss_Probe, 22, 22)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 1) return;
            currentMinerals = 150;

            Construction adopted = new Construction(AUnitType.Protoss_Pylon);
            adopted.setPositionToBuild(atlantis.map.position.APosition.create(10, 10));
            adopted.setStatus(
                atlantis.production.constructions.ConstructionOrderStatus.IN_PROGRESS);
            ConstructionRequests.addConstruction(adopted);

            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
            assertNull(
                NewConstructionRequest.requestConstructionOf(
                    AUnitType.Protoss_Pylon, null, order),
                "an adopted Pylon with no building yet is still in flight - "
                    + "no second Pylon below 40 supply on a poor bank");
        });
    }

    @Test
    public void richBankStillAllowsThePair() {
        // Owner rule: above 300 minerals Pylon + Core stay affordable
        // together, so the cap gates concurrency, never the demand.
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            probe,
            fake(AUnitType.Protoss_Probe, 22, 22)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 1) return;
            currentMinerals = 400;

            Construction adopted = new Construction(AUnitType.Protoss_Pylon);
            adopted.setPositionToBuild(atlantis.map.position.APosition.create(10, 10));
            adopted.setStatus(
                atlantis.production.constructions.ConstructionOrderStatus.IN_PROGRESS);
            ConstructionRequests.addConstruction(adopted);

            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
            assertNotNull(
                NewConstructionRequest.requestConstructionOf(
                    AUnitType.Protoss_Pylon, null, order),
                "rich bank: the second Pylon is affordable next to the Core");
        });
    }

    @Test
    public void legacyDoorMintsWhenNothingIsInFlight() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            probe,
            fake(AUnitType.Protoss_Probe, 22, 22)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 1) return;

            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
            assertNotNull(
                NewConstructionRequest.requestConstructionOf(
                    AUnitType.Protoss_Pylon, null, order),
                "with no Pylon in flight the legacy door keeps minting - "
                    + "the cap gates concurrency, not demand");
        });
    }
}
