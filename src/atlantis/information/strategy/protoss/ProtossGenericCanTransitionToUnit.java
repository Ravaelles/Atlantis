package atlantis.information.strategy.protoss;

import atlantis.game.A;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

public class ProtossGenericCanTransitionToUnit {
    public static boolean check(AUnitType type) {
        if (type.isDarkTemplar() || type.isTemplarArchives()) return forDT();

        return true;
    }

    private static boolean forDT() {
        return A.supplyUsed() >= 140 || Count.corsairs() > 0 || Have.templarArchives();
    }
}
