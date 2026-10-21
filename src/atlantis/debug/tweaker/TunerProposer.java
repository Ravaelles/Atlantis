package atlantis.debug.tweaker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Proposal step of the gradient loop (NEXT #160, step 6): from the hall
 * of fame, through the scenario's fitness, to the next param vector.
 * Wins outrank losses before fitness is even read (a fast loss with few
 * hits must never outrank a hard win); within an outcome the scenario
 * scalar decides. Slopes run over wins when there are at least two,
 * otherwise over everything played - a heuristic optimizer documents
 * its data rule instead of pretending losses carry no information.
 *
 * <p>Pure tooling, no game-loop callers. Deterministic given the same
 * archive and seed.
 */
public final class TunerProposer {

    private TunerProposer() {
    }

    /** Best-first by scenario: wins first, then lower fitness, then game name. */
    public static List<TunerHallOfFame.Entry> rank(TuningScenario scenario, List<TunerHallOfFame.Entry> entries) {
        ArrayList<TunerHallOfFame.Entry> ranked = new ArrayList<>();
        if (entries != null) ranked.addAll(entries);
        Collections.sort(ranked, new Comparator<TunerHallOfFame.Entry>() {
            @Override
            public int compare(TunerHallOfFame.Entry a, TunerHallOfFame.Entry b) {
                if (a.row().win() != b.row().win()) return a.row().win() ? -1 : 1;
                int byFitness = Double.compare(scenario.fitness(a.row()), scenario.fitness(b.row()));
                if (byFitness != 0) return byFitness;
                return a.row().game().compareTo(b.row().game());
            }
        });
        return ranked;
    }

    /**
     * The next vector: champion by scenario rank, descended against the
     * history slopes, jittered for exploration. Params never played are
     * backfilled at bound midpoints, so the fragment is always a complete
     * reproducible vector. Empty archive means an empty proposal - the
     * batch randomizes, exactly today's cold start.
     */
    public static Map<String, Double> propose(
        TuningScenario scenario, TunerHallOfFame hallOfFame,
        double step, double radius, long seed
    ) {
        if (scenario == null || hallOfFame == null || hallOfFame.entries().isEmpty()) {
            return new LinkedHashMap<String, Double>();
        }
        List<TunerHallOfFame.Entry> ranked = rank(scenario, hallOfFame.entries());
        TunerHallOfFame.Entry champion = ranked.get(0);

        List<TunerHallOfFame.Entry> informative = new ArrayList<>();
        for (TunerHallOfFame.Entry entry : ranked) {
            if (entry.row().win()) informative.add(entry);
        }
        if (informative.size() < 2) informative = ranked;
        List<TunerGradient.Sample> samples = new ArrayList<>();
        for (TunerHallOfFame.Entry entry : informative) samples.add(toSample(scenario, entry));

        Map<String, double[]> bounds = scenario.paramBounds();
        Map<String, Double> proposal = TunerGradient.propose(
            champion.vector(), samples, bounds, scenario.frozenParams(),
            step >= 0 ? step : scenario.gradientStep(),
            radius >= 0 ? radius : scenario.noiseRadius(),
            new Random(seed));
        if (bounds != null) {
            for (Map.Entry<String, double[]> bound : bounds.entrySet()) {
                if (!proposal.containsKey(bound.getKey())
                    && bound.getValue() != null && bound.getValue().length >= 2) {
                    proposal.put(bound.getKey(),
                        (bound.getValue()[0] + bound.getValue()[1]) / 2);
                }
            }
        }
        return proposal;
    }

    private static TunerGradient.Sample toSample(TuningScenario scenario, TunerHallOfFame.Entry entry) {
        return new TunerGradient.Sample(entry.vector(), scenario.fitness(entry.row()));
    }
}
