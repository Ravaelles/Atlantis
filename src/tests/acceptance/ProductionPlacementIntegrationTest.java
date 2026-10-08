package tests.acceptance;

import atlantis.production.v2.ProductionGoal;
import atlantis.production.v2.TargetPlacement;
import atlantis.production.v2.goals.DynamicGoals;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Acceptance tests for the join between Production V2 and the placement rewrite
 * (`_AI/redesign/01_PRODUCTION.md` M5 + `03_PLACEMENT.md` S5).
 *
 * <p>
 * The two tracks meet at exactly one point: the fortification policy states
 * "this base wants N cannons near here" as ordinary production goals, and the
 * placement planner resolves that constraint to a choke-aware tile. The tests
 * below pin the policy half of that join - the planner half has its own tests -
 * so
 * a change that starts computing tiles in the policy layer, or that stops
 * emitting
 * the goals at all, fails here.
 * </p>
 *
 * <p>
 * Pure: a hand-built snapshot, no game. The live snapshot assembly
 * ({@code GameStateSnapshot}) is exercised by the e2e smoke tests.
 * </p>
 */
public class ProductionPlacementIntegrationTest {

    /** A snapshot with nothing remarkable except what a test sets. */
    private static DynamicGoals.GameSnapshot snapshot(int minerals, int supplyTotal) {
        return new DynamicGoals.GameSnapshot(
                20, 1, supplyTotal, 4, supplyTotal, minerals, false,
                0, 1, 0, false, false, 0).atBase(30, 30);
    }

    private static List<ProductionGoal> cannonGoals(DynamicGoals.GameSnapshot state) {
        List<ProductionGoal> cannons = new ArrayList<>();
        for (ProductionGoal goal : DynamicGoals.contribute(state)) {
            if (goal.item().id().equals(AUnitType.Protoss_Photon_Cannon.name())) {
                cannons.add(goal);
            }
        }
        return cannons;
    }

    @Test
    public void aBaseShortOfCannonsGetsFortificationGoalsFromTheDynamicLayer() {
        // A poor, quiet game: the baseline is one cannon, and we have none.
        List<ProductionGoal> cannons = cannonGoals(snapshot(200, 20));

        assertFalse(cannons.isEmpty(),
                "a base with no cannons must get a fortification goal from the dynamic layer");
    }

    @Test
    public void aBaseWithEnoughCannonsGetsNone() {
        DynamicGoals.GameSnapshot satisfied = new DynamicGoals.GameSnapshot(
                20, 1, 20, 4, 20, 200, false,
                0, 1, 5, false, false, 0).atBase(30, 30);

        assertTrue(cannonGoals(satisfied).isEmpty(),
                "a base at its expected cannon count must emit nothing");
    }

    @Test
    public void theGoalsCarryANeighbourhoodConstraintNotATile() {
        List<ProductionGoal> cannons = cannonGoals(snapshot(900, 60));

        assertFalse(cannons.isEmpty(), "a rich base wants cannons");

        for (ProductionGoal goal : cannons) {
            assertTrue(goal.placement().mode() == TargetPlacement.Mode.NEIGHBOURHOOD,
                    "the policy says 'near this base'; where the tile is belongs to the planner, "
                            + "not to the goal (03_PLACEMENT.md §5.3) - got " + goal.placement());
        }
    }

    @Test
    public void moreMineralsProduceMoreCannonGoals() {
        int poor = cannonGoals(snapshot(200, 20)).size();
        int rich = cannonGoals(snapshot(900, 60)).size();

        assertTrue(rich >= poor,
                "a richer, larger base wants at least as many cannons: " + poor + " vs " + rich);
    }

    @Test
    public void aSnapshotWithoutABaseEmitsNoFortificationGoals() {
        // The 13-argument snapshot leaves the anchor unknown (-1), which is what a
        // caller that does not know the base position gets; fortification must then
        // stay quiet rather than guess a location.
        DynamicGoals.GameSnapshot noBase = new DynamicGoals.GameSnapshot(
                20, 1, 20, 4, 20, 900, false, 0, 1, 0, false, false, 0);

        assertTrue(cannonGoals(noBase).isEmpty(),
                "no base anchor means no fortification goals - a guessed location is worse than none");
    }
}
