package atlantis.units.workers;

import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class FreeWorkers {
    public static Selection get() {
        return Select.ourWorkers()
//            .gatheringMinerals(true)
            .notCarrying()
            .notGatheringGas()
            .notScout()
            .notConstructing()
            .notRepairing()
            .notProtectors()
            .notSpecialAction();
    }

    public static AUnit getOne() {
        return get().first();
    }
}
