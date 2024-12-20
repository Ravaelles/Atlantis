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

    public static int strengthWithoutCB() {
        return ArmyStrength.ourArmyRelativeStrengthWithoutCB();
    }

    protected static double calculate() {
        return calculateFrom(Select.ourCombatUnits());
    }

    protected static double calculateWithoutCB() {
        return calculateFrom(Select.ourCombatUnits().nonBuildings());
    }

    protected static double calculateFrom(Selection combatUnits) {
        double total = 0;

        total += combatUnits.totalHp();
        total += combatUnits.melee().count() * 10;
        total += combatUnits.ranged().count() * 30;

        total -= combatUnits.combatBuildings(true).count() * 50;
        total -= combatUnits.bases().count() * 100;

        return Math.max(1, total);
    }
}
