package atlantis.production.dynamic.protoss.units;

import atlantis.game.A;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;

import static atlantis.production.dynamic.AbstractDynamicUnits.buildToHave;

public class ProduceHighTemplar {
    public static boolean ht() {
        if (true) return false; // Always disabled

        if (Have.no(requiredBuilding())) return false;

        // Owner 2026-11-09: High Templar only after three Dark Templar
        // were produced in this game (history, not standing units).
        if (atlantis.game.util.CompletedUnitsHistory.completedCount(
            AUnitType.Protoss_Dark_Templar) < atlantis.production.demand.DynamicGoals.HIGH_TEMPLAR_MIN_DARK_TEMPLAR) {
            return false;
        }

        int maxHT = haveThisManyHT();

        return buildToHave(type(), maxHT);
    }

    private static AUnitType type() {
        return AUnitType.Protoss_High_Templar;
    }

    private static AUnitType requiredBuilding() {
        return AUnitType.Protoss_Templar_Archives;
    }

    private static int haveThisManyHT() {
        if (Have.no(requiredBuilding())) return 0;

        if (A.supplyUsed() <= 145) return 2;
        if (A.supplyUsed() <= 160) return 3;
        if (A.supplyUsed() <= 180) return 4;

        return 6;
//        return Math.max(Count.dragoons() / 4, 8);
    }
}
