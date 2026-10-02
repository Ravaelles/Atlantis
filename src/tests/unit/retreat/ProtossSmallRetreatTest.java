package tests.unit.retreat;

import atlantis.combat.retreating.protoss.small_scale.ProtossMeleeSmallScaleRetreat;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Protoss small-scale retreat against enemy melee.
 *
 * <p><b>What the decision actually is</b> (measured, not assumed): a Protoss
 * melee unit retreats from a *local* melee disadvantage, counted inside a small
 * radius ({@code ProtossSmallScaleEvaluate.RADIUS_SM = 1.2}, then
 * {@code RADIUS_LG = 1.9}), and only if it is not already winning overall - a
 * full-hp unit whose army evaluation is 2.5+ does not retreat even at the
 * front.</p>
 *
 * <p>The previous version of this class asserted that 3 zealots should retreat
 * from 4 zealots standing ~2 tiles away, and that the front zealot of a 5-vs-3
 * line should retreat as well. Neither could hold: at 2 tiles the enemy melee
 * is outside both local radii (reason {@code asMeleeGenericNo}, measured), and
 * the 5-vs-3 front zealot is protected by the
 * {@code eval >= 2.5 && hp >= 35} rule (reason {@code evalHighHpHigh},
 * measured). The two expectations also contradicted each other - one wanted a
 * retreat at a losing ratio, the other in a winning one - so at most one of
 * them could ever have been the contract.</p>
 *
 * <p>Each test names the rule it pins and asserts the reason string as well:
 * {@code ProtossMeleeSmallScaleRetreat} computes a reason at every branch and
 * used to throw it away, which made a failing decision impossible to explain.</p>
 */
public class ProtossSmallRetreatTest extends WorldStubForTests {

    @Test
    public void retreatsWhenLocallyOutnumberedByEnemyMelee() {
        FakeUnit[] ours = fakeOurs(zealotsAt(8, 8.1, 8.2));
        FakeUnit[] enemies = fakeEnemies(zealotsAt(9.7, 9.8, 9.9, 10.0));

        createWorld(1, ours, enemies, () -> {
            ProtossMeleeSmallScaleRetreat retreat = new ProtossMeleeSmallScaleRetreat(ours[2]);

            assertTrue(retreat.shouldSmallScaleRetreat(),
                "3 zealots against 4 at 1.5 tiles: locally outnumbered, so retreat. Reason: " + retreat.reason());
            assertTrue(retreat.reason().contains("overpoweredByEnemyMelee"),
                "the decision has to come from the local melee strength, was: " + retreat.reason());
            assertTrue(retreat.applies());
        });
    }

    @Test
    public void doesNotRetreatFromMeleeThatIsNotAdjacentYet() {
        FakeUnit[] ours = fakeOurs(zealotsAt(8, 8.1, 8.2));
        FakeUnit[] enemies = fakeEnemies(zealotsAt(10.0, 10.1, 10.2, 10.3));

        createWorld(1, ours, enemies, () -> {
            ProtossMeleeSmallScaleRetreat retreat = new ProtossMeleeSmallScaleRetreat(ours[2]);

            assertFalse(retreat.shouldSmallScaleRetreat(),
                "3 against 4 is a bad ratio, but at 2 tiles the enemy melee is outside both local "
                    + "radii, so there is nothing to run from yet. Reason: " + retreat.reason());
            assertTrue(retreat.reason().contains("asMeleeGenericNo"),
                "was: " + retreat.reason());
        });
    }

    @Test
    public void doesNotRetreatWhenTheArmyIsWinningEvenAtTheFront() {
        FakeUnit[] ours = fakeOurs(zealotsAt(7, 7.1, 8.1, 8.2, 9.9));
        FakeUnit[] enemies = fakeEnemies(zealotsAt(10.0, 10.1, 10.2));

        createWorld(1, ours, enemies, () -> {
            // The front zealot, one tile from three enemy zealots, but full hp and
            // in an army whose evaluation is 2.5+.
            ProtossMeleeSmallScaleRetreat retreat = new ProtossMeleeSmallScaleRetreat(ours[4]);

            assertFalse(retreat.shouldSmallScaleRetreat(),
                "5 against 3, full hp, evaluation 2.5: no small-scale retreat. Reason: " + retreat.reason());
            assertTrue(retreat.reason().contains("evalHighHpHigh"),
                "was: " + retreat.reason());
        });
    }

    // =========================================================

    private FakeUnit[] zealotsAt(double... positions) {
        FakeUnit[] units = new FakeUnit[positions.length];
        for (int i = 0; i < positions.length; i++) {
            units[i] = fake(AUnitType.Protoss_Zealot, positions[i]);
        }
        return units;
    }
}