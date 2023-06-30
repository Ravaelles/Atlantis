package atlantis.combat.squad.positioning;

import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.util.We;

public class ComeCloserToTanks {
    protected static boolean isTooFarFromTanks(AUnit unit) {
//        if (!We.terran() || unit.isMissionDefend()) {
        if (!We.terran()) {
            return false;
        }

        if (unit.isMoving() && unit.lastActionLessThanAgo(11, Actions.MOVE_FORMATION)) {
            return true;
        }

        if (unit.isMissionDefend()) {
            AUnit nearestEnemy = unit.nearestEnemy();

            // When DEFENDING allow to attack enemies near our buildings
            if (
                nearestEnemy != null
                && nearestEnemy.enemiesNear().buildings().inRadius(6, nearestEnemy).atLeast(2)
            ) {
                return false;
            }
        }

        // Too far from nearest tank
        if (unit.squad().units().tanks().count() >= 2) {
            AUnit tank = Select.ourTanks().nearestTo(unit);
            if (tank != null && !unitIsOvercrowded(unit) && !tankIsOvercrowded(tank)) {
                APosition goTo = unit.translateTilesTowards(1.5, tank)
                    .makeFreeOfAnyGroundUnits(1.5, 0.25, unit);
                if (goTo != null && unit.move(goTo, Actions.MOVE_FORMATION, "HugTanks", false)) {
                    unit.addLog("HugTanks");
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean unitIsOvercrowded(AUnit unit) {
        return unit.friendsInRadius(2).groundUnits().atLeast(7)
            || unit.friendsInRadius(4).groundUnits().atLeast(14);
    }

    protected static boolean tankIsOvercrowded(AUnit tank) {
        return tank.friendsInRadius(2).groundUnits().atLeast(6)
            || tank.friendsInRadius(4).groundUnits().atLeast(10);
    }
}
