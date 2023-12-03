package atlantis.production.dynamic.terran.units;

import atlantis.production.orders.production.queue.order.ForcedDirectProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.log.ErrorLog;

public class ForceProduceUnit {

    public static boolean forceProduce(AUnitType type) {
        if (type.isMarine()) {
            System.err.println("--- MARINE");
            System.err.println("--- " + type.whatBuildsIt());
            System.err.println("--- " + type.whatIsRequired());
        }

        AUnitType building = type.whatBuildsIt();
        if (building == null) {
            ErrorLog.printMaxOncePerMinute("ForceProduceUnit: " + type + " has no parent building");
            return false;
        }

        AUnit randomProducer = Select.ourFree(building).random();
        if (randomProducer == null) return false;

        return randomProducer.train(
            type, ForcedDirectProductionOrder.create(type)
        );
    }
}
