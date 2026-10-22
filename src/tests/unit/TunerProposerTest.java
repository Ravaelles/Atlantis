package tests.unit;

import atlantis.debug.tweaker.DuelHitsMinutesScenario;
import atlantis.debug.tweaker.TunerHallOfFame;
import atlantis.debug.tweaker.TunerObjective;
import atlantis.debug.tweaker.TunerProposer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NEXT #160, step 6: the proposer ranks by scenario (wins first, then
 * fitness), descends the champion against history, and always returns
 * a complete in-bounds vector - or nothing on an empty archive, so the
 * batch falls back to randomizing exactly like today.
 */
@Tag("infra")
public class TunerProposerTest {

    private final DuelHitsMinutesScenario scenario = new DuelHitsMinutesScenario();

    private static TunerHallOfFame hallOfFame() {
        TunerHallOfFame hall = new TunerHallOfFame(50);
        hall.offer(new TunerObjective.Row("game-1", true, 20, 60), vector(2.0));
        hall.offer(new TunerObjective.Row("game-2", true, 10, 60), vector(4.0));
        hall.offer(new TunerObjective.Row("game-3", false, 1, 10), vector(9.0));

        return hall;
    }

    private static Map<String, Double> vector(double kiteStep) {
        Map<String, Double> vector = new LinkedHashMap<>();
        for (Map.Entry<String, double[]> bound : new DuelHitsMinutesScenario().paramBounds().entrySet()) {
            vector.put(bound.getKey(), (bound.getValue()[0] + bound.getValue()[1]) / 2);
        }
        vector.put("KiteStepTiles", kiteStep);

        return vector;
    }

    @Test
    public void rankPutsBestWinFirst() {
        List<TunerHallOfFame.Entry> ranked = TunerProposer.rank(scenario, hallOfFame().entries());

        assertEquals("game-2", ranked.get(0).row().game(), "10 hits beats 20 hits");
        assertEquals("game-3", ranked.get(ranked.size() - 1).row().game(), "the loss sorts last whatever its fitness");
    }

    @Test
    public void proposalIsCompleteAndBounded() {
        Map<String, Double> proposal = TunerProposer.propose(scenario, hallOfFame(), 0.05, 0.10, 7L);

        assertEquals(33, proposal.size(), "full vector, every param gets a value");
        for (Map.Entry<String, double[]> bound : scenario.paramBounds().entrySet()) {
            double value = proposal.get(bound.getKey());
            assertTrue(value >= bound.getValue()[0] && value <= bound.getValue()[1],
                bound.getKey() + " in bounds: " + value);
        }
    }

    @Test
    public void proposalIsDeterministic() {
        Map<String, Double> a = TunerProposer.propose(scenario, hallOfFame(), 0.05, 0.10, 7L);
        Map<String, Double> b = TunerProposer.propose(scenario, hallOfFame(), 0.05, 0.10, 7L);

        assertEquals(a, b, "same archive, same seed, same proposal");
    }

    @Test
    public void emptyArchiveMeansNoProposal() {
        assertTrue(TunerProposer.propose(scenario, new TunerHallOfFame(50), 0.05, 0.10, 7L).isEmpty(),
            "cold start: batch randomizes like today");
    }
}
