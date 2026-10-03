package tests.fakes;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.OrderFallback;

/**
 * Records the orders the harness's units receive, in the lists the tests read
 * ({@link FakeUnitData#TRAIN}, {@link FakeUnitData#CANCEL}). Before the
 * {@link OrderFallback} port, {@code AUnitOrders} wrote to those lists itself.
 */
public class FakeOrderFallback implements OrderFallback.Sink {
    public static void installAsSink() {
        OrderFallback.useSink(new FakeOrderFallback());
    }

    @Override
    public boolean train(AUnitType unitToTrain) {
        return FakeUnitData.TRAIN.add(unitToTrain);
    }

    @Override
    public boolean cancelConstruction(AUnit unit) {
        return FakeUnitData.CANCEL.add(unit);
    }
}
