package atlantis.debug.tweaker;

/**
 * The in-repo behavior sim (tests.unit.sim): milliseconds per duel,
 * fully deterministic, no game process at all. The natural home for
 * gradient steps - propose, play dozens of vectors, keep the winners -
 * before Wine spends minutes confirming the survivors.
 *
 * <p>Not wired yet: the sim scenarios live in test scope while the loop
 * lives in production tooling, and dragging one across that boundary
 * needs its own slice (a sim batch runner with the same manifest
 * contract). Until then the tune loop refuses {@code sim} with a
 * pointer instead of pretending.
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
        return false;
    }
}
