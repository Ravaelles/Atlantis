package atlantis.production.constructions.commanders;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.units.AUnit;
import atlantis.util.We;

import java.util.Iterator;

public class ConstructionUnderAttack extends Commander {
    @Override
    protected boolean handle() {
        for (Iterator<Construction> iterator = ConstructionRequests.constructions.iterator(); iterator.hasNext(); ) {
            Construction construction = iterator.next();

            handleConstructionUnderAttack(construction);
        }
        return false;
    }

    private void handleConstructionUnderAttack(Construction order) {
        AUnit building = order.buildingUnit();
        if (building == null || building.isCompleted()) return;
        if (!building.lastUnderAttackLessThanAgo(100)) return;
        if (building.woundPercent() <= 50) return;

        // If it has less than 71HP or less than 60% and is close to being finished
        if (building.hp() <= 46 || building.getRemainingBuildTime() <= 5) {
            if (preventCancelAsProtoss(building)) return;

            order.cancel(building.type() + " under attack");
        }
    }

    private boolean preventCancelAsProtoss(AUnit building) {
        if (!We.protoss()) return false;

        if (building.isCannon() && building.hp() >= 82) return true;
        if (A.s <= 60 * 5 && building.hpPercent() >= 40) return true;

        return false;
    }
}
