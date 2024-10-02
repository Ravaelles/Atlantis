package atlantis.production.dynamic.protoss.buildings;

import atlantis.information.tech.ATechRequests;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;
import bwapi.TechType;

import static atlantis.units.AUnitType.Protoss_Arbiter;
import static atlantis.units.AUnitType.Protoss_Arbiter_Tribunal;
import static atlantis.util.Helpers.has;
import static atlantis.util.Helpers.hasFree;

public class ProduceArbiterTribunal {
    public static boolean produce() {
        if (Have.a(type())) return false;

        return DynamicCommanderHelpers.buildToHaveOne(90, type());

//        if (hasFree(type()) && has(Protoss_Arbiter)) {
//            ATechRequests.researchTech(TechType.Stasis_Field);
//        }
//        return false;
    }

    private static AUnitType type() {
        return Protoss_Arbiter_Tribunal;
    }
}
