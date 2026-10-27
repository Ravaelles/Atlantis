package tests.unit;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.BattleCommander;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.combat.retreating.FleeReason;
import atlantis.combat.retreating.RetreatFromLostFight;
import atlantis.config.env.Env;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NEXT #195: every flee branch owns a named reason; one method
 * ({@link RetreatFromLostFight#decide}) classifies, retreat() only
 * executes. Unpinned by construction: STAY_BASELESS (needs a UMS
 * baseless world), CORNERED_CUTOFF (needs a wall-sealed surround) and
 * NOWHERE_TO_RUN (needs a null rally) - they ride the existing
 * retreat net (RetreatSplitChainTest + sim) until a scenario names
 * them.
 */
@Tag("combat")
public class FleeReasonTest extends WorldStubForTests {

    @AfterEach
    public void resetTactic() {
        BattleCommander.clearForTests();
        Env.setLocalForTest(false);
    }

    @Test
    public void nonFleeTacticStays() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 30);

        RetreatFromLostFight.Verdict verdict = RetreatFromLostFight.decide(
            dragoon, FightOrFlight.Tactic.FIGHT, null, null);

        assertEquals(FleeReason.STAY_TACTIC, verdict.reason);
        assertNull(verdict.dest, "no destination, no march");
    }

    @Test
    public void meleeGrabStaysToFight() {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 30, 30);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 31, 30);
        zealot.cooldown = 0;

        world(3, fakeOurs(zealot), fakeEnemies(foe), () -> {
            if (atlantis.game.A.now() != 1) return;

            Env.setLocalForTest(true);

            RetreatFromLostFight.Verdict verdict = RetreatFromLostFight.decide(
                zealot, FightOrFlight.Tactic.FLEE, null, null);

            assertEquals(FleeReason.STAY_MELEE_GRAB, verdict.reason);
            assertNull(verdict.dest, "the shot lands now, the march would donate it");
            assertFalse(
                RetreatFromLostFight.retreat(zealot, FightOrFlight.Tactic.FLEE, null, null));
        });
    }

    @Test
    public void cutoffFightsWhenItCanHitBack() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit friendA = fake(AUnitType.Protoss_Dragoon, 50, 20);
        FakeUnit friendB = fake(AUnitType.Protoss_Dragoon, 51, 20);
        FakeUnit chewer = fake(AUnitType.Protoss_Zealot, 21, 20);

        world(3, fakeOurs(dragoon, friendA, friendB), fakeEnemies(chewer), () -> {
            if (atlantis.game.A.now() != 1) return;

            Env.setLocalForTest(true);
            Alpha.gather();

            RetreatFromLostFight.Verdict verdict = RetreatFromLostFight.decide(
                dragoon, FightOrFlight.Tactic.FLEE, null, dragoon);

            assertEquals(FleeReason.FIGHT_CUTOFF, verdict.reason);
            assertNull(verdict.dest, "cut off but armed: fight, never donate the march");
        });
    }

    @Test
    public void cutoffStepsAwayWhenOutranged() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit friendA = fake(AUnitType.Protoss_Dragoon, 50, 20);
        FakeUnit friendB = fake(AUnitType.Protoss_Dragoon, 51, 20);
        FakeUnit presser = fake(AUnitType.Protoss_Zealot, 26, 20);

        world(3, fakeOurs(dragoon, friendA, friendB), fakeEnemies(presser), () -> {
            if (atlantis.game.A.now() != 1) return;

            Env.setLocalForTest(true);
            Alpha.gather();

            RetreatFromLostFight.Verdict verdict = RetreatFromLostFight.decide(
                dragoon, FightOrFlight.Tactic.FLEE, null, dragoon);

            assertEquals(FleeReason.STEP_CUTOFF, verdict.reason);
            assertNotNull(verdict.dest, "step away locally, never march far");
        });
    }

    @Test
    public void lostUnitRalliesHome() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 7, 44);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 60, 30);

        world(3, fakeOurs(nexus, dragoon), new FakeUnit[]{}, () -> {
            if (atlantis.game.A.now() != 1) return;

            Env.setLocalForTest(true);

            RetreatFromLostFight.Verdict verdict = RetreatFromLostFight.decide(
                dragoon, FightOrFlight.Tactic.FLEE, nexus.position(), null);

            assertEquals(FleeReason.RALLY, verdict.reason);
            assertNotNull(verdict.dest, "a lost unit marches home");
            assertTrue(
                RetreatFromLostFight.retreat(
                    dragoon, FightOrFlight.Tactic.FLEE, nexus.position(), null),
                "the verdict executes");
        });
    }
}
