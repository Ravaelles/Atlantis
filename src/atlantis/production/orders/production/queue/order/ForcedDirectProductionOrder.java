package atlantis.production.orders.production.queue.order;

import atlantis.units.AUnitType;

public class ForcedDirectProductionOrder extends ProductionOrder {
    public static ForcedDirectProductionOrder create(AUnitType unitType) {
        return new ForcedDirectProductionOrder(unitType);
    }

    protected ForcedDirectProductionOrder(AUnitType unitType) {
        super(unitType, 0);
    }

    @Override
    public String toString() {
        return "ForcedDirectProductionOrder{" + super.toString() + '}';
    }
}
