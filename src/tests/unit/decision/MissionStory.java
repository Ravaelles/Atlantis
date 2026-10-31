package tests.unit.decision;

import atlantis.combat.cluster.ArmyMission;
import atlantis.combat.cluster.ClusterMission;
import atlantis.combat.cluster.FightOrFlight;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The unified decision-story standard: test WHAT the army decided, never
 * HOW it was executed.
 *
 * <p>A story is a game in words. Each method is one thing happening on
 * the battlefield; {@code theMissionIs} asserts the standing order that
 * must follow. Distances, postures and thresholds live in here, never in
 * the test - a story reads as sentences, and a failure prints the story
 * so far, so the bug is found by reading, not by debugging.</p>
 *
 * <p>Stories call the real deciders ({@link ArmyMission#decide},
 * {@link FightOrFlight#decide}) with the real mission and tactic classes:
 * nothing is mocked, so a passing story proves the production vocabulary
 * answers. Execution (orders, movement, firing) is deliberately absent -
 * that is the next layer's tests.</p>
 */
public final class MissionStory {

    private final ArmyMission.Snapshot in = new ArmyMission.Snapshot();
    private final List<String> soFar = new ArrayList<>();
    private int frame = 0;

    private MissionStory() {
        in.posture = ClusterMission.Posture.DEFEND;
        in.distToMain = 0;
        in.distToMainChoke = far();
        in.distToNaturalChoke = unknown();
        in.distToMiddleChoke = far();
    }

    /** A Protoss army facing Zerg, standing at the main base, nothing seen. */
    public static MissionStory protossAgainstZerg() {
        MissionStory story = new MissionStory();
        story.soFar.add("a Protoss army stands at the main base, no Zerg seen");
        return story;
    }

    /** The line stays at the main base. */
    public void holdsPositionAtMainBase() {
        in.distToMain = 0;
        in.distToMainChoke = far();
        next("the army holds at the main base");
    }

    /** The line walks out to the main choke mouth. */
    public void advancesToMainChoke() {
        in.distToMain = 9;
        in.distToMainChoke = at();
        next("the army advances to the main choke mouth");
    }

    /** The line walks out to the natural choke mouth. */
    public void holdsPositionAtNaturalChoke() {
        in.distToMain = 15;
        in.distToMainChoke = far();
        in.distToNaturalChoke = at();
        next("the army advances to the natural choke mouth");
    }

    /** Command switches the posture to attack. */
    public void isOrderedToAttack() {
        in.posture = ClusterMission.Posture.ATTACK;
        next("command orders the attack");
    }

    /** The army marches out, destination unknown. */
    public void marchesTowardTheMiddle() {
        in.distToMain = 30;
        in.distToMainChoke = far();
        in.distToMiddleChoke = far();
        next("the army marches toward the middle, destination unknown");
    }

    /** The army reaches the middle staging point. */
    public void reachesTheMiddle() {
        in.distToMiddleChoke = at();
        next("the army reaches the middle staging point");
    }

    /** The scout finds the Zerg main base. */
    public void scoutFindsEnemyBase() {
        in.enemyBaseKnown = true;
        in.distToEnemyBase = 40;
        next("the scout finds the Zerg main base");
    }

    /** The army touches the enemy. */
    public void meetsTheEnemy() {
        in.inContact = true;
        next("the army meets the enemy");
    }

    /** Raiders slip into the base: everything else yields. */
    public void raidersBreachTheBase() {
        in.breached = true;
        next("Zerg raiders breach the base");
    }

    /**
     * The standing order must be this mission. The assertion message is
     * the story so far plus the reason in plain words.
     */
    public void theMissionIs(ArmyMission expected, String why) {
        ArmyMission decided = ArmyMission.decide(in);
        assertEquals(expected, decided,
            "frame " + frame + " of the story:\n    "
                + String.join("\n    ", soFar)
                + "\n  expected " + expected + " (" + why + ")"
                + " but the army decided " + decided);
    }

    /** The cluster tactic must be this, for the given evaluation. */
    public void theTacticIs(FightOrFlight.Tactic expected, double eval, String why) {
        FightOrFlight.Tactic decided = FightOrFlight.decide(eval, frame, -1);
        assertEquals(expected, decided,
            "frame " + frame + " of the story:\n    "
                + String.join("\n    ", soFar)
                + "\n  expected tactic " + expected + " (" + why + ")"
                + " but the cluster decided " + decided);
    }

    private void next(String sentence) {
        frame += 24;
        soFar.add(sentence);
    }

    private static double at() {
        return 1.0;
    }

    private static double far() {
        return 30.0;
    }

    private static double unknown() {
        return Double.MAX_VALUE;
    }
}
