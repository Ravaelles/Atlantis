package atlantis.production.orders.build;

import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class TerranBuildOrder extends ABuildOrder {

    public TerranBuildOrder(String name) {
        super(name);
    }

    // =========================================================

    @Override
    public boolean produceUnit(AUnitType type, ProductionOrder order) {
        AUnitType whatBuildsIt = type.whatBuildsIt();
        AUnit parentUnit = Select.ourFree(whatBuildsIt).first();

        if (parentUnit != null && parentUnit.isFree()) {
            return parentUnit.train(type, order);
        }
//        else {
//            A.errPrintln("Can't find " + whatBuildsIt + " to produce " + type);
//        }

        return false;
    }

}
