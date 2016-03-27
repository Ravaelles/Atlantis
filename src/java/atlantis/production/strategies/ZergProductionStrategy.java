package atlantis.production.strategies;

import atlantis.AtlantisConfig;
import atlantis.workers.AtlantisWorkerCommander;
import atlantis.wrappers.Select;
import java.util.ArrayList;
import bwapi.Unit;
import bwapi.UnitType;

public class ZergProductionStrategy extends AtlantisProductionStrategy {

    @Override
    public void produceWorker() {
        produceZergUnit(AtlantisConfig.WORKER);
    }

    @Override
    public void produceInfantry(UnitType infantryType) {
        produceZergUnit(infantryType);
    }

    @Override
    public ArrayList<UnitType> produceWhenNoProductionOrders() {
        ArrayList<UnitType> units = new ArrayList<>();
        if (AtlantisWorkerCommander.shouldTrainWorkers(true)) {
            units.add(UnitType.Zerg_Drone);
        }
        else {
            units.add(UnitType.Zerg_Zergling);
        }
        return units;
    }

    // =========================================================
    
    /**
     * Produce zerg unit from free larva. Will do nothing if no free larva is available.
     */
    public void produceZergUnit(UnitType unitType) {
        for (Unit base : Select.ourBases().listUnits()) {
            for (Unit larva : base.getLarva()) {
                boolean result = base.train(unitType);
                result = base.train();
                System.out.println(result + "  -> TRAIN " + unitType + " IN " + base);
//                larva.morph(unitType);
                return;
            }
        }
    }

    // =========================================================
    // Auxiliary
//    private Unit getFreeLarva() {
//        return (Unit) Select.our().ofType(UnitType.Zerg_Larva).first();
//    }

}
