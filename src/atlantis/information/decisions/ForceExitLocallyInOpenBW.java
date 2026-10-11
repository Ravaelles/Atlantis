package atlantis.information.decisions;

import atlantis.Atlantis;
import atlantis.architecture.Commander;
import atlantis.config.env.Env;
import atlantis.debug.profiler.RealTime;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.util.AConsole;
import atlantis.util.log.ErrorLog;

public class ForceExitLocallyInOpenBW extends Commander {

    @Override
    public boolean applies() {
        if (!Env.isOpenBW()) return false;

        return A.minerals() >= 700;
    }

    protected boolean handle() {
        AConsole.errPrintln("####################################################");
        AConsole.errPrintln("####################################################");
        AConsole.errPrintln("####################################################");
        AConsole.errPrintln("####################################################");
        AConsole.errPrintln("####### ForceExitLocallyInOpenBW ###################");
        AConsole.errPrintln("####### min:" + A.minerals() + ", supply:"
            + A.supplyUsed() + "/" + A.supplyUsed() + " ######################");
        AConsole.errPrintln("####################################################");
        AConsole.errPrintln("####################################################");
        AConsole.errPrintln("####################################################");
        AConsole.errPrintln("####################################################");

        AGame.sendMessage("ForceExitLocallyAfterRealSeconds");
        ErrorLog.printErrorOnce(
            "Prevented too long game. It took " + RealTime.gameLengthInRealSeconds()
            + " real seconds, " + AGame.timeSeconds() + " in-game seconds,"
            + " supply: " + A.supplyUsed() + "/" + A.supplyTotal()
        );

        Atlantis.getInstance().onEnd(false);
        return false;
    }
}
