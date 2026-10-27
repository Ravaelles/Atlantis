package atlantis.decisions;

import atlantis.Atlantis;
import atlantis.architecture.Commander;
import atlantis.config.env.Env;
import atlantis.game.AGame;
import atlantis.units.select.Select;
import atlantis.util.AConsole;

/**
 * Local games only: once we have no units left, StarCraft keeps running with
 * nothing to do until the real-seconds limit fires. The owner asked for this
 * explicitly: wait a few real seconds (grace for the engine to finish the
 * defeat sequence and for the last events to arrive), then quit.
 */
public class QuitWhenNoUnitsLeft extends Commander {

    /**
     * Real seconds to wait after the last unit died, before quitting.
     */
    private static final int GRACE_REAL_SECONDS = 3;

    /**
     * Wall-clock time the last unit died, in ms; 0 = not triggered yet.
     */
    private static long lastUnitDiedRealTime = 0;

    public static void clear() {
        lastUnitDiedRealTime = 0;
    }

    private static boolean realSecondsElapsed() {
        long elapsedMs = System.currentTimeMillis() - lastUnitDiedRealTime;

        return elapsedMs >= GRACE_REAL_SECONDS * 1000L;
    }

    @Override
    public boolean applies() {
        if (!Env.isLocal()) return false;

        boolean noUnitsLeft = Select.our().isEmpty();
        if (!noUnitsLeft) {
            lastUnitDiedRealTime = 0;

            return false;
        }

        if (lastUnitDiedRealTime == 0) {
            lastUnitDiedRealTime = System.currentTimeMillis();
            AConsole.errPrintln("QuitWhenNoUnitsLeft: no units left, waiting "
                + GRACE_REAL_SECONDS + " real seconds before quitting");
        }

        return true;
    }

    @Override
    protected boolean handle() {
        if (!realSecondsElapsed()) return false;

        AConsole.errPrintln("### QuitWhenNoUnitsLeft: " + GRACE_REAL_SECONDS
            + " real seconds passed with no units left - quitting");

        AGame.sendMessage("gg");
        Atlantis.getInstance().onEnd(false);

        return true;
    }
}
