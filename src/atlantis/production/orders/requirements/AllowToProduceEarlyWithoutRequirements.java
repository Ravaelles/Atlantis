package atlantis.production.orders.requirements;

import atlantis.game.A;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;

public class AllowToProduceEarlyWithoutRequirements {
    public static boolean isAllowed(AUnitType type) {
        if (A.supplyUsed() >= 20) return false;

        if (earlyCyberneticsCore(type)) return true;

        return false;
    }

    private static boolean earlyCyberneticsCore(AUnitType type) {
        return type.isCyberneticsCore()
            && A.hasMinerals(176)
            && Count.gatewaysWithUnfinished() > 0;
    }
}
