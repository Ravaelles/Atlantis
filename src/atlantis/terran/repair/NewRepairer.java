package atlantis.terran.repair;

import atlantis.units.AUnit;
import atlantis.units.select.Selection;
import atlantis.units.workers.FreeWorkers;

public class NewRepairer {
    public static AUnit repairerFor(AUnit unitToRepair, boolean criticallyImportant) {
//        Selection workers = FreeWorkers.get();

//        if (criticallyImportant) {
        Selection candidates = FreeWorkers.get();
//            if (!Strategy.get().isRush()) {
//                candidates = Select.ourWorkers()
//                    .notSpecialAction()
//                    .notRepairing()
//                    .notConstructing();
//            }
        return candidates.exclude(unitToRepair).nearestTo(unitToRepair);
//        }

//        // Try to use one of the protectors if he's non occupied
//        Collection<AUnit> protectors = RepairAssignments.getProtectors();
//        for (Iterator<AUnit> iterator = protectors.iterator(); iterator.hasNext(); ) {
//            AUnit protector = iterator.next();
//            if (protector.isUnitActionRepair()) {
//                iterator.remove();
//            }
//        }
//
//        if (!protectors.isEmpty()) {
//            return Select.from(protectors, "protectors").nearestTo(unitToRepair);
//        }
//
////        // If no free protector was found, return normal worker.
////        else {
////            return workers
////                .exclude(unitToRepair)
////                .nearestTo(unitToRepair);
////        }
//
////        ErrorLog.printMaxOncePerMinute("No free repairer found for " + unitToRepair);
//        return null;
    }
}
