package atlantis.production.constructions.builders;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class MoveUnitsFromConstructionPlace {
    public static boolean move(AUnit unit, Construction construction, double distanceToConstruction) {
        if (distanceToConstruction >= 6) return false;

        APosition buildPosition = construction.buildPosition();
        if (buildPosition == null) return false;

        if (unit.enemiesNear().groundUnits().countInRadius(6, unit) >= 3) return false;

        for (AUnit otherUnit : unit.friendsNear().nonBuildings().list()) {
            if (otherUnit.distTo(buildPosition) >= 3) continue;
            if (otherUnit.isAttacking() || otherUnit.isRunningOrRetreating() || otherUnit.hasCooldown()) continue;

            if (otherUnit.moveAwayFrom(buildPosition, 2, Actions.MOVE_SPACE)) {
                otherUnit.setTooltip("MovingFromConstructionPlace");
            }
        }

        return true;
    }
}
