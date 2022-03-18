package atlantis.protoss;

import atlantis.combat.micro.avoid.AAvoidEnemies;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class ProtossReaver {

    public static boolean update(AUnit reaver) {
        if (reaver.scarabCount() <= 0) {
            reaver.setTooltipTactical("NoScarab");
            return AAvoidEnemies.avoidEnemiesIfNeeded(reaver);
        }

        if (reaver.cooldownRemaining() >= 10) {
            return false;
        }

        Selection enemiesInRange = reaver.enemiesNear();
        AUnit enemy;

        // First attack very close enemies
        if ((enemy = enemiesInRange.canBeAttackedBy(reaver, 0).nearestTo(reaver)) != null) {
            reaver.attackUnit(enemy);
            reaver.setTooltipTactical("Tasty" + enemy.name());
            return true;
        }

        // If no very close enemy, then attack the one most distant
        if ((enemy = enemiesInRange.canBeAttackedBy(reaver, 7).nearestTo(reaver)) != null) {
            reaver.attackUnit(enemy);
            reaver.setTooltipTactical("Nice" + enemy.name());
            return true;
        }

        return false;
    }

}
