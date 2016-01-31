package atlantis.production.strategies;

import atlantis.AtlantisConfig;
import atlantis.wrappers.SelectUnits;
import java.util.ArrayList;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;

public class ProtossProductionStrategy extends AtlantisProductionStrategy {

    @Override
    protected String getFilename() {
        return "ProtossDefault.csv";
    }

    @Override
    public void produceWorker() {
        Unit building = SelectUnits.ourOneIdle(AtlantisConfig.BASE);
        if (building != null) {
            building.train(AtlantisConfig.WORKER);
        }
    }

    @Override
    public void produceInfantry(UnitType infantryType) {
        Unit building = SelectUnits.ourOneIdle(AtlantisConfig.BARRACKS);
        if (building != null) {
            building.train(infantryType);
        }
    }

    @Override
    public ArrayList<UnitType> produceWhenNoProductionOrders() {
        ArrayList<UnitType> units = new ArrayList<>();
        units.add(UnitType.UnitTypes.Protoss_Zealot);
        return units;
    }

}
