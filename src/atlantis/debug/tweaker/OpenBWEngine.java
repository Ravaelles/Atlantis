package atlantis.debug.tweaker;

/**
 * Headless OpenBW: fast and unattended, but not tuning-capable yet.
 * Two gaps, both on the runner side, both documented where they bite:
 * the client is killed on the 30 s wall cap before OnGameEnd fires,
 * so no tweaker row is ever written, and the duel map does not tick
 * there at all. The day those land, this descriptor already tells the
 * loop which batch script and env to use.
 */
public final class OpenBWEngine extends TuningEngine {

    @Override
    public String name() {
        return "openbw";
    }

    @Override
    public String batchScript() {
        return "scripts/run-openbw-ums.sh";
    }

    @Override
    public String defaultEnv() {
        return "bwapi-data/AI/ENV-OPENBW-FAST";
    }

    @Override
    public int gameTimeoutSeconds() {
        return 30;
    }

    @Override
    public boolean isLive() {
        return true;
    }

    @Override
    public boolean supportsTuning() {
        return false;
    }
}
