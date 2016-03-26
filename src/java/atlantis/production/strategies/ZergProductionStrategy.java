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
        _produceUnit(AtlantisConfig.WORKER);
    }

    @Override
    public void produceInfantry(UnitType infantryType) {
        _produceUnit(infantryType);
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
        _produceUnit(unitType);
    }

    // =========================================================
    protected void _produceUnit(UnitType unitType) {
        for (Unit base : Select.ourBases().listUnits()) {
            for (Unit unit : base.getLarva()) {
//                System.out.println(unit + " into " + unitType);
                base.train(unitType);
                return;
            }
        }
    }

    // =========================================================
    // Auxiliary
    private Unit getFreeLarva() {
        return (Unit) Select.our().ofType(UnitType.Zerg_Larva).first();
    }

}
