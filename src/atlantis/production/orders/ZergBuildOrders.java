package atlantis.production.orders;

import atlantis.AtlantisConfig;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.workers.AtlantisWorkerCommander;
import java.util.ArrayList;

public class ZergBuildOrders extends AtlantisBuildOrdersManager {

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
        
        boolean shouldTrainWorkers = AtlantisWorkerCommander.shouldTrainWorkers(false);
        
        if (shouldTrainWorkers) {
            units.add(AUnitType.Zerg_Drone);
        }
        
        units.add(AUnitType.Zerg_Hydralisk);
        units.add(AUnitType.Zerg_Zergling);
        units.add(AUnitType.Zerg_Hydralisk);
        
        if (shouldTrainWorkers) {
            units.add(AUnitType.Zerg_Drone);
        }
        
        units.add(AUnitType.Zerg_Zergling);
        units.add(AUnitType.Zerg_Zergling);
        
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
    }

}
