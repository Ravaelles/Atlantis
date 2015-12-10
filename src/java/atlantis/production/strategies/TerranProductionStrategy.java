package atlantis.production.strategies;

import atlantis.AtlantisConfig;
import atlantis.wrappers.SelectUnits;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;

public class TerranProductionStrategy extends AtlantisProductionStrategy {

    @Override
    protected String getFilename() {
        return "DefaultTerran.csv";
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

}
