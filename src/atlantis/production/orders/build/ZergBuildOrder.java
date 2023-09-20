package atlantis.production.orders.build;

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
    public boolean produceUnit(AUnitType type) {
        return ProduceZergUnit.produceZergUnit(type);
    }
}
