package tests.unit;

import atlantis.debug.tweaker.TunerFitness;
import atlantis.debug.tweaker.TunerObjective;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NEXT #160, step 6: TIMES_HIT x GAME_minutes, lower wins, with the
 * win gate that keeps a fast loss from outranking a hard win.
 */
@Tag("infra")
public class TunerFitnessTest {

    @Test
    public void formulaIsHitsTimesMinutes() {
        assertEquals(14 * (22.0 / 60.0), TunerFitness.fitness(14, 22), 0.0001);
        assertEquals(0.0, TunerFitness.fitness(0, 30), 0.0001, "untouched winner is perfect");
    }

    @Test
    public void winAlwaysBeatsLoss() {
        TunerObjective.Row bloodyWin = new TunerObjective.Row("a", true, 100, 600);
        TunerObjective.Row cleanLoss = new TunerObjective.Row("b", false, 1, 10);

        assertTrue(TunerFitness.compare(bloodyWin, cleanLoss) < 0,
            "a 100-hit win still beats a 1-hit loss");
        assertTrue(TunerFitness.compare(cleanLoss, bloodyWin) > 0);
    }

    @Test
    public void lowerFitnessWinsWithinOutcome() {
        TunerObjective.Row quick = new TunerObjective.Row("a", true, 10, 30);
        TunerObjective.Row bloody = new TunerObjective.Row("b", true, 10, 60);

        assertTrue(TunerFitness.compare(quick, bloody) < 0, "same hits, shorter wins");
    }

    @Test
    public void unknownReadsWorst() {
        TunerObjective.Row blind = new TunerObjective.Row("a", true, Integer.MAX_VALUE, 30);
        TunerObjective.Row seen = new TunerObjective.Row("b", true, 500, 30);

        assertTrue(TunerFitness.compare(seen, blind) < 0, "instrumented always beats blind");
        assertEquals(Double.MAX_VALUE, TunerFitness.fitness(Integer.MAX_VALUE, 30), 0.0);
        assertEquals(Double.MAX_VALUE, TunerFitness.fitness(10, Integer.MAX_VALUE), 0.0);
    }

    @Test
    public void tiesBreakOnName() {
        TunerObjective.Row a = new TunerObjective.Row("a", true, 10, 30);
        TunerObjective.Row b = new TunerObjective.Row("b", true, 10, 30);

        assertTrue(TunerFitness.compare(a, b) < 0, "deterministic across runs");
        assertEquals(0, TunerFitness.compare(a, new TunerObjective.Row("a", true, 10, 30)));
    }
}
