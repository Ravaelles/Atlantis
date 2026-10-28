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
 * Hidden-unit crisis response at the V2 goal layer (owner: a seen lurker
 * ordered no Forge, no cannons, no Robo - the legacy queue path fires but
 * V2 LIVE never drains it, and the goal layer has no detection branch at
 * all). Within 30 frames of the sighting a Forge or a Robotics Facility
 * is ordered; for lurkers the Robo into Observatory into Observer chain
 * is preferred, with cannons holding the base while it spins up.
 *
 * <p>Pure snapshot state, no world.</p>
 */
@Tag("production")
public class HiddenUnitDetectionGoalsTest {

    private static final int SIGHTING_FRAME = 0;
    private static final int RESPONSE_DEADLINE_FRAMES = 30;

    private static DynamicGoals.GameSnapshot state(boolean hiddenUnits, boolean hasForge) {
        DynamicGoals.GameSnapshot snapshot = new DynamicGoals.GameSnapshot(
            20, 1, 30, 10, 40, 600, false, 8, 1,
            0, true, false, 0,
            200, true, 4, 1, false, 200);
        snapshot.hasForge = hasForge;
        snapshot.enemyHasHiddenUnits = hiddenUnits;

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
    public void lurkerSightingOrdersForgeOrRoboWithinThirtyFrames() {
        int firstResponseFrame = -1;

        for (int frame = 0; frame <= RESPONSE_DEADLINE_FRAMES; frame++) {
            DynamicGoals.GameSnapshot snapshot = state(frame >= SIGHTING_FRAME, false);
            List<ProductionGoal> goals = contribute(snapshot);

            if (hasGoal(goals, AUnitType.Protoss_Forge)
                || hasGoal(goals, AUnitType.Protoss_Robotics_Facility)) {
                firstResponseFrame = frame;
                break;
            }
        }

        assertTrue(firstResponseFrame >= 0 && firstResponseFrame <= RESPONSE_DEADLINE_FRAMES,
            "a seen lurker orders a Forge or a Robo within 30 frames - first response at "
                + firstResponseFrame);
    }

    @Test
    public void lurkerChainPrefersRoboIntoObservatoryAndObserver() {
        List<ProductionGoal> goals = contribute(state(true, false));

        assertTrue(hasGoal(goals, AUnitType.Protoss_Robotics_Facility),
            "fast Robo first: " + goals);
        assertTrue(hasGoal(goals, AUnitType.Protoss_Observatory),
            "Observatory behind it: " + goals);
        assertTrue(hasGoal(goals, AUnitType.Protoss_Observer),
            "and the Observer itself: " + goals);
    }

    @Test
    public void cannonsHoldWhileTheRoboChainSpinsUp() {
        List<ProductionGoal> goals = contribute(state(true, false));

        assertTrue(hasGoal(goals, AUnitType.Protoss_Photon_Cannon),
            "cannons hold while the Robo chain spins up: " + goals);
    }

    @Test
    public void standingForgeStillWantsTheRoboChain() {
        List<ProductionGoal> goals = contribute(state(true, true));

        assertFalse(hasGoal(goals, AUnitType.Protoss_Forge),
            "a standing Forge is never re-ordered: " + goals);
        assertTrue(hasGoal(goals, AUnitType.Protoss_Robotics_Facility),
            "the Robo chain is still wanted: " + goals);
        assertTrue(hasGoal(goals, AUnitType.Protoss_Photon_Cannon),
            "cannons still hold: " + goals);
    }

    @Test
    public void noSightingMeansNoDetectionGoals() {
        List<ProductionGoal> goals = contribute(state(false, false));

        assertFalse(hasGoal(goals, AUnitType.Protoss_Forge),
            "no lurker, no Forge from detection: " + goals);
        assertFalse(hasGoal(goals, AUnitType.Protoss_Robotics_Facility),
            "no lurker, no Robo from detection: " + goals);
        assertFalse(hasGoal(goals, AUnitType.Protoss_Observatory),
            "no lurker, no Observatory from detection: " + goals);
    }
}
