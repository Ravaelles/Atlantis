package atlantis.production.orders.requirements;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.Counter;
import atlantis.util.We;

public class AllowToProduceEarlyWithoutRequirements {
    public static boolean isAllowedForProtoss(AUnitType type) {
        if (!We.protoss()) return false;
        if (A.supplyUsed() >= 20) return false;

        if (earlyCyberneticsCore(type)) return true;

        return false;
    }

    private static boolean earlyCyberneticsCore(AUnitType type) {
        return type.isCyberneticsCore()
            && A.hasMinerals(176)
            && Count.gatewaysWithUnfinished() > 0;
    }

    public static boolean isGenericAllowed(AUnitType type) {
        if (!type.isABuilding()) return false;
        if (!type.is(AUnitType.Protoss_Citadel_of_Adun, AUnitType.Protoss_Templar_Archives)) return false;

        AUnitType requiredType = type.requiredUnits().first();
        if (requiredType == null) {
            A.printStackTrace("!No requirement for " + type);
            return false;
        }

        AUnit required = Select.ourWithUnfinishedOfType(requiredType).first();
        if (required == null) return false;

        boolean allow = required.getRemainingBuildTime() <= 10;
//        if (allow) {
//            System.err.println("Allowing early production of " + type + " as " + required + " is almost done.");
//        }

        return allow;
    }
}
