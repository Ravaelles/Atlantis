package atlantis.debug.tweaker;

/**
 * Real StarCraft under Wine plus ChaosLauncher: the only tuning engine
 * today. Per-game env files carry PARAM_TWEAKER=1 and the proposed
 * vector; the bot randomizes only when a key is absent, and the fixed
 * __tweaker.csv copy per game makes the played values readable.
 */
public final class WineEngine extends TuningEngine {

    @Override
    public String name() {
        return "wine";
    }

    @Override
    public String batchScript() {
        return "scripts/run-wine-batch.sh";
    }

    @Override
    public String defaultEnv() {
        return "bwapi-data/AI/ENV-WINE-FAST";
    }

    @Override
    public int gameTimeoutSeconds() {
        return 750;
    }

    @Override
    public boolean isLive() {
        return true;
    }

    @Override
    public boolean supportsTuning() {
        return true;
    }
}
