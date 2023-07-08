package atlantis.combat.squad.positioning;

import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.managers.Manager;
import atlantis.units.select.Select;
import atlantis.util.We;

public class ComeCloserToTanks extends Manager {

    public ComeCloserToTanks(AUnit unit) {
        super(unit);
    }

    protected Manager handleTooFarFromTanks() {
//        if (!We.terran() || unit.isMissionDefend()) {
        if (!We.terran()) {
            return null;
        }

        if (unit.isMoving() && unit.isManager(this) && unit.lastActionLessThanAgo(13)) {
            return continueUsingManager();
        }

        if (unit.isMissionDefend()) {
            AUnit nearestEnemy = unit.nearestEnemy();

            // When DEFENDING allow to attack enemies near our buildings
            if (
                nearestEnemy != null
                && nearestEnemy.enemiesNear().buildings().inRadius(6, nearestEnemy).atLeast(2)
            ) {
                return null;
            }
        }

        // Too far from nearest tank
        if (squad.units().tanks().count() >= 2) {
            AUnit tank = Select.ourTanks().nearestTo();
            if (tank != null && !unitIsOvercrowded() && !tankIsOvercrowded(tank)) {
                APosition goTo = unit.translateTilesTowards(1.5, tank)
                    .makeFreeOfAnyGroundUnits(1.5, 0.25, unit);

                if (goTo != null && unit.move(goTo, Actions.MOVE_FORMATION, "HugTanks", false)) {
                    unit.addLog("HugTanks");
                    return usingManager(this);
                }
            }
        }

        return null;
    }

    protected boolean unitIsOvercrowded() {
        return unit.friendsInRadius(2).groundUnits().atLeast(5)
            || unit.friendsInRadius(4).groundUnits().atLeast(10);
    }

    protected boolean tankIsOvercrowded(AUnit tank) {
        return tank.friendsInRadius(2).groundUnits().atLeast(5)
            || tank.friendsInRadius(4).groundUnits().atLeast(9);
    }
}
