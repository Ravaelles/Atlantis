package atlantis.production.dynamic.protoss.decisions;

import atlantis.game.A;
import atlantis.information.generic.Army;
import atlantis.information.strategy.Strategy;
import atlantis.production.dynamic.protoss.units.ProduceDarkTemplar;
import atlantis.units.select.Select;

import static atlantis.units.AUnitType.Protoss_Citadel_of_Adun;
import static atlantis.units.AUnitType.Protoss_Templar_Archives;

public class AllowProduceZealot {
    public static boolean allowed() {
        if (A.supplyUsed(180) && (A.gas() >= 100 || Army.strength() >= 130)) return false;
        if (A.supplyUsed(188)) return false;
        if (A.supplyUsed(175) && A.supplyFree() <= 4) return false;

        if (A.minerals() >= 275) return true;

        if (Strategy.get().isGoingHiddenUnits() && A.minerals() <= 300) {
            if (Select.ourWithUnfinishedOfType(Protoss_Templar_Archives).notEmpty()) return false;
            if (Select.ourWithUnfinishedOfType(Protoss_Citadel_of_Adun).notEmpty()) return false;
        }

        return true;
    }
}
