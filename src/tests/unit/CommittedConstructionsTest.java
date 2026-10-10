package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.v2.PlacementReservation;
import atlantis.production.v2.ProductionItem;
import atlantis.production.v2.ProductionPlan;
import atlantis.production.v2.UnitProducible;
import atlantis.production.v2.engine.CommittedConstructions;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the idempotency rule of {@link CommittedConstructions}: the pending-
 * construction re-offer must de-dupe on type <b>and tile</b>, never on type
 * alone.
 *
 * <p>
 * The regression this guards (STATE.md #48): two Pylons/Gateways requested at
 * different tiles are two constructions, and de-duping on the type dropped the
 * second one while the first was still only a request - the plan then never
 * carried it, so it was never built. The merge is a pure function of the
 * construction queue, so it is testable without a game or a stub world (the
 * queue is the only global it reads, and this test owns it).
 * </p>
 */
public class CommittedConstructionsTest {

    private static final AUnitType GATEWAY = AUnitType.Protoss_Gateway;

    @BeforeEach
    public void clearQueue() {
        ConstructionRequests.constructions.clear();
    }

    @AfterEach
    public void clearQueueAgain() {
        ConstructionRequests.constructions.clear();
    }

    private static Construction pending(AUnitType type, int tx, int ty) {
        Construction construction = new Construction(type);
        construction.setPositionToBuild(APosition.create(tx, ty));
        return construction;
    }

    @Test
    public void twoRequestsOfOneTypeAtDifferentTilesAreBothKept() {
        ConstructionRequests.constructions.add(pending(GATEWAY, 20, 20));
        ConstructionRequests.constructions.add(pending(GATEWAY, 30, 30));

        ProductionPlan merged = CommittedConstructions.mergeInto(new ProductionPlan(), 100);

        assertEquals(2, merged.size(),
                "two Gateways at distinct tiles are two constructions, not one duplicate");
        assertEquals(2, gatewaysIn(merged));
    }

    @Test
    public void theSameTypeAndTileIsOfferedOnlyOnce() {
        ConstructionRequests.constructions.add(pending(GATEWAY, 20, 20));

        ProductionPlan alreadyPlanning = new ProductionPlan();
        alreadyPlanning.add(new ProductionItem(
                UnitProducible.of(GATEWAY), 0, false,
                PlacementReservation.success(20, 20, 0).committedAt(0)));

        ProductionPlan merged = CommittedConstructions.mergeInto(alreadyPlanning, 100);

        assertEquals(1, merged.size(),
                "the same building at the same tile is already in the plan and must not be added twice");
    }

    @Test
    public void aPlanRequestAtAnotherTileDoesNotSuppressTheRequest() {
        // The plan carries a Gateway at 20:20; the queue has one at 30:30 - the
        // same rule seen from the plan side: the type alone is not the identity.
        ConstructionRequests.constructions.add(pending(GATEWAY, 30, 30));

        ProductionPlan plan = new ProductionPlan();
        plan.add(new ProductionItem(
                UnitProducible.of(GATEWAY), 0, false,
                PlacementReservation.success(20, 20, 0)));

        ProductionPlan merged = CommittedConstructions.mergeInto(plan, 100);

        assertEquals(2, merged.size(),
                "the request at 30:30 is a second building and must survive the merge");
    }

    @Test
    public void aConstructionWithoutATileIsIgnored() {
        ConstructionRequests.constructions.add(new Construction(GATEWAY)); // no position yet

        assertTrue(CommittedConstructions.mergeInto(new ProductionPlan(), 100).isEmpty(),
                "a construction with no chosen tile cannot be dispatched to a builder");
    }

    private static int gatewaysIn(ProductionPlan plan) {
        int count = 0;
        for (ProductionItem item : plan.items()) {
            if (item.item().id().equals(GATEWAY.name()))
                count++;
        }
        return count;
    }
}
