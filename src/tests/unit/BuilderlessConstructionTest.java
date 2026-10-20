package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.commanders.ConstructionThatLooksBugged;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * What a builderless NOT_STARTED construction does (NEXT #40).
 *
 * <p>
 * The recovery branch is unreachable by construction: the method returns
 * unless the status is NOT_STARTED, then assigns a builder only when the
 * status is NOT not-started. So a planned construction with no builder is
 * cancelled, never adopted. That is the pinned behavior - whether cancelling
 * (current) or adopting (the dead branch) is intended is the owner's call,
 * and this test will fail loudly if anyone flips it either way.
 * </p>
 */
@Tag("production")
public class BuilderlessConstructionTest extends AbstractTestWithWorld {

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

    @AfterEach
    public void cleanConstructions() {
        ConstructionRequests.constructions.clear();
    }

    @Test
    public void builderlessNotStartedIsCancelledNotAdopted() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);

        world(5, fakeOurs(nexus), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            Construction construction = new Construction(AUnitType.Protoss_Pylon);
            construction.setPositionToBuild(APosition.create(30, 30));
            construction.setStatus(ConstructionOrderStatus.NOT_STARTED);
            construction.setProductionOrder(
                new ProductionOrder(AUnitType.Protoss_Pylon, APosition.create(30, 30), 0));
            ConstructionRequests.constructions.add(construction);

            new ConstructionThatLooksBugged().invokedCommander();

            assertFalse(
                ConstructionRequests.constructions.contains(construction),
                "current behavior: a builderless plan is cancelled, not given a builder "
                    + "(the assign branch above it cannot run - intended? owner call)");
        });
    }

    @Test
    public void orderlessConstructionDoesNotThrow() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);

        world(5, fakeOurs(nexus), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            // The V2 shape: a construction with no queue order at all.
            Construction construction = new Construction(AUnitType.Protoss_Pylon);
            construction.setPositionToBuild(APosition.create(30, 30));
            construction.setStatus(ConstructionOrderStatus.NOT_STARTED);
            ConstructionRequests.constructions.add(construction);

            new ConstructionThatLooksBugged().invokedCommander();

            assertTrue(true, "reaching here without NullPointerException is the assertion");
        });
    }
}
