package atlantis.production.constructing.commanders;

import atlantis.architecture.Commander;
import atlantis.game.AGame;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.ConstructionOrderStatus;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
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

    private void handleConstructionThatLooksBugged(Construction order) {
        if (order.status() != ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED) {
            return;
        }

        if (order.builder() == null) {
            if (Count.workers() >= 3) {
                ErrorLog.printMaxOncePerMinute("Weird case, " + order.buildingType() + " has no builder. Cancel.");
            }
            order.cancel();
            return;
        }

        AUnit main = Select.main();
        int timeout = 30 * (
            8
                + (order.buildingType().isBase() || order.buildingType().isCombatBuilding() ? 40 : 10)
                + ((int) (2.9 * order.buildPosition().groundDistanceTo(main != null ? main : order.builder())))
        );

        if (AGame.now() - order.timeOrdered() > timeout) {
//            System.err.println(" // " + AGame.now() + " // " + order.timeOrdered() + " // > " + timeout);
            ErrorLog.printMaxOncePerMinute("Cancel construction of " + order.buildingType() + " (Took too long)");
            order.cancel();
        }
    }
}
