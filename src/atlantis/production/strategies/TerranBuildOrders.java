package atlantis.production.strategies;

import atlantis.AtlantisConfig;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import java.util.ArrayList;

import bwapi.UnitType;

public class TerranBuildOrders extends AtlantisBuildOrders {

    @Override
    public void produceWorker() {
        AUnit building = Select.ourOneIdle(AtlantisConfig.BASE);
        if (building != null) {
            building.train(AtlantisConfig.WORKER);
        }
    }

    @Override
    public void produceInfantry(AUnitType infantryType) {
        AUnit building = Select.ourOneIdle(AtlantisConfig.BARRACKS);
        if (building != null) {
            building.train(infantryType);
        }
    }

    @Override
    public ArrayList<AUnitType> produceWhenNoProductionOrders() {
        ArrayList<AUnitType> units = new ArrayList<>();
        
        int marines = Select.our().countUnitsOfType(AUnitType.Terran_Marine);
        int medics = Select.our().countUnitsOfType(AUnitType.Terran_Medic);
        
        if ((double) marines / medics < 3) {
            units.add(AUnitType.Terran_Marine);
        }
        else {
            units.add(AUnitType.Terran_Medic);
        }
        return units;
    }

}
