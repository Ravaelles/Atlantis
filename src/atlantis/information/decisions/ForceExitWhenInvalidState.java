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

        // Absent data is not an empty army: on maps where the engine has
        // not published supply at all (UMS M&M money map: supply 0/0 with
        // a standing 30-unit army and bank 10000), supplyUsed()==0 would
        // read as "nothing to play" and suicide a healthy game at frame 2
        // (measured 2026-10-20). Only a PUBLISHED zero is a stuck game.
        if (A.supplyTotal() <= 0) return false;

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
