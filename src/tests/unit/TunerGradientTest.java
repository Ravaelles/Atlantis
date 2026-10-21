package tests.unit;

import atlantis.debug.tweaker.TunerGradient;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NEXT #160, step 6: slopes vote on the sign, the schedule owns the
 * size; frozen params never move; everything clamps into bounds.
 */
@Tag("infra")
public class TunerGradientTest {

    private static Map<String, double[]> bounds() {
        Map<String, double[]> bounds = new LinkedHashMap<>();
        bounds.put("x", new double[] { 0.0, 10.0 });
        bounds.put("y", new double[] { 0.0, 10.0 });
        return bounds;
    }

    private static TunerGradient.Sample sample(double x, double y, double fitness) {
        Map<String, Double> vector = new LinkedHashMap<>();
        vector.put("x", x);
        vector.put("y", y);
        return new TunerGradient.Sample(vector, fitness);
    }

    @Test
    public void slopePointsUphill() {
        List<TunerGradient.Sample> history = Arrays.asList(
            sample(2.0, 5.0, 10.0),
            sample(4.0, 5.0, 20.0),
            sample(6.0, 5.0, 30.0));

        Map<String, Double> slopes = TunerGradient.slopes(history, new HashSet<>(Arrays.asList("x", "y")));

        assertTrue(slopes.get("x") > 0, "growing x grows fitness: " + slopes.get("x"));
        assertEquals(0.0, slopes.get("y"), 0.0001, "no variance, no signal");
    }

    @Test
    public void tooLittleHistoryMeansNoSignal() {
        assertTrue(TunerGradient.slopes(new ArrayList<TunerGradient.Sample>(),
            new HashSet<>(Arrays.asList("x"))).get("x") == 0.0);
        assertTrue(TunerGradient.slopes(
            Collections.singletonList(sample(1.0, 1.0, 5.0)),
            new HashSet<>(Arrays.asList("x"))).get("x") == 0.0);
    }

    @Test
    public void descendMovesAgainstTheSlope() {
        Map<String, Double> champion = new LinkedHashMap<>();
        champion.put("x", 5.0);
        Map<String, Double> slopes = new HashMap<>();
        slopes.put("x", 2.0);

        Map<String, Double> next = TunerGradient.descend(champion, slopes, bounds(),
            Collections.<String>emptySet(), 0.05);

        assertEquals(5.0 - 0.05 * 10.0, next.get("x"), 0.0001, "half a unit down a span of 10");
    }

    @Test
    public void frozenNeverMovesAndBoundsClamp() {
        Map<String, Double> champion = new LinkedHashMap<>();
        champion.put("x", 9.9);
        Map<String, Double> slopes = new HashMap<>();
        slopes.put("x", -100.0);

        Map<String, Double> frozen = TunerGradient.descend(champion, slopes, bounds(),
            new HashSet<>(Arrays.asList("x")), 0.05);
        assertEquals(9.9, frozen.get("x"), 0.0001, "frozen rides at champion value");

        Map<String, Double> clamped = TunerGradient.descend(champion, slopes, bounds(),
            Collections.<String>emptySet(), 0.05);
        assertEquals(10.0, clamped.get("x"), 0.0001, "a wild slope cannot leave bounds");
    }

    @Test
    public void jitterStaysInBoundsAndIsDeterministic() {
        Map<String, Double> champion = new LinkedHashMap<>();
        champion.put("x", 5.0);

        Map<String, Double> a = TunerGradient.jitter(champion, bounds(),
            Collections.<String>emptySet(), 0.1, new Random(7));
        Map<String, Double> b = TunerGradient.jitter(champion, bounds(),
            Collections.<String>emptySet(), 0.1, new Random(7));

        assertEquals(a.get("x"), b.get("x"), 0.0001, "same seed, same proposal");
        assertTrue(a.get("x") >= 4.0 && a.get("x") <= 6.0, "within one radius: " + a.get("x"));
    }

    @Test
    public void intsRenderIntegral() {
        java.util.Set<String> ints = new HashSet<>(Arrays.asList("DragoonLowHp"));
        assertEquals("49", TunerGradient.formatValue("DragoonLowHp", 49.4, ints));
        assertEquals("50", TunerGradient.formatValue("DragoonLowHp", 49.6, ints));
        assertEquals("60", TunerGradient.formatValue("DragoonLowHp", 99.9, ints, new double[] { 30, 60 }),
            "even a wild value renders inside bounds, never randomizes");
        assertEquals("2.5", TunerGradient.formatValue("KiteStepTiles", 2.5, ints));
    }
}
