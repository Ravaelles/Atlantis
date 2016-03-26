package atlantis.production.strategies;

import atlantis.AtlantisConfig;
import atlantis.wrappers.Select;
import java.util.ArrayList;
import bwapi.Unit;
import bwapi.UnitType;

public class ProtossProductionStrategy extends AtlantisProductionStrategy {

    @Override
    protected String getFilename() {
        return "ProtossDefault.csv";
    }

    @Override
    public void produceWorker() {
        Unit building = Select.ourOneIdle(AtlantisConfig.BASE);
        if (building != null) {
            building.train(AtlantisConfig.WORKER);
        }
    }

    @Override
    public void produceInfantry(UnitType infantryType) {
        Unit building = Select.ourOneIdle(AtlantisConfig.BARRACKS);
        if (building != null) {
            building.train(infantryType);
        }
    }

    @Override
    public ArrayList<UnitType> produceWhenNoProductionOrders() {
        ArrayList<UnitType> units = new ArrayList<>();
        units.add(UnitType.Protoss_Zealot);
        return units;
    }

}
