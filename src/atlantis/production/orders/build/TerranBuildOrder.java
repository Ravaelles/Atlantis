package atlantis.production.orders.build;

import atlantis.game.A;
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
        AUnit parentUnit = Select.ourFree(whatBuildsIt).first();

        if (parentUnit != null && parentUnit.isFree()) {
            return parentUnit.train(type);
        }
//        else {
//            A.errPrintln("Can't find " + whatBuildsIt + " to produce " + type);
//        }

        return false;
    }

}
