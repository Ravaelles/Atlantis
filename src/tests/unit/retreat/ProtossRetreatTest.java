package tests.unit.retreat;

import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;
import tests.unit.AbstractTestWithUnits;
import tests.unit.helpers.protoss.RetreatScenarioTest;
import tests.unit.helpers.protoss.RetreatTestGoonsVsHydras;
import tests.unit.helpers.protoss.UnitsForRetreatTest;

import static org.junit.jupiter.api.Assertions.*;

public class ProtossRetreatTest extends AbstractTestWithUnits {
    private RetreatScenarioTest scenario;

    @Test
    public void retreatGoonsVsHydras() {
        assertFalse(RetreatTestGoonsVsHydras.testWith(2, 1).retreatManagerApplied);
        assertFalse(RetreatTestGoonsVsHydras.testWith(2, 2).retreatManagerApplied);
        assertTrue(RetreatTestGoonsVsHydras.testWith(1, 2).retreatManagerApplied);
        assertTrue(RetreatTestGoonsVsHydras.testWith(1, 3).retreatManagerApplied);
        assertTrue(RetreatTestGoonsVsHydras.testWith(4, 9).retreatManagerApplied);
    }

    @Test
    public void goonsVsCannons() {
        scenario = new RetreatScenarioTest(
            UnitsForRetreatTest.ours(AUnitType.Protoss_Dragoon, 1),
            UnitsForRetreatTest.enemies(AUnitType.Protoss_Photon_Cannon, 1)
        );

        assertTrue(scenario.retreatManagerApplied);

        scenario = new RetreatScenarioTest(
            UnitsForRetreatTest.ours(AUnitType.Protoss_Dragoon, 2),
            UnitsForRetreatTest.enemies(AUnitType.Protoss_Photon_Cannon, 2)
        );

        assertTrue(scenario.retreatManagerApplied);

        scenario = new RetreatScenarioTest(
            UnitsForRetreatTest.ours(AUnitType.Protoss_Dragoon, 2),
            UnitsForRetreatTest.enemies(AUnitType.Protoss_Photon_Cannon, 1)
        );

        assertFalse(scenario.retreatManagerApplied);

        scenario = new RetreatScenarioTest(
            UnitsForRetreatTest.ours(AUnitType.Protoss_Dragoon, 3),
            UnitsForRetreatTest.enemies(AUnitType.Protoss_Photon_Cannon, 1)
        );

        assertFalse(scenario.retreatManagerApplied);
    }

    @Test
    public void goonsVsGoons() {
        scenario = new RetreatScenarioTest(
            UnitsForRetreatTest.ours(AUnitType.Protoss_Dragoon, 3),
            UnitsForRetreatTest.enemies(AUnitType.Protoss_Dragoon, 2)
        );

        assertFalse(scenario.retreatManagerApplied);

        scenario = new RetreatScenarioTest(
            UnitsForRetreatTest.ours(AUnitType.Protoss_Dragoon, 3),
            UnitsForRetreatTest.enemies(AUnitType.Protoss_Dragoon, 3)
        );

        assertFalse(scenario.retreatManagerApplied);

        scenario = new RetreatScenarioTest(
            UnitsForRetreatTest.ours(AUnitType.Protoss_Dragoon, 3),
            UnitsForRetreatTest.enemies(AUnitType.Protoss_Dragoon, 4)
        );

        assertTrue(scenario.retreatManagerApplied);
    }
}
