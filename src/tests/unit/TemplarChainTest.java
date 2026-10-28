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
 * Templar chain at the V2 goal layer (legacy ProduceTemplarArchives,
 * ProduceHighTemplar, ResearchPsionicStorm, ProduceDarkTemplar):
 * Archives open vs Zerg on bank or supply, HT up to a supply cap,
 * Storm once a caster stands, DT up to a supply cap unless a
 * Reaver-less Support Bay outranks them.
 *
 * <p>Pure snapshot state, no world.</p>
 */
@Tag("production")
public class TemplarChainTest {

    private static DynamicGoals.GameSnapshot state(
        int supply, int gas, int combat, int strength,
        boolean archives, int ht, int dt, boolean bay, int reavers) {
        DynamicGoals.GameSnapshot snapshot = new DynamicGoals.GameSnapshot(
            24, 1, supply, 10, supply + 10, 600, false, combat, 1,
            0, true, false, 0, gas, true, 6, 1, false, strength);
        snapshot.hasArchives = archives;
        snapshot.highTemplar = ht;
        snapshot.darkTemplar = dt;
        snapshot.hasSupportBay = bay;
        snapshot.reavers = reavers;

        return snapshot.atBase(100, 60);
    }

    private static List<ProductionGoal> contribute(DynamicGoals.GameSnapshot snapshot) {
        return DynamicGoals.contribute(snapshot, ProductionRacePolicy.forRace("Protoss"));
    }

    private static boolean hasGoal(List<ProductionGoal> goals, AUnitType type) {
        String id = UnitProducible.of(type).id();

        return goals.stream().anyMatch(goal -> goal.item().id().equals(id));
    }

    private static boolean hasTech(List<ProductionGoal> goals, bwapi.TechType type) {
        String id = atlantis.production.v2.TechProducible.of(type).id();

        return goals.stream().anyMatch(goal -> goal.item().id().equals(id));
    }

    @Test
    public void fundedLineOpensArchives() {
        assertTrue(hasGoal(contribute(state(100, 250, 20, 200, false, 0, 0, false, 0)),
            AUnitType.Protoss_Templar_Archives),
            "banked line vs Zerg opens the Archives");
    }

    @Test
    public void poorEarlyLineStaysShut() {
        assertFalse(hasGoal(contribute(state(80, 100, 10, 100, false, 0, 0, false, 0)),
            AUnitType.Protoss_Templar_Archives),
            "supply 80 with 100 gas opens nothing");
    }

    @Test
    public void archivesTrainTemplar() {
        List<ProductionGoal> goals = contribute(state(100, 250, 20, 200, true, 0, 0, false, 0));

        assertTrue(hasGoal(goals, AUnitType.Protoss_High_Templar),
            "Archives at 100 want Templar: " + goals);
        assertTrue(hasGoal(goals, AUnitType.Protoss_Dark_Templar),
            "and the unseen blade: " + goals);
    }

    @Test
    public void fullCasteHoldsFire() {
        List<ProductionGoal> goals = contribute(state(100, 250, 20, 200, true, 2, 3, false, 0));

        assertFalse(hasGoal(goals, AUnitType.Protoss_High_Templar),
            "2 HT at supply 100 hold");
        assertFalse(hasGoal(goals, AUnitType.Protoss_Dark_Templar),
            "3 DT at supply 100 hold");
    }

    @Test
    public void casterCallsTheStorm() {
        assertTrue(hasTech(contribute(state(100, 250, 20, 200, true, 1, 0, false, 0)),
            bwapi.TechType.Psionic_Storm),
            "a standing caster researches the storm");
    }

    @Test
    public void noCasterNoStorm() {
        assertFalse(hasTech(contribute(state(100, 250, 20, 200, true, 0, 0, false, 0)),
            bwapi.TechType.Psionic_Storm),
            "no caster, no storm");
    }

    @Test
    public void reaverlessBayOutranksFirstDt() {
        assertFalse(hasGoal(contribute(state(100, 250, 20, 200, true, 0, 0, true, 0)),
            AUnitType.Protoss_Dark_Templar),
            "the gas builds the Reaver first");
    }
}
