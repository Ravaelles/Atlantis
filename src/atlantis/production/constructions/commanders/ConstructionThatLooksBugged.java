package atlantis.production.constructions.commanders;

import atlantis.architecture.Commander;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.units.AUnitType;
import atlantis.util.log.ErrorLog;

import java.util.Iterator;

public class ConstructionThatLooksBugged extends Commander {
    @Override
    protected boolean handle() {
        for (Iterator<Construction> iterator = ConstructionRequests.constructions.iterator(); iterator.hasNext(); ) {
            Construction construction = iterator.next();

            handleConstructionThatLooksBugged(construction);
        }
        return false;
    }

    private void handleConstructionThatLooksBugged(Construction constr) {
        if (constr.status() != ConstructionOrderStatus.NOT_STARTED) return;
        if (constr.buildingUnit() != null) return;

        AUnitType buildingType = constr.buildingType();
        AUnitType type = buildingType;
        if (constr.builder() == null || constr.builder().isDead()) {
            if (constr.status() != ConstructionOrderStatus.NOT_STARTED) {
                constr.assignOptimalBuilder();
            }

            if (constr.builder() == null || constr.buildPosition() == null) {
                ErrorLog.printMaxOncePerMinute("Weird case, " + type + " has no builder. Cancel.");
                constr.productionOrder().cancel(type + " looks bugged ");
            }

//            else {
//                if (Count.workers() >= 3) {
//                    ErrorLog.printMaxOncePerMinute("Weird case, " + type + " has no builder. Cancel.");
//                }
//                constr.productionOrder().cancel();
//                return;
//            }
        }

        CancelTooLongConstructions.cancelCauseTakingTooLongIfNeeded(constr, type, buildingType);
    }
}
