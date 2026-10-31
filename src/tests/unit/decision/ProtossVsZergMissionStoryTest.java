package tests.unit.decision;

import atlantis.combat.cluster.ArmyMission;
import atlantis.combat.cluster.FightOrFlight;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * The Protoss-vs-Zerg decision story: hold the base, hold the choke,
 * stage through the middle, kill the enemy base.
 *
 * <p>Read it as the game. Every line is a battlefield event or the
 * standing order it must produce - no distances, no postures, no
 * thresholds; those live in {@link MissionStory}. When a decision
 * breaks, this test is the first tool: run it, read the story it
 * prints, and the wrong turn shows itself. Execution (orders,
 * movement, firing) is never asserted here - only decisions.</p>
 */
@Tag("combat")
public class ProtossVsZergMissionStoryTest {

    @Test
    public void holdBaseThenChokeThenMarchMiddleThenKill() {
        MissionStory story = MissionStory.protossAgainstZerg();

        story.holdsPositionAtMainBase();
        story.theMissionIs(ArmyMission.HOLD_MAIN,
            "while no Zerg is seen the line stays home");

        story.advancesToMainChoke();
        story.theMissionIs(ArmyMission.HOLD_MAIN_CHOKE,
            "at the choke mouth the line holds it, never exits");

        story.isOrderedToAttack();
        story.marchesTowardTheMiddle();
        story.theMissionIs(ArmyMission.STAGE_MIDDLE,
            "destination unknown, so the army stages out, not home");

        story.reachesTheMiddle();
        story.theMissionIs(ArmyMission.HOLD_MIDDLE,
            "at the middle the army forms up and waits for contact");

        story.scoutFindsEnemyBase();
        story.theMissionIs(ArmyMission.ATTACK_ENEMY_BASE,
            "a known enemy base is the destination: march there and fight");
    }

    @Test
    public void contactOnTheMarchMeansAttack() {
        MissionStory story = MissionStory.protossAgainstZerg();

        story.isOrderedToAttack();
        story.marchesTowardTheMiddle();
        story.meetsTheEnemy();
        story.theMissionIs(ArmyMission.ATTACK_ENEMY_BASE,
            "joined on the march: the fight in front is the mission");
    }

    @Test
    public void raidersInTheBaseCancelEverything() {
        MissionStory story = MissionStory.protossAgainstZerg();

        story.isOrderedToAttack();
        story.marchesTowardTheMiddle();
        story.theMissionIs(ArmyMission.STAGE_MIDDLE,
            "staging out while the base is safe");

        story.raidersBreachTheBase();
        story.theMissionIs(ArmyMission.HOLD_MAIN,
            "raiders inside: the army comes home, whatever it was doing");
    }

    @Test
    public void lostFightFleesWinnableFightStays() {
        MissionStory story = MissionStory.protossAgainstZerg();

        story.isOrderedToAttack();
        story.scoutFindsEnemyBase();
        story.theTacticIs(FightOrFlight.Tactic.FLEE, 0.5,
            "a lost evaluation flees instead of feeding");

        story.theTacticIs(FightOrFlight.Tactic.FIGHT, 1.5,
            "a winning evaluation stands and fights");
    }
}
