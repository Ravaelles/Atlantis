package atlantis.protoss;

import atlantis.combat.micro.avoid.AAvoidUnits;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class ProtossReaver {

    public static boolean update(AUnit reaver) {
        if (reaver.scarabCount() <= 0) {
            reaver.setTooltipTactical("NoScarab");
            return AAvoidUnits.avoidEnemiesIfNeeded(reaver);
        }

        Selection enemiesInRange = Select.enemyRealUnits().inRadius(8, reaver);
        AUnit enemy;

        // First attack very close enemies
        if ((enemy = enemiesInRange.clone().inRadius(5, reaver).nearestTo(reaver)) != null) {
            reaver.attackUnit(enemy);
            reaver.setTooltipTactical("Near" + enemy.name());
            return true;
        }

        // If no very close enemy, then attack the one most distant
        if ((enemy = enemiesInRange.clone().nearestTo(reaver)) != null) {
            reaver.attackUnit(enemy);
            reaver.setTooltipTactical("Far" + enemy.name());
            return true;
        }

        return false;
    }

}
