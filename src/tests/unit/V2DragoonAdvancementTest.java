package tests.unit;

import atlantis.production.v2.ProductionGoal;
import atlantis.production.v2.ProductionRacePolicy;
import atlantis.production.v2.UnitProducible;
import atlantis.production.v2.goals.DynamicGoals;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the V2 army-advancement arithmetic (NEXT #45 race-army coverage):
 * after the Core the advanced unit replaces the baseline - but only as a
 * floor fill. A standing Zealot army at/above the floor freezes the
 * advancement forever (OpenBW `202456`: Core from 4:35, 7 Zealots, zero
 * Dragoons in 17.6 min - demand-side, not execution).
 *
 * <p>Pure snapshots, no game. Whether the floor should convert an
 * existing army is an owner call - this test documents what the
 * arithmetic does today, it does not bless it.</p>
 */
public class V2DragoonAdvancementTest {

    private static final String DRAGOON = UnitProducible.of(AUnitType.Protoss_Dragoon).id();
    private static final String ZEALOT = UnitProducible.of(AUnitType.Protoss_Zealot).id();

    private static DynamicGoals.GameSnapshot state(int armySize, boolean hasCore) {
        return new DynamicGoals.GameSnapshot(
            25, 1, 30, 10, 40, 500, false, armySize, 1,
            0, false, false, 0,
            200, hasCore, 0, 1, false, 0);
    }

    private static boolean contains(List<ProductionGoal> goals, String id) {
        return goals.stream().anyMatch(goal -> goal.item().id().equals(id));
    }

    @Test
    public void advancementFiresFromEmptyArmyAfterCore() {
        List<ProductionGoal> goals =
            DynamicGoals.contribute(state(0, true), ProductionRacePolicy.forRace("Protoss"));

        assertTrue(contains(goals, DRAGOON), "empty army past the Core wants Dragoons, not Zealots");
        assertFalse(contains(goals, ZEALOT), "the baseline does not double-fill behind the advancement");
        ProductionGoal dragoon = goals.stream()
            .filter(goal -> goal.item().id().equals(DRAGOON))
            .findFirst().orElseThrow(AssertionError::new);
        assertEquals(ProductionGoal.PRIORITY_MAINARMYBASE, dragoon.priority());
        assertEquals(6, dragoon.count(), "floor is workers/4 = 25/4, all of it missing");
    }

    @Test
    public void zealotArmyAtFloorFreezesAdvancement() {
        List<ProductionGoal> goals =
            DynamicGoals.contribute(state(7, true), ProductionRacePolicy.forRace("Protoss"));

        assertFalse(contains(goals, DRAGOON),
            "7 standing Zealots cover the floor of 6 - no Dragoon is ever wanted (202456 freeze)");
    }

    @Test
    public void noCoreMeansZealotBaseline() {
        List<ProductionGoal> goals =
            DynamicGoals.contribute(state(0, false), ProductionRacePolicy.forRace("Protoss"));

        assertTrue(contains(goals, ZEALOT), "pre-Core baseline is Zealots");
        assertFalse(contains(goals, DRAGOON), "no Core, no Dragoons");
    }
}
