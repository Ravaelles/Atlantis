package atlantis.production.dynamic.terran.units;

import atlantis.game.A;
import atlantis.information.generic.Army;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

public class MechInsteadOfInfantry {
    public static boolean check() {
        if (!A.hasGas(100)) return false;
        if (!Have.machineShop()) return false;

        if (Count.basesWithUnfinished() >= 2) {
            return A.hasMinerals(300);
        }

        return Army.strength() >= 90;
    }
}
