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
    public static int realSecondsLimit = 60 * 10;

    @Override
    public boolean applies() {
        return Env.isLocal()
            && realSecondsLimit > 0
            && A.now() % 300 == 0
            && RealTime.gameLengthInRealSeconds() >= realSecondsLimit;
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
        ErrorLog.printErrorOnce("Prevent too long game. It ran " + RealTime.gameLengthInRealSeconds() + " real seconds");

        Atlantis.getInstance().onEnd(false);
        return false;
    }
}
