package atlantis.production.constructing.commanders;

import atlantis.architecture.Commander;
import atlantis.game.AGame;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.ConstructionOrderStatus;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.workers.FreeWorkers;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

import java.util.Iterator;

public class ConstructionThatLooksBugged extends Commander {
    @Override
    protected void handle() {
        for (Iterator<Construction> iterator = ConstructionRequests.constructions.iterator(); iterator.hasNext(); ) {
            Construction construction = iterator.next();

            handleConstructionThatLooksBugged(construction);
        }
    }

    private void handleConstructionThatLooksBugged(Construction constr) {
        if (constr.status() != ConstructionOrderStatus.NOT_STARTED) return;
        if (constr.buildingUnit() != null) return;

        AUnitType type = constr.buildingType();
        if (constr.builder() == null || constr.builder().isDead()) {
            if (constr.status() != ConstructionOrderStatus.NOT_STARTED) {
                constr.assignOptimalBuilder();
            }

            if (constr.builder() == null || constr.buildPosition() == null) {
                ErrorLog.printMaxOncePerMinute("Weird case, " + type + " has no builder. Cancel.");
                constr.productionOrder().cancel();
            }

//            else {
//                if (Count.workers() >= 3) {
//                    ErrorLog.printMaxOncePerMinute("Weird case, " + type + " has no builder. Cancel.");
//                }
//                constr.productionOrder().cancel();
//                return;
//            }
        }

        AUnit main = Select.main();
        int bonus = We.protoss() ? 30 * 8 : 0;
        int timeout = bonus + 30 * (
            8
                + (type.isBase() || type.isCombatBuilding() ? 40 : 10)
                + ((int) (2.9 * constr.buildPosition().groundDistanceTo(main != null ? main : constr.builder())))
        );

        if (!type.isCombatBuilding() && !type.producesLandUnits()) {
            timeout += 30 * 40;
        }

        if (AGame.now() - constr.timeOrdered() > timeout) {
//            System.err.println(" // " + AGame.now() + " // " + constr.timeOrdered() + " // > " + timeout);
            ErrorLog.printMaxOncePerMinute(
                "Cancel constr of " + type
                    + " (Took too long)"
                    + " buildable:" + constr.buildPosition().isBuildable()
            );
            constr.cancel();
        }
    }
}
