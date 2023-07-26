package atlantis.combat.retreating;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class TerranShouldRetreat extends Manager {

    public TerranShouldRetreat (AUnit unit) {
        super(unit);
    }

    public Manager shouldRetreat() {
        if (unit.isTerranInfantry()) {
            if (!unit.mission().isMissionDefend()) {
                if (unit.enemiesNear().ranged().notEmpty() && unit.friendsNear().atMost(4) && unit.combatEvalRelative() <= 2) {
                    unit.setTooltipTactical("BewareRanged");
                    return usedManager(this);
                }
            }
        }

        return null;
    }
}
