package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.*;

public class ProduceCitadelOfAdun {
    public static boolean produce() {
        if (A.supplyUsed() <= 60) return false;
        if (Have.a(type())) return false;

        if (Have.notEvenPlanned(type())) {
            if (DynamicCommanderHelpers.buildNow(type(), true)) return true;
        }

        return false;
    }

    private static AUnitType type() {
        return Protoss_Citadel_of_Adun;
    }
}
