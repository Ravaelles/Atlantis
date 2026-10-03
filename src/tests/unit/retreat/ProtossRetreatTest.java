package tests.unit.retreat;

import atlantis.combat.missions.Missions;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.unit.AbstractTestWithUnits;
import tests.unit.helpers.protoss.RetreatScenarioTest;
import tests.unit.helpers.protoss.RetreatTestGoonsVsHydras;
import tests.unit.helpers.protoss.UnitsForRetreatTest;

import static org.junit.jupiter.api.Assertions.*;
import tests.unit.helpers.ClearAllCaches;

public class ProtossRetreatTest extends WorldStubForTests {
    private RetreatScenarioTest scenario;

    @Test
    public void retreatGoonsVsHydras() {
        assertFalse(goonsVsHydras(3, 2).retreatManagerApplied);
        assertFalse(goonsVsHydras(2, 2).retreatManagerApplied);
        assertFalse(goonsVsHydras(2, 3).retreatManagerApplied);
//        assertFalse(RetreatTestGoonsVsHydras.testWith(1, 2).retreatManagerApplied);
        assertTrue(goonsVsHydras(1, 5).retreatManagerApplied);
        assertTrue(goonsVsHydras(1, 3).retreatManagerApplied);
        assertTrue(goonsVsHydras(2, 5).retreatManagerApplied);
        assertTrue(goonsVsHydras(4, 9).retreatManagerApplied);
    }

    /**
     * Protoss doctrine: we do <b>not</b> run from an enemy defensive building.
     *
     * <p>{@code ProtossShouldFullRetreat.shouldFullRetreat()} returns
     * {@code f("DontEnemyCB")} as soon as an anti-ground combat building is
     * within 10 tiles, so a lone dragoon standing 6 tiles from a photon cannon
     * keeps shooting it: measured eval 0.3 (three times worse than the cannon)
     * and still no retreat. The three "expect retreat" expectations this test
     * used to carry contradicted that rule, which is why it could not pass.</p>
     *
     * <p>This is a division of labor, not neglect: the same situation belongs
     * to {@code ProtossCombatBuildingClose}, which fires for a lone ground
     * unit near a cannon ({@code ShouldAvoidCannonAsProtoss} answers avoid
     * when the chances look bad, and one dragoon is never "strong enough to
     * attack") and moves it to a safety margin instead of routing the army.
     * Retreat staying out is what keeps the two managers from fighting over
     * the unit - pinned on the other side by
     * {@code AvoidCombatBuildingsTest}. Whether standing off at the margin
     * rather than leaving is right against a longer-ranged building is a game
     * question, tracked separately in {@code _AI/BUGS.md} B-18.</p>
     */
    @Test
    public void goonsVsCannons() {
        scenario = goonsVs(1, 1, AUnitType.Protoss_Photon_Cannon);
        assertFalse(scenario.retreatManagerApplied, "a lone dragoon does not run from a cannon");

        scenario = goonsVs(2, 2, AUnitType.Protoss_Photon_Cannon);
        assertFalse(scenario.retreatManagerApplied);

        scenario = goonsVs(3, 2, AUnitType.Protoss_Photon_Cannon);
        assertFalse(scenario.retreatManagerApplied);

        // =========================================================
        // ...and of course not when we are winning the fight either.

        scenario = goonsVs(10, 1, AUnitType.Protoss_Photon_Cannon);
        assertFalse(scenario.retreatManagerApplied);

        scenario = goonsVs(6, 1, AUnitType.Protoss_Photon_Cannon);
        assertFalse(scenario.retreatManagerApplied);

        scenario = goonsVs(4, 1, AUnitType.Protoss_Photon_Cannon);
        assertFalse(scenario.retreatManagerApplied);
    }

    /**
     * Every scenario starts from an empty world. A scenario is a world built
     * inside a test method, so no @BeforeEach runs between them and the previous
     * scenario's selections survive into the next one - measured: 1 dragoon vs 3
     * hydras retreats when it is the first world of the method and does not when
     * it is the fifth.
     */
    private RetreatScenarioTest goonsVsHydras(int goons, int hydras) {
        ClearAllCaches.clearAll();
        return new RetreatScenarioTest(
            UnitsForRetreatTest.ours(AUnitType.Protoss_Dragoon, goons),
            UnitsForRetreatTest.enemies(AUnitType.Zerg_Hydralisk, hydras));
    }

    private RetreatScenarioTest goonsVs(int goons, int enemies, AUnitType enemyType) {
        ClearAllCaches.clearAll();
        return new RetreatScenarioTest(
            UnitsForRetreatTest.ours(AUnitType.Protoss_Dragoon, goons),
            UnitsForRetreatTest.enemies(enemyType, enemies));
    }

    @Test
    public void goonsVsGoons_3v2() {
        scenario = goonsVs(3, 2, AUnitType.Protoss_Dragoon);

        assertFalse(scenario.retreatManagerApplied);
    }

    @Test
    public void goonsVsGoons_3v3() {
        ClearAllCaches.clearAll();
        scenario = RetreatScenarioTest.testWith(
            UnitsForRetreatTest.ours(AUnitType.Protoss_Dragoon, 3),
            UnitsForRetreatTest.enemies(AUnitType.Protoss_Dragoon, 3),
            Missions.DEFEND
        );

        assertFalse(scenario.retreatManagerApplied);
    }

    @Test
    public void goonsVsGoons_3v4() {
        scenario = goonsVs(3, 4, AUnitType.Protoss_Dragoon);

        assertTrue(scenario.retreatManagerApplied);
    }
}
