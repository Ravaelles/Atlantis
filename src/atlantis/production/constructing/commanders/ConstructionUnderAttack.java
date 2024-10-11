package atlantis.production.constructing.commanders;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.units.AUnit;
import atlantis.util.We;

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
            if (building.hp() <= 42 || building.getRemainingBuildTime() <= 30) {
                if (preventCancelAsProtoss(building)) return;

                order.cancel();
            }
        }
    }

    private boolean preventCancelAsProtoss(AUnit building) {
        if (!We.protoss()) return false;

        if (building.isCannon() && building.hpPercent() >= 70) return true;
        if (A.s <= 60 * 5 && building.hpPercent() >= 40) return true;

        return false;
    }
}
