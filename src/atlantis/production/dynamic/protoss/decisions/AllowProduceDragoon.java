package atlantis.production.dynamic.protoss.decisions;

import atlantis.game.A;
import atlantis.information.strategy.Strategy;
import atlantis.production.dynamic.protoss.units.ProduceDarkTemplar;
import atlantis.units.select.Select;

import static atlantis.units.AUnitType.Protoss_Citadel_of_Adun;
import static atlantis.units.AUnitType.Protoss_Templar_Archives;

public class AllowProduceDragoon {
    public static boolean allowed() {
        if (A.supplyUsed(188)) return false;
        if (A.supplyUsed(184) && (A.supplyFree() <= 4 || A.gas() <= 200)) return false;

        if (A.minerals() >= 500) return true;
        if (ProduceDarkTemplar.requested >= 2 && A.minerals() >= 350) return true;

        if (Strategy.get().isGoingHiddenUnits()) {
            if (Select.ourWithUnfinishedOfType(Protoss_Templar_Archives).notEmpty()) return false;
            if (Select.ourWithUnfinishedOfType(Protoss_Citadel_of_Adun).notEmpty()) return false;
        }

        return true;
    }
}
