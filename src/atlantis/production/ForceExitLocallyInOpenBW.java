package atlantis.production;

import atlantis.Atlantis;
import atlantis.architecture.Commander;
import atlantis.config.env.Env;
import atlantis.debug.profiler.RealTime;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.util.AConsole;
import atlantis.util.log.ErrorLog;

/**
 * Ends an OpenBW game the moment we hold >= 700 minerals.
 *
 * <p>
 * The owner's tripwire (2026-10-11): a bot sitting on 700 unspent minerals is
 * not playing
 * StarCraft, it is hoarding - and waiting out the remaining 19 minutes of a
 * broken run teaches
 * nothing. Ending there turns "the run is wrong" into a two-line verdict
 * instead of a timeout.
 * </p>
 *
 * <p>
 * <b>Package: moved here from {@code atlantis.information.decisions}.</b> It
 * was registered in
 * {@code atlantis.units.special.SpecialCommander}, which is core, and
 * {@code ArchitectureBoundaryTest} forbids core from depending on
 * {@code atlantis.information..}.
 * The two exit governors already in that list are in the frozen baseline;
 * adding a third would
 * have GROWN the baseline, which CONVENTIONS §5 forbids. This package may
 * depend on
 * {@code information}, so the class lives where the dependency is legal - the
 * same move made for
 * {@link ForceExitWhenOpeningProven}.
 * </p>
 */
public class ForceExitLocallyInOpenBW extends Commander {

    @Override
    public boolean applies() {
        if (!Env.isOpenBW()) return false;

        if (A.minerals() >= 700) return true;
        if (A.supplyUsed() <= 6 && A.now() >= 30 * 60 * 3) return true;

        return false;
    }

    protected boolean handle() {
        AConsole.errPrintln("####################################################");
        AConsole.errPrintln("####################################################");
        AConsole.errPrintln("####################################################");
        AConsole.errPrintln("####################################################");
        AConsole.errPrintln("####### ForceExitLocallyInOpenBW ###################");
        AConsole.errPrintln("####### min:" + A.minerals() + ", supply:"
                + A.supplyUsed() + "/" + A.supplyTotal() + " ######################");
        AConsole.errPrintln("####### At:" + A.minSec() + ", real " + RealTime.gameLengthInRealSeconds() + "s");
        AConsole.errPrintln("####################################################");
        AConsole.errPrintln("####################################################");
        AConsole.errPrintln("####################################################");
        AConsole.errPrintln("####################################################");

        AGame.sendMessage("ForceExitLocallyInOpenBW");
        ErrorLog.printErrorOnce(
                "Prevented too long game. It took " + RealTime.gameLengthInRealSeconds()
                        + " real seconds, " + AGame.timeSeconds() + " in-game seconds,"
                        + " supply: " + A.supplyUsed() + "/" + A.supplyTotal());

        Atlantis.getInstance().onEnd(false);
        return false;
    }
}
