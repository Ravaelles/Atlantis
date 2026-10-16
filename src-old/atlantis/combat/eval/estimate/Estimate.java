package atlantis.combat.eval.estimate;

import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class Estimate {
    public static double forSelection(Selection selection) {
        double total = 0;

        for (AUnit unit : selection.list()) {
            total += unit.estimate();
        }

        return total;
    }

    public static double unit(AUnit unit) {
        if (!unit.hasAnyWeapon()) return 0;

        return unit.type().dpsGround();
    }
}
