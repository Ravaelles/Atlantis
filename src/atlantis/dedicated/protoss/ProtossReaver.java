package atlantis.dedicated.protoss;

import atlantis.combat.micro.avoid.AAvoidUnits;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class ProtossReaver {

    public static boolean update(AUnit reaver) {
        if (reaver.scarabCount() <= 0) {
            reaver.setTooltip("NoScarab");
            return AAvoidUnits.avoidEnemiesIfNeeded(reaver);
        }

        Select<? extends AUnit> enemiesInRange = Select.enemyRealUnits().inRadius(8, reaver);
        AUnit enemy;

        // First attack very close enemies
        if ((enemy = enemiesInRange.clone().inRadius(5, reaver).nearestTo(reaver)) != null) {
            reaver.attackUnit(enemy);
            reaver.setTooltip("Near" + enemy.shortName());
            return true;
        }

        // If no very close enemy, then attack the one most distant
        if ((enemy = enemiesInRange.clone().nearestTo(reaver)) != null) {
            reaver.attackUnit(enemy);
            reaver.setTooltip("Far" + enemy.shortName());
            return true;
        }

        return false;
    }

}
