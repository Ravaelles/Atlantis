package atlantis.production.dynamic.protoss.buildings;

import atlantis.information.tech.ATechRequests;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import bwapi.TechType;

import static atlantis.units.AUnitType.Protoss_Arbiter;
import static atlantis.units.AUnitType.Protoss_Arbiter_Tribunal;
import static atlantis.util.Helpers.has;
import static atlantis.util.Helpers.hasFree;

public class ProduceArbiterTribunal {
    public static void produce() {
        DynamicCommanderHelpers.buildToHaveOne(90, Protoss_Arbiter_Tribunal);

        if (hasFree(Protoss_Arbiter_Tribunal) && has(Protoss_Arbiter)) {
            ATechRequests.researchTech(TechType.Stasis_Field);
        }
    }
}
