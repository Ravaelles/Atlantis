package atlantis.information.generic;

import atlantis.information.enemy.EnemyUnits;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class EnemyArmyStrength {

    public static double calculate() {
        double total = 0;

        Selection allEnemyCombatUnits = EnemyUnits.visibleAndFogged().combatUnits();
        total += allEnemyCombatUnits.totalHp();
        total += allEnemyCombatUnits.melee().count() * 10;
        total += allEnemyCombatUnits.ranged().count() * 30;

        return total;
    }

}
