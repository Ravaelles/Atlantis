package atlantis.production.orders.build;

import atlantis.config.AtlantisRaceConfig;
import atlantis.production.orders.production.queue.order.ForcedDirectProductionOrder;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.zerg.ProduceZergUnit;
import atlantis.units.AUnitType;

public class ZergBuildOrder extends ABuildOrder {

//    public static final ZergBuildOrder ZERG_13_POOL_MUTA = new ZergBuildOrder("13 Pool Muta");

    // =========================================================

    public ZergBuildOrder(String name) {
        super(name);
    }

    // =========================================================

    @Override
    public boolean produceUnit(AUnitType type, ProductionOrder order) {
        return ProduceZergUnit.produceZergUnit(type, order);
    }
}
