package atlantis.information.decisions;

import atlantis.Atlantis;
import atlantis.architecture.Commander;
import atlantis.config.env.Env;
import atlantis.debug.profiler.RealTime;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.util.AConsole;
import atlantis.util.log.ErrorLog;

public class ForceExitWhenInvalidState extends Commander {
    /** Wall-clock limit in real seconds; set from ENV, never widened past the cap. */
    public static int realSecondsLimit = 60 * 10;

    /** In-game limit in game seconds; 0 disables it. 20 game minutes by default. */
    public static int inGameSecondsLimit = 60 * 20;

    @Override
    public boolean applies() {
        if (!Env.isLocal()) return false;

        if (A.supplyUsed() <= 5 && A.minerals() >= 1000) return true;

        return false;
    }

    protected boolean handle() {
        AConsole.errPrintln("####################################################");
        AConsole.errPrintln("####################################################");
        AConsole.errPrintln("####################################################");
        AConsole.errPrintln("####################################################");
        AConsole.errPrintln("####### ForceExitWhenInvalidState ##################");
        AConsole.errPrintln("####################################################");
        AConsole.errPrintln("####################################################");
        AConsole.errPrintln("####################################################");
        AConsole.errPrintln("####################################################");

        AGame.sendMessage("ForceExitWhenInvalidState");

        Atlantis.getInstance().onEnd(false);
        return false;
    }
}
