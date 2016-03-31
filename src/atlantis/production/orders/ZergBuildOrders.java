package atlantis.production.orders;

import atlantis.AtlantisConfig;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.workers.AtlantisWorkerCommander;
import atlantis.units.Select;
import java.util.ArrayList;

public class ZergBuildOrders extends AtlantisBuildOrders {

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
        if (AtlantisWorkerCommander.shouldTrainWorkers(true)) {
            units.add(AUnitType.Zerg_Drone);
        }
        else {
            units.add(AUnitType.Zerg_Zergling);
        }
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
//                System.err.println("TRAIN FAIL: " + unitType + " IN " + base);
//                System.err.println("Are you sure you have supply? Chech against it.");
//                larva.morph(unitType);
                return;
            }
        }
    }

    // =========================================================
    // Auxiliary
//    private AUnit getFreeLarva() {
//        return (Unit) Select.our().ofType(AUnitType.Zerg_Larva).first();
//    }

}
