package atlantis.information.generic;

import atlantis.information.enemy.EnemyUnits;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class EnemyArmyStrength {
    public static double calculate() {
        return calculateFrom(EnemyUnits.discovered().combatUnits());
    }

    public static double calculateWithoutCB() {
        return calculateFrom(EnemyUnits.discovered().combatUnits().nonBuildings());
    }

    private static double calculateFrom(Selection combatUnits) {
        double total = 0;

        total += combatUnits.totalHp();
        total += combatUnits.melee().count() * 10;
        total += combatUnits.ranged().count() * 30;

        total -= combatUnits.combatBuildings(true).count() * 50;
        total -= combatUnits.bases().count() * 100;

        return Math.max(1, total);
    }
}
