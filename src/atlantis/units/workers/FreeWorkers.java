package atlantis.units.workers;

import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class FreeWorkers {
    public static Selection get() {
        return Select.ourWorkers()
//            .gatheringMinerals(true)
            // An uncompleted unit takes no order (canCommand=false until completion,
            // measured NEXT #63) - picking one as builder/scout/repairer burns the
            // assignment on a worker that cannot move, let alone build.
            .onlyCompleted()
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
