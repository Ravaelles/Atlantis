package tests.unit;

import atlantis.debug.tweaker.TunerObjective.Row;
import atlantis.debug.tweaker.TunerPromotion;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NEXT #160 step 3: promotion needs K-of-N replicate wins, because
 * one duel is spawn luck (measured 97 vs 163 first-blood frames on
 * identical code).
 */
@Tag("combat")
public class TunerPromotionTest {

    @Test
    public void twoOfThreeWinsPromotes() {
        assertTrue(TunerPromotion.isPromoted(Arrays.asList(
            new Row("g1", true, 10, 30),
            new Row("g2", false, 40, 60),
            new Row("g3", true, 12, 35)), 2, 3));
    }

    @Test
    public void oneOfThreeWinsDoesNotPromote() {
        assertFalse(TunerPromotion.isPromoted(Arrays.asList(
            new Row("g1", true, 10, 30),
            new Row("g2", false, 40, 60),
            new Row("g3", false, 45, 70)), 2, 3));
    }

    @Test
    public void shortRecordNeverPromotes() {
        assertFalse(TunerPromotion.isPromoted(Collections.singletonList(
            new Row("g1", true, 10, 30)), 1, 3),
            "one lucky game is spawn luck until replicated");
    }

    @Test
    public void winCounterIgnoresNulls() {
        assertEquals(2, TunerPromotion.wins(Arrays.asList(
            new Row("g1", true, 10, 30), null, new Row("g3", true, 12, 35))));
    }
}
