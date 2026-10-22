package atlantis.production;

import atlantis.Atlantis;
import atlantis.architecture.Commander;
import atlantis.config.env.Env;
import atlantis.debug.profiler.RealTime;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.orders.production.queue.PriorityWaiters;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
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

        if (A.minerals() < 600) return false;

        // Saving is not hoarding: an unfinished HIGH+ waiter (expansion,
        // tech) holds earmarked minerals - ending here cuts every Nexus-2
        // warp, whose reserve legitimately sits at 400-600 (game 175329).
        // With nothing waiting, 600+ unspent is still the broken hoard.
        if (PriorityWaiters.mineralsAbove(ProductionOrderPriority.STANDARD) > 0) return false;

        // The deadlock this catches is supply-blocked hoarding (bank high,
        // nothing trainable). Bank high with free supply is saving or
        // friction, not deadlock - ending there killed healthy V2 buildups
        // mid-game (measured 2026-10-19: exit at 37/49 with 604 banked).
        if (A.supplyFree() > 4) return false;

        // A provider on its way means the hoard may be waiting for supply,
        // not deadlocked - ending there killed a healthy V2 buildup at
        // 40/41 with 604 banked while a Pylon warped (hp 451, game
        // 20261017_143626). Deadlock is hoard + blocked supply + nothing
        // coming.
        if (supplyProvidersComing() > 0) return false;

        return true;
    }

    private static int supplyProvidersComing() {
        return comingOf(AUnitType.Protoss_Pylon)
            + comingOf(AUnitType.Terran_Supply_Depot)
            + comingOf(AUnitType.Zerg_Overlord);
    }

    private static int comingOf(AUnitType provider) {
        return Math.max(0, Count.ofTypeWithUnfinished(provider) - Count.ofType(provider));
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
