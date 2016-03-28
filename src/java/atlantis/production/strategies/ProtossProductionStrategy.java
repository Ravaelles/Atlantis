package atlantis.production.strategies;

import atlantis.AtlantisConfig;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import java.util.ArrayList;

import bwapi.UnitType;

public class ProtossProductionStrategy extends AtlantisProductionStrategy {

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
        units.add(AUnitType.Protoss_Zealot);
        return units;
    }

}
