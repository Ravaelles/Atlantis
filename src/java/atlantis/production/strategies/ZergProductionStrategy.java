package atlantis.production.strategies;

import atlantis.AtlantisConfig;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.workers.AtlantisWorkerCommander;
import atlantis.units.Select;
import bwapi.Unit;
import java.util.ArrayList;

import bwapi.UnitType;

public class ZergProductionStrategy extends AtlantisProductionStrategy {

    @Override
    public void produceWorker() {
        produceZergUnit(AtlantisConfig.WORKER);
    }

    @Override
    public void produceInfantry(AUnitType infantryType) {
        produceZergUnit(infantryType);
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
            for (Unit larva : base.getLarva()) {
                boolean result = base.train(unitType);
                System.out.println(result + "  -> TRAIN " + unitType + " IN " + base);
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
