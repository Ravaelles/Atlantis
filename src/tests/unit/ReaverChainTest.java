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
 * Reaver chain at the V2 goal layer (legacy ProduceRoboticsSupportBay,
 * ProduceReavers, ProduceShuttle, ProduceArbiterTribunal,
 * ProduceArbiters): the Bay opens the line, Reavers pump to a banked
 * cap with eyes first vs air, Shuttles follow Reavers, Tribunal and
 * two Arbiters close the late line.
 *
 * <p>Pure snapshot state, no world.</p>
 */
@Tag("production")
public class ReaverChainTest {

    private static DynamicGoals.GameSnapshot state(
        int supply, int minerals, int gas, int strength, int combat,
        boolean robo, boolean bay, int reavers, int shuttles,
        int observers, int mutas, boolean tribunal, int arbiters) {
        DynamicGoals.GameSnapshot snapshot = new DynamicGoals.GameSnapshot(
            24, 1, supply, 10, supply + 10, minerals, false, combat, 1,
            0, true, false, mutas, gas, true, 6, 1, false, strength);
        snapshot.hasRobo = robo;
        snapshot.hasSupportBay = bay;
        snapshot.reavers = reavers;
        snapshot.shuttles = shuttles;
        snapshot.observers = observers;
        snapshot.hasTribunal = tribunal;
        snapshot.arbiters = arbiters;
        snapshot.hasArchives = true;

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
    public void richMidGameOpensBay() {
        assertTrue(hasGoal(contribute(state(100, 600, 200, 200, 12, false, false, 0, 0, 0, 0, false, 0)),
            AUnitType.Protoss_Robotics_Support_Bay),
            "100 supply with 600/200 opens the Bay");
    }

    @Test
    public void poorEarlyGameStaysShut() {
        assertFalse(hasGoal(contribute(state(60, 200, 100, 100, 8, false, false, 0, 0, 0, 0, false, 0)),
            AUnitType.Protoss_Robotics_Support_Bay),
            "supply 60 with 200/100 opens nothing");
    }

    @Test
    public void bayTrainsReavers() {
        List<ProductionGoal> goals = contribute(state(100, 600, 200, 200, 12, true, true, 0, 0, 1, 0, false, 0));

        assertTrue(hasGoal(goals, AUnitType.Protoss_Reaver),
            "Bay standing with an eye orders the first Reaver: " + goals);
        assertFalse(hasGoal(goals, AUnitType.Protoss_Shuttle),
            "no Shuttle before the first Reaver stands");
    }

    @Test
    public void reaverCallsShuttle() {
        assertTrue(hasGoal(contribute(state(110, 600, 200, 200, 12, true, true, 1, 0, 1, 0, false, 0)),
            AUnitType.Protoss_Shuttle),
            "a standing Reaver with no Shuttle orders one");
    }

    @Test
    public void blindReaverWaitsForEyes() {
        assertFalse(hasGoal(contribute(state(100, 600, 200, 200, 12, true, true, 0, 0, 0, 3, false, 0)),
            AUnitType.Protoss_Reaver),
            "no Reaver into uncovered mutas");
    }

    @Test
    public void lateBulkOpensTribunal() {
        List<ProductionGoal> goals = contribute(state(190, 600, 400, 400, 20, true, true, 2, 1, 2, 0, false, 0));

        assertTrue(hasGoal(goals, AUnitType.Protoss_Arbiter_Tribunal),
            "190 supply with a line opens the Tribunal: " + goals);
    }

    @Test
    public void tribunalTrainsArbiters() {
        assertTrue(hasGoal(contribute(state(190, 600, 400, 400, 20, true, true, 2, 1, 2, 0, true, 0)),
            AUnitType.Protoss_Arbiter),
            "Tribunal standing with none orders Arbiters");
    }

    @Test
    public void twoArbitersHold() {
        assertFalse(hasGoal(contribute(state(190, 600, 400, 400, 20, true, true, 2, 1, 2, 0, true, 2)),
            AUnitType.Protoss_Arbiter),
            "two standing Arbiters hold the line");
    }
}
