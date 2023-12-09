package atlantis.production.constructing.commanders;

import atlantis.architecture.Commander;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.units.AUnit;

import java.util.Iterator;

public class ConstructionUnderAttack extends Commander {
    @Override
    protected void handle() {
        for (Iterator<Construction> iterator = ConstructionRequests.constructions.iterator(); iterator.hasNext(); ) {
            Construction construction = iterator.next();

            handleConstructionUnderAttack(construction);
        }
    }

    private void handleConstructionUnderAttack(Construction order) {
        AUnit building = order.buildingUnit();

        // If unfinished building is under attack
        if (building != null && !building.isCompleted() && building.lastUnderAttackLessThanAgo(20)) {

            // If it has less than 71HP or less than 60% and is close to being finished
            if (building.hp() <= 32 || building.getRemainingBuildTime() <= 30) {
                order.cancel();
            }
        }
    }
}
