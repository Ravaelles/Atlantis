package atlantis.production.dynamic.protoss.buildings;

import atlantis.production.dynamic.DynamicCommanderHelpers;

import static atlantis.units.AUnitType.Protoss_Stargate;

public class ProduceStargate {
    public static void produce() {
        DynamicCommanderHelpers.buildToHaveOne(80, Protoss_Stargate);
    }
}
