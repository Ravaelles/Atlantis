package atlantis.combat.micro;

import atlantis.map.AMap;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.UnitActions;

public class AvoidEdgesWhenMoving {

    public static boolean handle(AUnit unit) {
        if (unit.isMoving() && !unit.isRunning() && unit.isGroundUnit()) {
            APosition pos = unit.getPosition();

            if (AMap.distanceToNearestChokeLessThan(unit, 5)) {
                return false;
            }

            if (!AMap.isWalkable(pos.translateByTiles(1, 0))) {
                return unit.move(pos.translateByTiles(-3 , 0), UnitActions.MOVE,"Edges");
            }
            if (!AMap.isWalkable(pos.translateByTiles(-1, 0))) {
                return unit.move(pos.translateByTiles(3 , 0), UnitActions.MOVE,"Edges");
            }
            if (!AMap.isWalkable(pos.translateByTiles(0, 1))) {
                return unit.move(pos.translateByTiles(0 , -3), UnitActions.MOVE,"Edges");
            }
            if (!AMap.isWalkable(pos.translateByTiles(0, -1))) {
                return unit.move(pos.translateByTiles(0 , 3), UnitActions.MOVE,"Edges");
            }
        }

        return false;
    }

}
