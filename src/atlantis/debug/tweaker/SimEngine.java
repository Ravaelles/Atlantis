package atlantis.debug.tweaker;

/**
 * The in-repo behavior sim (tests.unit.sim): milliseconds per duel,
 * fully deterministic, no game process at all. The batch runner
 * ({@code scripts/run-sim-batch.sh} + test-scope {@code SimTunerBatch})
 * plays scripted duels in-JVM and lands the wine-contract artifacts
 * (tweaker.csv, batch.csv, manifest.txt), so the tune driver consumes
 * them unchanged. Only the duel scenario map tunes; everything else
 * fails loudly at the runner.
 */
public final class SimEngine extends TuningEngine {

    @Override
    public String name() {
        return "sim";
    }

    @Override
    public String batchScript() {
        return "scripts/run-sim-batch.sh";
    }

    @Override
    public String defaultEnv() {
        return "";
    }

    @Override
    public int gameTimeoutSeconds() {
        return 0;
    }

    @Override
    public boolean isLive() {
        return false;
    }

    @Override
    public boolean supportsTuning() {
        return true;
    }
}
