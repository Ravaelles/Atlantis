package atlantis.production;

import atlantis.Atlantis;
import atlantis.architecture.Commander;
import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.util.AConsole;
import atlantis.util.log.ErrorLog;

/**
 * Ends the game once our supply reaches a configured level - the debug-run
 * horizon, not a hard limit (owner 2026-10-19: we test until one or two
 * defects show, and they already show at supply 25-30; longer runs stay
 * available by raising the number, even to 100, when a hunt needs them).
 * A short game means a small log (roughly 3x less to read) and fast
 * iteration; the shell asserts nothing extra, so an early exit cannot
 * hide a failure - it only stops spending time after the defects are in.
 *
 * <p>Off when {@code FORCE_END_AT_SUPPLY} is 0 or absent. The Wine runner
 * defaults it to 50; OpenBW tiers leave it off (their gates own the end).</p>
 */
public class ForceExitAtSupply extends Commander {

    @Override
    public boolean applies() {
        int horizon = Env.forceEndAtSupply();
        if (horizon <= 0) return false;

        // Once a second is plenty for a threshold crossed once, and it keeps
        // the check off the hot path (this runs every frame otherwise).
        if (A.now() % 30 != 0) return false;

        return A.supplyUsed() >= horizon;
    }

    @Override
    protected boolean handle() {
        AConsole.errPrintln("####################################################");
        AConsole.println("### ForceExitAtSupply: supply " + A.supplyUsed() + " reached #####");
        AConsole.errPrintln("####################################################");

        AGame.sendMessage("ForceExitAtSupply");
        ErrorLog.printErrorOnce(
            "Supply horizon reached at " + AGame.timeSeconds() + " in-game seconds: "
                + A.supplyUsed() + "/" + A.supplyTotal());

        Atlantis.getInstance().onEnd(true);

        return false;
    }
}
