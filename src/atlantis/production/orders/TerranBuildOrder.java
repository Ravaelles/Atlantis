package atlantis.production.orders;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class TerranBuildOrder extends ABuildOrder {

    public TerranBuildOrder(String name) {
        super(name);
    }

    // =========================================================

    @Override
    public boolean produceUnit(AUnitType type) {
        AUnitType whatBuildsIt = type.whatBuildsIt();
        AUnit unitThatWillProduce = Select.ourOfType(whatBuildsIt).free().first();
        if (unitThatWillProduce != null) {
            return unitThatWillProduce.train(type);
        }
//        else {
//            System.err.println("Can't find " + whatBuildsIt + " to produce " + unitType);
//        }

        return false;
    }

}
