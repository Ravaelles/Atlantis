package atlantis.enemy;

import atlantis.units.AUnit;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

public class EnemyInformation {

    public static boolean enemyStartedWithDefensiveBuilding = false;

    public static boolean isEnemyNearAnyOurBuilding() {
        return enemyNearAnyOurBuilding() != null;
    }

    public static AUnit enemyNearAnyOurBuilding() {
        if (!Have.base()) {
            return null;
        }

        AUnit nearestEnemy = Select.enemyCombatUnits().nearestTo(Select.main());
        if (nearestEnemy != null) {
            return Select.ourBuildings().inRadius(13, nearestEnemy).atLeast(1)
                    ? nearestEnemy : null;
        }

        return null;
    }

}
