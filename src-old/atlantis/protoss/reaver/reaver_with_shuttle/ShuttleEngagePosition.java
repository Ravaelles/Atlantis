package atlantis.protoss.reaver.reaver_with_shuttle;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.position.Positions;
import atlantis.map.position.helpers.NearestWalkable;
import atlantis.units.AUnit;
import bwapi.Color;

public class ShuttleEngagePosition {
    public static HasPosition definePosition(AUnit unit, AUnit reaver, AUnit enemy) {
        HasPosition targetPosition = enemy;

        if (!unit.position().isWalkable()) {
//            Positions<APosition> walkablePositions = NearestWalkable.andFreeFromEnemies(
            targetPosition = NearestWalkable.andFreeFromEnemies(
                enemy, reaver, 14, 14, 4,
                reaver.enemiesNear().canAttack(reaver, 20), 8
            );

//            targetPosition = walkablePositions.groundNearestTo(reaver);
            if (targetPosition != null) targetPosition.paintCircleFilled(30, Color.Green);

            if (targetPosition == null) {
                targetPosition = enemy.position();
            }
        }

        return targetPosition;
    }
}
