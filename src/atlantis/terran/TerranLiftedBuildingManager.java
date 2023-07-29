package atlantis.terran;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class TerranLiftedBuildingManager extends Manager {

    public TerranLiftedBuildingManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isLifted() && unit.isABuilding() && unit.isWounded();
    }

    /**
     * Buildings will be lifted:
     * - when under attack,
     * - when base runs out of minerals, we fly to a new location
     */
    @Override
    public Manager handle() {
        if (unit.lastUnderAttackLessThanAgo(30 * 5)) {
            AUnit enemy = unit.enemiesNear().canAttack(unit, 3).nearestTo(unit);
            if (enemy != null) {
                unit.setTooltip("IntoSafety");
                unit.runningManager().runFrom(enemy, 4, Actions.RUN_ENEMY, false);
                return usedManager(this);
            }
        }

        return null;
    }
}
