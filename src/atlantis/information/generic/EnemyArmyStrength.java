package atlantis.information.generic;

import atlantis.information.enemy.EnemyUnits;
import atlantis.units.select.Selection;

public class EnemyArmyStrength {

    public static double calculate() {
        double total = 0;

        Selection allEnemyCombatUnits = EnemyUnits.discovered().combatUnits();
        total += allEnemyCombatUnits.totalHp();
        total += allEnemyCombatUnits.melee().count() * 10;
        total += allEnemyCombatUnits.ranged().count() * 30;

        total -= allEnemyCombatUnits.combatBuildings(true).count() * 50;
        total -= allEnemyCombatUnits.bases().count() * 100;

        return Math.max(1, total);
    }

}
