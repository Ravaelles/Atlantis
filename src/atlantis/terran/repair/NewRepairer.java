package atlantis.terran.repair;

import atlantis.information.strategy.OurStrategy;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

import java.util.Collection;
import java.util.Iterator;

public class NewRepairer {
    public static AUnit repairerFor(AUnit unitToRepair, boolean criticallyImportant) {
        if (criticallyImportant) {
            Selection candidates = Select.ourWorkers().notRepairing();
            if (!OurStrategy.get().isRush()) {
                candidates = candidates.notScout().notConstructing();
            }
            return candidates.exclude(unitToRepair).nearestTo(unitToRepair);
        }

        // Try to use one of the protectors if he's non occupied
        Collection<AUnit> protectors = RepairAssignments.getProtectors();
        for (Iterator<AUnit> iterator = protectors.iterator(); iterator.hasNext();) {
            AUnit protector = iterator.next();
            if (protector.isUnitActionRepair()) {
                iterator.remove();
            }
        }

        if (!protectors.isEmpty()) {
            return Select.from(protectors, "protectors").nearestTo(unitToRepair);
        }

        // If no free protector was found, return normal worker.
        else {
            return Select.ourWorkers()
                .notCarrying()
                .notRepairing()
                .notGatheringGas()
                .notConstructing()
                .notScout()
                .exclude(unitToRepair)
                .nearestTo(unitToRepair);
        }
    }
}
