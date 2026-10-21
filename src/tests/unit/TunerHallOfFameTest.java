package tests.unit;

import atlantis.debug.tweaker.TunerHallOfFame;
import atlantis.debug.tweaker.TunerObjective.Row;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NEXT #160 step 2: the hall of fame keeps the top-N vectors and
 * mutates around champions inside bounds - exploitation with a
 * shrinking radius, not uniform random in fixed bounds.
 */
@Tag("combat")
public class TunerHallOfFameTest {

    private static Map<String, Double> vector(double kite, double pinch) {
        LinkedHashMap<String, Double> vector = new LinkedHashMap<>();
        vector.put("KiteStepTiles", kite);
        vector.put("DragoonPinchHp", pinch);
        return vector;
    }

    @Test
    public void keepsOnlyTopNByObjective() {
        TunerHallOfFame hall = new TunerHallOfFame(2);

        assertTrue(hall.offer(new Row("g1", true, 19, 90), vector(2.0, 80.0)));
        assertTrue(hall.offer(new Row("g2", true, 6, 60), vector(2.5, 70.0)));
        assertFalse(hall.offer(new Row("g3", false, 0, 20), vector(1.5, 60.0)),
            "a loss must not evict a win from a full hall");

        assertEquals(2, hall.entries().size());
        assertEquals("g2", hall.champion().get().row().game());
    }

    @Test
    public void emptyHallHasNoChampion() {
        assertFalse(new TunerHallOfFame(3).champion().isPresent());
    }

    @Test
    public void mutationStaysInsideRadiusAndBounds() {
        TunerHallOfFame hall = new TunerHallOfFame(3);
        hall.offer(new Row("g1", true, 6, 60), vector(2.0, 80.0));

        Map<String, double[]> bounds = new HashMap<>();
        bounds.put("KiteStepTiles", new double[] { 1.5, 3.0 });
        bounds.put("DragoonPinchHp", new double[] { 60.0, 100.0 });

        Map<String, Double> mutated = TunerHallOfFame.mutateAround(
            hall.champion().get(), bounds, 0.1, new Random(7));

        double kite = mutated.get("KiteStepTiles");
        double pinch = mutated.get("DragoonPinchHp");
        assertTrue(kite >= 1.5 && kite <= 3.0, "clamped into bounds, was " + kite);
        assertTrue(pinch >= 60.0 && pinch <= 100.0, "clamped into bounds, was " + pinch);
        assertTrue(Math.abs(kite - 2.0) <= 0.1 * 1.5 + 1e-9, "inside the radius, was " + kite);
        assertTrue(Math.abs(pinch - 80.0) <= 0.1 * 40.0 + 1e-9, "inside the radius, was " + pinch);
    }

    @Test
    public void zeroRadiusReturnsTheChampionUntouched() {
        TunerHallOfFame hall = new TunerHallOfFame(3);
        hall.offer(new Row("g1", true, 6, 60), vector(2.0, 80.0));

        Map<String, Double> mutated = TunerHallOfFame.mutateAround(
            hall.champion().get(), new HashMap<String, double[]>(), 0.0, new Random(1));

        assertEquals(2.0, mutated.get("KiteStepTiles"));
        assertEquals(80.0, mutated.get("DragoonPinchHp"));
    }

    @Test
    public void frozenParamsRideAtChampionValuesWhileRestExplore() {
        TunerHallOfFame hall = new TunerHallOfFame(3);
        hall.offer(new Row("g1", true, 6, 60), vector(2.0, 80.0));

        Map<String, double[]> bounds = new HashMap<>();
        bounds.put("KiteStepTiles", new double[] { 1.5, 3.0 });
        bounds.put("DragoonPinchHp", new double[] { 60.0, 100.0 });

        java.util.Set<String> frozen = new java.util.HashSet<>(Arrays.asList("KiteStepTiles"));
        Map<String, Double> mutated = TunerHallOfFame.mutateAroundUnfrozen(
            hall.champion().get(), bounds, frozen, 0.5, new Random(7));

        assertEquals(2.0, mutated.get("KiteStepTiles"), "frozen params never move");
        double pinch = mutated.get("DragoonPinchHp");
        assertTrue(pinch >= 60.0 && pinch <= 100.0, "unfrozen params explore inside bounds");
    }

    @Test
    public void fileRoundTripKeepsRankingAndSkipsGarbage() {
        TunerHallOfFame hall = new TunerHallOfFame(5);
        hall.offer(new Row("g1", true, 19, 90), vector(2.0, 80.0));
        hall.offer(new Row("g2", true, 6, 60), vector(2.5, 70.0));

        String saved = hall.save();

        TunerHallOfFame reloaded = new TunerHallOfFame(5);
        reloaded.load("garbage line\n" + saved + "g9,X,1,2|K=1.0\ntoo,short\n");

        assertEquals(2, reloaded.entries().size(), "garbage lines are skipped, never ranked");
        assertEquals("g2", reloaded.champion().get().row().game());
        assertEquals(2.5, reloaded.champion().get().vector().get("KiteStepTiles"));
    }
}
