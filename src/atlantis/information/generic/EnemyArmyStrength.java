package atlantis.information.generic;

import atlantis.information.enemy.EnemyUnits;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class EnemyArmyStrength {

    public static double calculate() {
        double total = 0;

        Selection combatUnits = Select.enemyCombatUnits()
                .add(EnemyUnits.unitsDiscoveredSelection().combatUnits())
                .removeDuplicates();
        total += combatUnits.totalHp();
        total += combatUnits.melee().count() * 10;
        total += combatUnits.ranged().count() * 30;

        return total;
    }

}
