package atlantis.config;

public class AtlantisConfig {

    /**
     * Disabling makes game so fast, you actually be like "Daaaaamn!".
     * Unfortunately it means nothing gets rendered, so game appears to be frozen.
     */
    public static boolean DISABLE_GUI = false;

    //    public static boolean USE_CODE_PROFILER = true;

    /**
     * Per-commander frame costs, accumulated all game and printed once at
     * the end (GameSummary). Always on (owner 2026-11-09): two
     * currentTimeMillis reads per top-level commander per frame cost
     * microseconds against millisecond frames, and every game becomes
     * its own profile - the crash-by-timeout hunt needs attribution
     * from the game that died, not from a guess.
     */
    public static boolean USE_CODE_PROFILER = true;
}
