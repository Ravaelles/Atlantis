package atlantis.production.dynamic.protoss.units;

import atlantis.game.A;
import atlantis.information.decisions.Decisions;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;

import static atlantis.production.AbstractDynamicUnits.buildToHave;

public class ProduceHighTemplar {
    public static boolean ht() {
        if (Have.no(requiredBuilding())) return false;

        if (
            Have.no(AUnitType.Protoss_Robotics_Facility)
                || Have.no(AUnitType.Protoss_Robotics_Support_Bay)
        ) return false;

        int maxReavers = haveThisManyReavers();

        buildToHave(AUnitType.Protoss_Reaver, maxReavers);
        return false;
    }

    private static AUnitType requiredBuilding() {
        return AUnitType.Protoss_Templar_Archives;
    }

    private static int haveThisManyReavers() {
        return Decisions.isEnemyGoingAirAndWeAreNotPreparedEnough()
            ? 0
            : (1 + A.supplyUsed() / 45);
    }
}
