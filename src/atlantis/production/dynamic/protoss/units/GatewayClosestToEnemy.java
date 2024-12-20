package atlantis.production.dynamic.protoss.units;

import atlantis.information.enemy.EnemyInfo;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

import static atlantis.units.AUnitType.Protoss_Gateway;

public class GatewayClosestToEnemy {
    public static AUnit get() {
        APosition enemyPosition = EnemyInfo.enemyLocationOrGuess();

        if (enemyPosition == null) return Select.ourFree(Protoss_Gateway).random();

        return Select.ourFree(Protoss_Gateway).groundNearestTo(enemyPosition);
    }
}
