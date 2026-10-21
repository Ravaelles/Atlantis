package atlantis.debug.tweaker;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * One tunable scenario: the abstract half of the tuner loop (NEXT #160,
 * step 6). A scenario names the game to play, the rows that count, the
 * scalar that decides, and the param space to search - the batch runner
 * plays it, the gradient proposer searches it. Concrete scenarios fill
 * in one map and one trade-off each; the loop machinery never branches
 * on a scenario name.
 *
 * <p>Pure tooling, no game-loop callers.
 */
public abstract class TuningScenario {

    /** Stable id, also the hall-of-fame namespace when several scenarios share a file. */
    public abstract String name();

    /** Map file the batch plays, exactly as the runner resolves it. */
    public abstract String mapFile();

    /** Replicates per candidate before the record means anything (duels are noisy). */
    public abstract int replicates();

    /** Lower is better. Wins outrank losses before this is even read. */
    public abstract double fitness(TunerObjective.Row row);

    /** Ordered param bounds to search; the batch env renders exactly these names. */
    public abstract Map<String, double[]> paramBounds();

    /** Params held at champion values while the rest explore (empty = optimize all). */
    public Set<String> frozenParams() {
        return Collections.emptySet();
    }

    /** Gradient descent step as a fraction of each param's span. */
    public double gradientStep() {
        return 0.05;
    }

    /** Exploration noise as a fraction of each param's span, applied after the descent step. */
    public double noiseRadius() {
        return 0.10;
    }
}
