package atlantis.information.generic;

import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class OurArmy {
    /**
     * Relative to enemy in %.
     * <p>
     * 80 means that our army is 80% as strong as enemy's army.
     */
    public static int strength() {
        return ArmyStrength.ourArmyRelativeStrength();
    }

    public static double calculate() {
        double total = 0;

        Selection combatUnits = Select.ourCombatUnits();
        total += combatUnits.totalHp();
        total += combatUnits.melee().count() * 10;
        total += combatUnits.ranged().count() * 30;

        return Math.max(1, total);
    }
}
