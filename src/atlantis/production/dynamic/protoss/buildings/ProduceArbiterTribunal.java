package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.information.generic.Army;
import atlantis.information.tech.ATechRequests;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;
import bwapi.TechType;

import static atlantis.units.AUnitType.Protoss_Arbiter;
import static atlantis.units.AUnitType.Protoss_Arbiter_Tribunal;

public class ProduceArbiterTribunal {
    public static boolean produce() {
        if (A.supplyUsed() <= A.whenEnemyProtossTerranZerg(170, 120, 180)) return false;

        if (!Have.roboticsSupportBay()) return false;
        if (Have.a(type())) return false;

        if (Army.strength() <= 130 && !A.canAfford(400, 300)) {
            return false;
        }

        return DynamicCommanderHelpers.buildToHaveOne(90, type());

//        if (Count.ofTypeFree(type()) > 0 && Count.ofType(AUnitType.Protoss_Arbiter) > 0) {
//            ATechRequests.researchTech(TechType.Stasis_Field);
//        }
//        return false;
    }

    private static AUnitType type() {
        return Protoss_Arbiter_Tribunal;
    }
}
