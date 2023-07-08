package atlantis.combat.micro.avoid.special;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;

public class AvoidEdgesWhenMoving {

    public  boolean handle() {
        if (A.isUms()) {
            return false;
        }

        if (unit.isMoving() && !unit.isRunning() && unit.isGroundUnit()) {
            APosition pos = unit.position();

//            if (AMap.distanceToNearestChokeLessThan(unit, 5)) {
//                return false;
//            }

//            if (!AMap.isWalkable(pos.translateByTiles(1, 0))) {
//                return unit.move(pos.translateByTiles(-3 , 0), UnitActions.MOVE,"Edges");
//            }
//            if (!AMap.isWalkable(pos.translateByTiles(-1, 0))) {
//                return unit.move(pos.translateByTiles(3 , 0), UnitActions.MOVE,"Edges");
//            }
//            if (!AMap.isWalkable(pos.translateByTiles(0, 1))) {
//                return unit.move(pos.translateByTiles(0 , -3), UnitActions.MOVE,"Edges");
//            }
//            if (!AMap.isWalkable(pos.translateByTiles(0, -1))) {
//                return unit.move(pos.translateByTiles(0 , 3), UnitActions.MOVE,"Edges");
//            }
        }

        return false;
    }

}
