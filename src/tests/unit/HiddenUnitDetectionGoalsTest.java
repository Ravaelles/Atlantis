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
    public void lurkersScaleEyesToFour() {
        DynamicGoals.GameSnapshot snapshot = state(true, false);
        snapshot.enemyLurkers = 2;

        assertTrue(hasGoal(contribute(snapshot), AUnitType.Protoss_Observer),
            "2 lurkers want 4 eyes, none standing: " + contribute(snapshot));

        DynamicGoals.GameSnapshot full = state(true, false);
        full.enemyLurkers = 2;
        full.observers = 4;

        assertFalse(hasGoal(contribute(full), AUnitType.Protoss_Observer),
            "4 standing eyes hold the patrol: " + contribute(full));
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

    @Test
    public void detectionTwinNamesEmittedGoals() {
        // Owner #223: the twin is how the next lurker/DT game attributes
        // the chain - goal emission (this twin) vs downstream drops
        // (dispatch twins). Silence without threat, names with it.
        atlantis.util.log.EventLog.clearForTests();
        atlantis.production.demand.V2GoalSourceTwin.clearForTests();

        List<ProductionGoal> goals = contribute(state(true, false));
        atlantis.production.demand.V2GoalSourceTwin.emitDetection(0, goals, true);

        String log = atlantis.util.log.EventLog.toJsonLines();
        assertTrue(log.contains("\"key\":\"V2_DETECTION_GOALS\""),
            "the emitted detection goals are twinned: " + log);
        assertTrue(log.contains("Robotics"),
            "the Robo answer is named: " + log);

        atlantis.util.log.EventLog.clearForTests();
        atlantis.production.demand.V2GoalSourceTwin.emitDetection(300, goals, false);

        assertFalse(atlantis.util.log.EventLog.toJsonLines().contains("V2_DETECTION_GOALS"),
            "no threat, no rows");
    }
}
