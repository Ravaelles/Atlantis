package tests.unit;

import atlantis.production.demand.ProductionGoal;
import atlantis.production.policy.ProductionRacePolicy;
import atlantis.production.producibles.UnitProducible;
import atlantis.production.demand.DynamicGoals;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Citadel + Leg Enhancements at the V2 goal layer (legacy
 * ProduceCitadelOfAdun / ResearchLegEnhancements, verbatim gates):
 * the Citadel wants supply 140+ with gas 180 and an Observatory
 * standing; Legs want the Citadel past supply 90 with 4+ zealots.
 * Research coverage is execution-side, so no researched flag rides
 * the snapshot.
 *
 * <p>Pure snapshot state, no world.</p>
 */
@Tag("production")
public class CitadelLegsTest {

    private static DynamicGoals.GameSnapshot state(
        int supply, int gas, int zealots, boolean obs, boolean citadel) {
        DynamicGoals.GameSnapshot snapshot = new DynamicGoals.GameSnapshot(
            24, 1, supply, 10, supply + 10, 600, false, 10, 1,
            0, true, false, 0, gas, true, 6, 1, false, 200);
        snapshot.zealots = zealots;
        snapshot.hasObservatory = obs;
        snapshot.hasCitadel = citadel;

        return snapshot.atBase(100, 60);
    }

    private static List<ProductionGoal> contribute(DynamicGoals.GameSnapshot snapshot) {
        return DynamicGoals.contribute(snapshot, ProductionRacePolicy.forRace("Protoss"));
    }

    private static boolean hasGoal(List<ProductionGoal> goals, AUnitType type) {
        String id = UnitProducible.of(type).id();

        return goals.stream().anyMatch(goal -> goal.item().id().equals(id));
    }

    private static boolean hasUpgrade(List<ProductionGoal> goals, bwapi.UpgradeType type) {
        String id = atlantis.production.producibles.UpgradeProducible.of(type).id();

        return goals.stream().anyMatch(goal -> goal.item().id().equals(id));
    }

    @Test
    public void richLateGameOrdersCitadel() {
        assertTrue(hasGoal(contribute(state(150, 200, 6, true, false)),
            AUnitType.Protoss_Citadel_of_Adun),
            "140+ with gas and eyes standing orders the Citadel");
    }

    @Test
    public void noEyesNoCitadel() {
        assertFalse(hasGoal(contribute(state(150, 200, 6, false, false)),
            AUnitType.Protoss_Citadel_of_Adun),
            "without the Observatory the Citadel waits");
    }

    @Test
    public void midGameNeedsNothing() {
        List<ProductionGoal> goals = contribute(state(100, 150, 6, true, false));

        assertFalse(hasGoal(goals, AUnitType.Protoss_Citadel_of_Adun),
            "supply 100 never orders the Citadel");
    }

    @Test
    public void citadelWithZealotsResearchesLegs() {
        assertTrue(hasUpgrade(contribute(state(100, 150, 6, true, true)),
            bwapi.UpgradeType.Leg_Enhancements),
            "Citadel past 90 with 6 zealots researches speed");
    }

    @Test
    public void fewZealotsHoldLegs() {
        assertFalse(hasUpgrade(contribute(state(100, 150, 2, true, true)),
            bwapi.UpgradeType.Leg_Enhancements),
            "2 zealots hold the research");
    }

    @Test
    public void noCitadelNoLegs() {
        assertFalse(hasUpgrade(contribute(state(100, 150, 6, true, false)),
            bwapi.UpgradeType.Leg_Enhancements),
            "without the building there is no research");
    }
}
