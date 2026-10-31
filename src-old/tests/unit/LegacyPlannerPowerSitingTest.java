package tests.unit;

import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.v2.LegacyPlacementPlanner;
import atlantis.production.v2.PlacementReservation;
import atlantis.production.v2.TargetPlacement;
import atlantis.production.v2.UnitProducible;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase 5(a) wiring: the default V2 planner answers Pylon sites through
 * the scored ring, still on fully validated tiles. The stub world cannot
 * judge power geometry (no map behind the tiles), so this pins the
 * contract, not the choice: a reservation comes back, the support channel
 * stays silent, repeated passes stay stable.
 */
@Tag("placement")
public class LegacyPlannerPowerSitingTest extends AbstractTestWithWorld {

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

    @BeforeEach
    public void hermetic() {
        if (Queue.get() != null) Queue.get().allOrders().list().clear();
        ConstructionRequests.constructions.clear();
    }

    @AfterEach
    public void clean() {
        if (Queue.get() != null) Queue.get().allOrders().list().clear();
        ConstructionRequests.constructions.clear();
    }

    @Test
    public void pylonReservationSurvivesScoring() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 24),
            fake(AUnitType.Protoss_Probe, 22, 24)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            currentSupplyUsed = 16;

            LegacyPlacementPlanner planner = new LegacyPlacementPlanner();
            planner.startPass();
            PlacementReservation reservation = planner.reservePlacement(
                UnitProducible.of(AUnitType.Protoss_Pylon), TargetPlacement.anywhere(), 2);

            assertTrue(reservation.isSuccessful(), "a tile is still answered");
            assertTrue(reservation.tileX() > 0 && reservation.tileY() > 0, "a real tile");
            assertTrue(
                planner.drainSupportRequests().isEmpty(),
                "the legacy planner declares no support - the catalogue path does that");
        });
    }

    @Test
    public void schedulerKeepsWorkingThroughThePlanner() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 24)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            LegacyPlacementPlanner planner = new LegacyPlacementPlanner();
            planner.startPass();
            PlacementReservation first = planner.reservePlacement(
                UnitProducible.of(AUnitType.Protoss_Gateway), TargetPlacement.anywhere(), 2);
            PlacementReservation second = planner.reservePlacement(
                UnitProducible.of(AUnitType.Protoss_Gateway), TargetPlacement.anywhere(), 2);

            assertFalse(
                first.isSuccessful() && second.isSuccessful()
                    && first.tileX() == second.tileX() && first.tileY() == second.tileY(),
                "non-Pylon buildings keep the single-tile behavior, no same-tile pair");
        });
    }
}
