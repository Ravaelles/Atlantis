package atlantis.combat.retreating;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class TerranInfantryShouldRetreat extends Manager {

    public TerranInfantryShouldRetreat(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isTerranInfantry();
    }

    public Manager shouldRetreat() {
        if (!applies()) return null;

        if (!unit.mission().isMissionDefend()) {
            if (
                unit.enemiesNear().ranged().notEmpty()
                    && unit.friendsNear().atMost(4) && unit.combatEvalRelative() <= 2
            ) {
                unit.setTooltipTactical("BewareRanged");
                return usedManager(this);
            }
        }

        return null;
    }
}
