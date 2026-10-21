package atlantis.debug.tweaker;

import java.util.Map;

/**
 * The 1v1 duel scenario (NEXT #160, step 6): dragoon against zealot on
 * the duel map, fitness TIMES_HIT x GAME_length_IN_MINUTES, lower wins.
 * Fewer hits taken AND a shorter game - kiting that drags forever to
 * save one hit scores worse than a decisive trade, and a bloody fast
 * win scores worse than a clean one. The trade-off is the point.
 *
 * <p>Optimizes every registered param (nothing frozen): the duel
 * exercises kiting, pinch, margin and stand cadences together, so
 * freezing a family would hide exactly the interactions worth finding.
 * Three replicates per candidate (duel noise is spawn luck, measured
 * 97 vs 163 first-blood frames on identical code).
 */
public final class DuelHitsMinutesScenario extends TuningScenario {

    @Override
    public String name() {
        return "duel-hits-x-minutes";
    }

    @Override
    public String mapFile() {
        return "1Drag_v_1Zeal.scm";
    }

    @Override
    public int replicates() {
        return 3;
    }

    @Override
    public double fitness(TunerObjective.Row row) {
        return TunerFitness.fitness(row);
    }

    @Override
    public Map<String, double[]> paramBounds() {
        return ParamTweakerFactory.bounds();
    }
}
