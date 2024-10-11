package atlantis.production.dynamic.protoss.units;

import atlantis.game.A;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;

import static atlantis.production.AbstractDynamicUnits.buildToHave;

public class ProduceDarkTemplar {
    private static int requested = 0;

    public static boolean dt() {
        if (true) return false;

        if (Have.no(requiredBuilding())) return false;
        if (requested >= 1) return false;

        int maxDT = haveThisManyHT();
        return produce(maxDT);
    }

    private static boolean produce(int maxDT) {
        if (buildToHave(type(), maxDT)) {
            requested++;
            return true;
        }

        return false;
    }

    private static AUnitType type() {
        return AUnitType.Protoss_Dark_Templar;
    }

    private static AUnitType requiredBuilding() {
        return AUnitType.Protoss_Templar_Archives;
    }

    private static int haveThisManyHT() {
        if (A.supplyUsed() <= 160) return 1;

        return 2;
    }
}
