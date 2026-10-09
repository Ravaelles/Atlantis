package tests.acceptance;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.missions.Mission;
import atlantis.combat.missions.defend.MissionDefend;
import atlantis.combat.missions.defend.protoss.ProtossMissionDefendAllowsToAttack;
import atlantis.combat.squad.Squad;
import atlantis.units.CombatEvalScale;
import atlantis.units.select.Select;
import atlantis.decisions.Decision;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static atlantis.units.AUnitType.Protoss_Gateway;
import static atlantis.units.AUnitType.Protoss_Nexus;
import static atlantis.units.AUnitType.Protoss_Probe;
import static atlantis.units.AUnitType.Protoss_Zealot;
import static atlantis.units.AUnitType.Terran_Marine;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * B-1's audit step 2, first row: "strong" and "no reading" are the same number.
 *
 * <p>{@code AUnit.eval()} answers 9874.0 when the simulation has nothing to simulate (no
 * armed, mobile enemy in reach), and a number that high also means "we are winning every
 * exchange". So every {@code eval() >= x} guard reads "nothing in reach" as "we are
 * strong, proceed" - harmless when the choice is about what is nearby, wrong when it is
 * about a chosen target that may be out of reach.
 * {@link CombatEvalScale#NO_ENEMY_IN_REACH} and {@code AUnit.hasEnemyForEval()} exist so
 * a caller can tell the two apart; this test pins that they say what they claim, in both
 * directions, and that the quiet reading is the constant rather than a coincidence.</p>
 *
 * <p>One world per test method, on purpose: the stub world caches selections per frame,
 * and two worlds in one method share those caches (this file was wrong that way first).</p>
 */
public class EvalHasReadingTest extends WorldStubForTests {

    @Test
    public void quietAroundIsNoReading() {
        FakeUnit zealot = fake(Protoss_Zealot, 10);

        world(2, ourWorld(zealot), fakeEnemies(), () -> {
            assertFalse(zealot.hasEnemyForEval(), "nothing is in reach in this world");

            assertTrue(zealot.eval() > CombatEvalScale.NO_ENEMY_IN_REACH - 1,
                "and eval() stays at the quiet sentinel after its Protoss additive adjustments");
        });
    }

    @Test
    public void anArmedEnemyNearbyIsAReading() {
        FakeUnit zealot = fake(Protoss_Zealot, 10);
        FakeUnit marine = fakeEnemy(Terran_Marine, 12);

        world(2, ourWorld(zealot), fakeEnemies(marine), () -> {
            assertTrue(zealot.hasEnemyForEval(), "a Marine two tiles away is a reading");
            assertTrue(zealot.eval() < CombatEvalScale.NO_ENEMY_IN_REACH,
                "and eval() is then about that fight, not the quiet shortcut: " + zealot.eval());
        });
    }

    @Test
    public void anEnemyWithoutAWeaponIsNotAReading() {
        FakeUnit zealot = fake(Protoss_Zealot, 10);
        // A Gateway has no weapon: it is in reach, and it is not a fight.
        FakeUnit enemyGateway = fakeEnemy(Protoss_Gateway, 12);

        world(2, ourWorld(zealot), fakeEnemies(enemyGateway), () -> {
            assertFalse(zealot.hasEnemyForEval(),
                "the evaluator skips enemies without a weapon, so this is still the quiet reading");
        });
    }

    @Test
    public void defendDoesNotChaseAnUnmeasuredTargetOnTheQuietEvalValue() {
        FakeUnit zealot = fake(Protoss_Zealot, 10, 10);
        FakeUnit probe = fake(Protoss_Probe, 11, 10);
        FakeUnit nexus = fake(Protoss_Nexus, 10, 10);
        FakeUnit distantMarine = fakeEnemy(Terran_Marine, 40, 10);

        world(2, new FakeUnit[]{nexus, probe, zealot}, fakeEnemies(distantMarine), () -> {
            Select.clearCache();
            MissionDefend defend = new MissionDefend();
            defend.setFocusPointManager(new FixedDefendFocusPoint(
                new AFocusPoint(nexus.position(), nexus, "TestDefendFocus")));
            Squad squad = new TestSquad(defend);
            squad.addUnit(zealot);
            zealot.forceSetSquad(squad);

            assertFalse(zealot.hasEnemyForEval(), "the assigned marine is outside the eval reach");
            assertEquals(Decision.INDIFFERENT,
                new ProtossMissionDefendAllowsToAttack(zealot).allowsToAttackEnemyUnit(distantMarine),
                "a target 30 tiles away must not pass only because no enemy is in eval reach");

            squad.markLastUnderAttackNow();
            assertEquals(Decision.TRUE,
                new ProtossMissionDefendAllowsToAttack(zealot).allowsToAttackEnemyUnit(distantMarine),
                "the independent recent-attack exception still permits targets near the defend focus");
        });
    }

    private static final class TestSquad extends Squad {
        private TestSquad(Mission mission) {
            super("Test", mission);
        }

        @Override
        public boolean shouldHaveThisSquad() {
            return true;
        }

        @Override
        public int expectedUnits() {
            return 1;
        }
    }

    private static final class FixedDefendFocusPoint extends atlantis.combat.advance.focus.MissionFocusPoint {
        private final AFocusPoint focus;

        private FixedDefendFocusPoint(AFocusPoint focus) {
            this.focus = focus;
        }

        @Override
        public AFocusPoint focusPoint() {
            return focus;
        }
    }

    private FakeUnit[] ourWorld(FakeUnit... extra) {
        FakeUnit[] base = fakeOurs(
            fake(Protoss_Nexus, 10, 10),
            fake(Protoss_Probe, 11, 10)
        );

        FakeUnit[] all = new FakeUnit[base.length + extra.length];
        System.arraycopy(base, 0, all, 0, base.length);
        System.arraycopy(extra, 0, all, base.length, extra.length);
        return all;
    }
}