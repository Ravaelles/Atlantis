package tests.unit;

import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.builders.ShouldNotTravelToConstructYet;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Dispatch timing by simulation, not by static margin (PurpleWave's Bank,
 * ported): the bank now plus mining income, minus earlier-queued unpaid
 * rows, gives the earliest affordable frame; the walk takes distance over
 * the builder's speed. Walking now is right when the money lands no later
 * than the boots - the walk overlaps earning and any wait happens AT the
 * site. Scope: supply 50 and under, 240-frame horizon; above, legacy.
 */
@Tag("infra")
public class TravelForecastTest extends AbstractTestWithWorld {

    private FakeUnit builder;
    private ProductionOrder earlierOrder;
    private ProductionOrder selfOrder;

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
        earlierOrder = null;
        selfOrder = null;
        if (Queue.get() != null) {
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
        }
        ConstructionRequests.constructions.clear();
    }

    @AfterEach
    public void clean() {
        earlierOrder = null;
        selfOrder = null;
        if (Queue.get() != null) {
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
        }
        ConstructionRequests.constructions.clear();
    }

    @Test
    public void goesWhenAffordableNow() {
        world(3, fixture(false), fakeEnemies(), () -> {
            if (atlantis.game.A.now() == 1) seedQueue(false);
            if (atlantis.game.A.now() != 2) return;
            currentSupplyUsed = 20;
            currentMinerals = 200;

            assertFalse(
                ShouldNotTravelToConstructYet.check(linkedPylon(), builder, 5.0),
                "bank covers the price: walk now");
        });
    }

    @Test
    public void earlierUnpaidRowHoldsDispatch() {
        world(3, fixture(false), fakeEnemies(), () -> {
            if (atlantis.game.A.now() == 1) seedQueue(true);
            if (atlantis.game.A.now() != 2) return;
            currentSupplyUsed = 20;
            currentMinerals = 150;

            assertTrue(
                ShouldNotTravelToConstructYet.check(linkedPylon(), builder, 5.0),
                "150 bank minus the earlier 150 Gateway leaves nothing: wait");
            assertFalse(
                ShouldNotTravelToConstructYet.check(AUnitType.Protoss_Pylon, 5.0),
                "documents the delta: the legacy margin walks into the earlier row's minerals");
        });
    }

    @Test
    public void scopeBoundaryAboveFiftyUsesLegacy() {
        world(3, fixture(false), fakeEnemies(), () -> {
            if (atlantis.game.A.now() == 1) seedQueue(true);
            if (atlantis.game.A.now() != 2) return;
            currentMinerals = 150;
            Construction construction = linkedPylon();

            currentSupplyUsed = 60;
            assertFalse(
                ShouldNotTravelToConstructYet.check(construction, builder, 5.0),
                "past 50 the legacy margin decides (and it walks here)");

            currentSupplyUsed = 20;
            assertTrue(
                ShouldNotTravelToConstructYet.check(construction, builder, 5.0),
                "same state at 20: the forecast holds for the earlier row");
        });
    }

    @Test
    public void syncBoundaryHoldThenGo() {
        world(3, fixture(true), fakeEnemies(), () -> {
            if (atlantis.game.A.now() == 1) seedQueue(false);
            if (atlantis.game.A.now() != 2) return;
            currentSupplyUsed = 20;
            Construction construction = linkedPylon();

            currentMinerals = 60;
            assertTrue(
                ShouldNotTravelToConstructYet.check(construction, builder, 5.0),
                "money lands ~89f out, boots in ~39: walking now donates mining");
            assertFalse(
                ShouldNotTravelToConstructYet.check(AUnitType.Protoss_Pylon, 5.0),
                "documents the delta: the legacy margin walks at bank 60");

            currentMinerals = 82;
            assertFalse(
                ShouldNotTravelToConstructYet.check(construction, builder, 5.0),
                "money lands ~40f out, boots in ~39: walk now, wait at the site");
        });
    }

    @Test
    public void beyondHorizonHolds() {
        world(3, fixture(false), fakeEnemies(), () -> {
            if (atlantis.game.A.now() == 1) seedQueue(false);
            if (atlantis.game.A.now() != 2) return;
            currentSupplyUsed = 20;
            currentMinerals = 0;

            assertTrue(
                ShouldNotTravelToConstructYet.check(linkedPylon(), builder, 5.0),
                "no income in sight: beyond the horizon, hold");
        });
    }

    private FakeUnit[] fixture(boolean mining) {
        FakeUnit[] miners = new FakeUnit[10];
        for (int i = 0; i < miners.length; i++) {
            miners[i] = fake(AUnitType.Protoss_Probe, 21 + i, 24);
            if (mining) miners[i].setAction(Actions.GATHER_MINERALS);
        }
        builder = fake(AUnitType.Protoss_Probe, 21, 26);
        FakeUnit[] ours = new FakeUnit[12];
        ours[0] = fake(AUnitType.Protoss_Nexus, 20, 20);
        System.arraycopy(miners, 0, ours, 1, miners.length);
        ours[11] = builder;
        return fakeOurs(ours);
    }

    private void seedQueue(boolean withEarlier) {
        Queue.get().allOrders().list().clear();
        Queue.get().clearCache();
        ConstructionRequests.constructions.clear();
        int position = 0;
        if (withEarlier) {
            earlierOrder = new ProductionOrder(AUnitType.Protoss_Gateway, 10);
            Queue.get().addNew(position++, earlierOrder);
        }
        selfOrder = new ProductionOrder(AUnitType.Protoss_Pylon, 16);
        Queue.get().addNew(position, selfOrder);
    }

    private Construction linkedPylon() {
        Construction construction = new Construction(AUnitType.Protoss_Pylon);
        construction.setProductionOrder(selfOrder);
        selfOrder.setConstruction(construction);
        construction.setPositionToBuild(
            atlantis.map.position.APosition.create(26, 24));
        construction.setBuilder(builder);
        ConstructionRequests.addConstruction(construction);
        return construction;
    }
}
