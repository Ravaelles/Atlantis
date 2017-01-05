package atlantis.production.orders;

import atlantis.AtlantisConfig;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import java.util.ArrayList;

public class ProtossBuildOrders extends AtlantisBuildOrdersManager {

    @Override
    public void produceWorker() {
        AUnit building = Select.ourOneIdle(AtlantisConfig.BASE);
        if (building != null) {
            building.train(AtlantisConfig.WORKER);
        }
    }

    @Override
    public void produceUnit(AUnitType unitType) {
        AUnitType whatBuildsIt = unitType.getWhatBuildsIt();
        AUnit unitThatWillProduce = Select.ourOneIdle(whatBuildsIt);
        if (unitThatWillProduce != null) {
            unitThatWillProduce.train(unitType);
        }
        else {
            System.err.println("Can't find " + whatBuildsIt + " to produce " + unitType);
        }
    }

    @Override
    public ArrayList<AUnitType> produceWhenNoProductionOrders() {
        ArrayList<AUnitType> units = new ArrayList<>();
        units.add(AUnitType.Protoss_Zealot);
        return units;
    }

}
