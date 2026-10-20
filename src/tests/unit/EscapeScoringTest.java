package tests.unit;

import atlantis.battle.escape.EscapeScoring;
import atlantis.map.position.APosition;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Escape slice 4 (NEXT #176): static obstacles near the destination
 * penalize it, closer ones more.
 */
@Tag("combat")
public class EscapeScoringTest {

    @Test
    public void cleanDestinationScoresZero() {
        double penalty = EscapeScoring.staticPenalty(
            APosition.create(30, 20),
            Arrays.asList(APosition.create(50, 50), APosition.create(10, 10)),
            EscapeScoring.STATIC_RADIUS_TILES);

        assertEquals(0.0, penalty, 0.001);
    }

    @Test
    public void closerBlockerHurtsMore() {
        APosition dest = APosition.create(30, 20);

        double near = EscapeScoring.staticPenalty(
            dest, Collections.singletonList(APosition.create(31, 20)), 3);
        double far = EscapeScoring.staticPenalty(
            dest, Collections.singletonList(APosition.create(32, 20)), 3);

        assertTrue(near > far, "1 tile hurts more than 2: " + near + " vs " + far);
        assertEquals((3 - 1) * EscapeScoring.STATIC_WEIGHT, near, 0.001);
    }

    @Test
    public void nullInputsScoreZero() {
        assertEquals(0.0, EscapeScoring.staticPenalty(null, Collections.emptyList(), 3), 0.001);
        assertEquals(0.0, EscapeScoring.staticPenalty(APosition.create(30, 20), null, 3), 0.001);
    }

    @Test
    public void openFieldHasNoCornerPenalty() {
        double[] open = { 12, 12, 12, 12, 12, 12, 12, 12 };

        assertEquals(0.0, EscapeScoring.cornerPenalty(open), 0.001);
    }

    @Test
    public void pocketScoresWorst() {
        double[] pocket = { 1, 1, 1, 1, 1, 1, 1, 1 };
        double[] corridor = { 12, 1, 12, 1, 12, 1, 12, 1 };

        assertTrue(EscapeScoring.cornerPenalty(pocket) > EscapeScoring.cornerPenalty(corridor),
            "no way out beats a corridor: " + EscapeScoring.cornerPenalty(pocket)
                + " vs " + EscapeScoring.cornerPenalty(corridor));
        assertTrue(EscapeScoring.cornerPenalty(corridor) > 0, "walls on 4 sides still cost");
    }

    @Test
    public void loneFarEnemyIsIgnored() {
        double pressure = EscapeScoring.enemyPressure(
            APosition.create(30, 20),
            Collections.singletonList(APosition.create(60, 60)),
            EscapeScoring.ENEMY_RADIUS_TILES);

        assertEquals(0.0, pressure, 0.001);
    }

    @Test
    public void secondMeleeCounts() {
        APosition dest = APosition.create(30, 20);
        double one = EscapeScoring.enemyPressure(
            dest, Collections.singletonList(APosition.create(35, 20)), 20);
        double two = EscapeScoring.enemyPressure(
            dest, Arrays.asList(APosition.create(35, 20), APosition.create(36, 20)), 20);

        assertTrue(two > one, "the second Zealot matters: " + two + " vs " + one);
    }
}
