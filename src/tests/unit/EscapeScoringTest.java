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
}
