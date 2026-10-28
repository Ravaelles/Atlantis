package tests.unit;

import atlantis.production.v2.ProductionGoal;
import atlantis.production.v2.ProductionRacePolicy;
import atlantis.production.v2.UnitProducible;
import atlantis.production.v2.goals.DynamicGoals;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Stargate chain at the V2 goal layer (owner: the whole mid-game tech
 * line never presents - zero Stargates in every retained macro game).
 * The legacy want is a board-driven matchup table (mutas or late bulk
 * vs Zerg, carrier tech vs Protoss, never vs Terran); V2 had no author
 * for it at all. The Corsair pump rides the same frame: eyes only with
 * a Stargate standing or warping, a standing patrol plus one per two
 * mutas, losses replaced.
 *
 * <p>Pure snapshot state, no world.</p>
 */
@Tag("production")
public class StargateCorsairTest {

    private static DynamicGoals.GameSnapshot state(
        boolean zerg, boolean protoss, int supply, int dragoons,
        int armyStrength, int combatUnits, int cannons,
        int mutas, int hydras, int lurkers, boolean carrierTech,
        int corsairs, boolean hasStargate) {
        DynamicGoals.GameSnapshot snapshot = new DynamicGoals.GameSnapshot(
            24, 1, supply, 10, supply + 10, 600, false, combatUnits, 1,
            cannons, zerg, protoss, mutas,
            300, true, dragoons, 1, false, armyStrength);
        snapshot.enemyHydras = hydras;
        snapshot.enemyLurkers = lurkers;
        snapshot.enemyCarrierTech = carrierTech;
        snapshot.combatUnits = combatUnits;
        snapshot.corsairs = corsairs;
        snapshot.hasStargate = hasStargate;

        return snapshot.atBase(100, 60);
    }

    private static List<ProductionGoal> contribute(DynamicGoals.GameSnapshot snapshot) {
        return DynamicGoals.contribute(snapshot, ProductionRacePolicy.forRace("Protoss"));
    }

    private static boolean hasGoal(List<ProductionGoal> goals, AUnitType type) {
        String id = UnitProducible.of(type).id();

        return goals.stream().anyMatch(goal -> goal.item().id().equals(id));
    }

    @Test
    public void mutasOrderStargateAndEyes() {
        DynamicGoals.GameSnapshot snapshot = state(
            true, false, 60, 6, 200, 8, 2, 4, 0, 0, false, 0, false);
        List<ProductionGoal> goals = contribute(snapshot);

        assertTrue(hasGoal(goals, AUnitType.Protoss_Stargate),
            "mutas in the sky order the building: " + goals);
        assertFalse(hasGoal(goals, AUnitType.Protoss_Corsair),
            "no eyes before the building stands or warps: " + goals);
    }

    @Test
    public void standingStargatePumpsCorsairsAgainstMutas() {
        DynamicGoals.GameSnapshot snapshot = state(
            true, false, 60, 6, 200, 8, 2, 4, 0, 0, false, 0, true);

        assertTrue(hasGoal(contribute(snapshot), AUnitType.Protoss_Corsair),
            "4 mutas want 4 eyes, none standing: " + contribute(snapshot));
    }

    @Test
    public void fullPatrolHoldsFire() {
        DynamicGoals.GameSnapshot snapshot = state(
            true, false, 60, 6, 200, 8, 2, 0, 0, 0, false, 3, true);

        assertFalse(hasGoal(contribute(snapshot), AUnitType.Protoss_Corsair),
            "3 standing cover the patrol of 2: " + contribute(snapshot));
    }

    @Test
    public void earlyZergStaysGrounded() {
        DynamicGoals.GameSnapshot snapshot = state(
            true, false, 30, 2, 60, 3, 1, 0, 0, 0, false, 0, false);
        List<ProductionGoal> goals = contribute(snapshot);

        assertFalse(hasGoal(goals, AUnitType.Protoss_Stargate),
            "supply 30 is no Stargate: " + goals);
        assertFalse(hasGoal(goals, AUnitType.Protoss_Corsair),
            "and no eyes either: " + goals);
    }

    @Test
    public void carrierTechOrdersStargateVsProtoss() {
        DynamicGoals.GameSnapshot snapshot = state(
            false, true, 100, 13, 400, 16, 2, 0, 0, 0, true, 0, false);

        assertTrue(hasGoal(contribute(snapshot), AUnitType.Protoss_Stargate),
            "13 dragoons facing carriers order the building: " + contribute(snapshot));
    }

    @Test
    public void terranNeverOrdersStargate() {
        DynamicGoals.GameSnapshot snapshot = state(
            false, false, 100, 13, 400, 16, 2, 0, 0, 0, false, 0, false);

        assertFalse(hasGoal(contribute(snapshot), AUnitType.Protoss_Stargate),
            "the legacy branch answers false vs Terran: " + contribute(snapshot));
    }
}
