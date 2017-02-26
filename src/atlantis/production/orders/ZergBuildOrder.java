package atlantis.production.orders;

import atlantis.AtlantisConfig;
import atlantis.production.ADynamicWorkerProductionManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import java.util.ArrayList;

public class ZergBuildOrder extends ABuildOrder {
    
    public static final ZergBuildOrder ZERG_13_POOL_MUTA = new ZergBuildOrder("13 Pool Muta");
    
    // =========================================================
    
    private ZergBuildOrder(String relativePath) {
        super("Zerg/" + relativePath);
    }

    // =========================================================

    @Override
    public void produceWorker() {
        produceZergUnit(AtlantisConfig.WORKER);
    }

    @Override
    public void produceUnit(AUnitType unitType) {
        produceZergUnit(unitType);
    }

    @Override
    public ArrayList<AUnitType> produceWhenNoProductionOrders() {
        ArrayList<AUnitType> units = new ArrayList<>();
        
        boolean shouldTrainWorkers = ADynamicWorkerProductionManager.shouldTrainWorkers(false);
        
        if (shouldTrainWorkers) {
            units.add(AUnitType.Zerg_Drone);
        }
        
        units.add(AUnitType.Zerg_Hydralisk);
        units.add(AUnitType.Zerg_Zergling);
        
        if (shouldTrainWorkers) {
            units.add(AUnitType.Zerg_Drone);
        }
        
        units.add(AUnitType.Zerg_Mutalisk);
        units.add(AUnitType.Zerg_Mutalisk);
        
        return units;
    }

    // =========================================================
    
    /**
     * Produce zerg unit from free larva. Will do nothing if no free larva is available.
     */
    public void produceZergUnit(AUnitType unitType) {
        for (AUnit base : Select.ourBases().listUnits()) {
            for (AUnit larva : base.getLarva()) {
                boolean result = base.train(unitType);
                return;
            }
        }
//        AUnit larva = Select.ourLarva().first();
//        System.out.println(larva);
//        if (larva != null) {
//            larva.train(unitType);
//        }
    }

}
