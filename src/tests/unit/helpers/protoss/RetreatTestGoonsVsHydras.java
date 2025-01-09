package tests.unit.helpers.protoss;

import atlantis.units.AUnitType;

public class RetreatTestGoonsVsHydras {
    public static RetreatScenarioTest testWith(int goons, int hydras) {
        return new RetreatScenarioTest(
            UnitsForRetreatTest.ours(AUnitType.Protoss_Dragoon, goons),
            UnitsForRetreatTest.enemies(AUnitType.Zerg_Hydralisk, hydras)
        );
    }
}
