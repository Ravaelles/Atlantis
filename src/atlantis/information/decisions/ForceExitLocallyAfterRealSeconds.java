package atlantis.information.decisions;

import atlantis.Atlantis;
import atlantis.architecture.Commander;
import atlantis.config.env.Env;
import atlantis.debug.profiler.RealTime;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.util.AConsole;
import atlantis.util.log.ErrorLog;

public class ForceExitLocallyAfterRealSeconds extends Commander {
    /** Wall-clock limit in real seconds; set from ENV, never widened past the cap. */
    public static int realSecondsLimit = 60 * 10;

    /** In-game limit in game seconds; 0 disables it. 20 game minutes by default. */
    public static int inGameSecondsLimit = 60 * 20;

    @Override
    public boolean applies() {
        if (!Env.isLocal()) return false;
        if (A.now() % 300 != 0) return false;

        if (A.minerals() >= 1200 && A.supplyUsed() == 4) return true;

        if (realSecondsLimit > 0 && RealTime.gameLengthInRealSeconds() >= realSecondsLimit) return true;

        return inGameSecondsLimit > 0 && AGame.timeSeconds() >= inGameSecondsLimit;
    }

    protected boolean handle() {
        AConsole.errPrintln("####################################################");
        AConsole.errPrintln("####################################################");
        AConsole.errPrintln("####################################################");
        AConsole.errPrintln("####################################################");
        AConsole.errPrintln("### ForceExitLocallyAfterRealSeconds #########");
        AConsole.errPrintln("####################################################");
        AConsole.errPrintln("####################################################");
        AConsole.errPrintln("####################################################");
        AConsole.errPrintln("####################################################");

        AGame.sendMessage("ForceExitLocallyAfterRealSeconds");
        ErrorLog.printErrorOnce("Prevent too long game. It ran " + RealTime.gameLengthInRealSeconds()
            + " real seconds / " + AGame.timeSeconds() + " in-game seconds");

        Atlantis.getInstance().onEnd(false);
        return false;
    }
}
