package atlantis.production.orders;

import atlantis.AtlantisConfig;
import atlantis.production.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

import java.util.ArrayList;

public class ZergBuildOrder extends ABuildOrder {
    
//    public static final ZergBuildOrder ZERG_13_POOL_MUTA = new ZergBuildOrder("13 Pool Muta");

    // =========================================================

    public ZergBuildOrder(String name) {
        super(name);
    }

    // =========================================================

    @Override
    public boolean produceWorker() {
        return produceZergUnit(AtlantisConfig.WORKER);
    }

    @Override
    public boolean produceUnit(AUnitType unitType) {
        return produceZergUnit(unitType);
    }

    // =========================================================
    
    /**
     * Produce zerg unit from free larva. Will do nothing if no free larva is available.
     */
    public boolean produceZergUnit(AUnitType unitType) {
        for (AUnit base : Select.ourBases().listUnits()) {
            for (AUnit larva : base.getLarva()) {
                return base.train(unitType);
            }
        }

        return false;
    }

}
