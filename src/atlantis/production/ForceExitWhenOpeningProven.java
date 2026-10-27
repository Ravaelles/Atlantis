package atlantis.production;

import atlantis.Atlantis;
import atlantis.architecture.Commander;
import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.AConsole;
import atlantis.util.log.ErrorLog;

/**
 * Ends the game as soon as the opening is PROVEN: a finished Cybernetics Core
 * and at least one
 * Dragoon.
 *
 * <p>
 * The {@code --short} tier of {@code scripts/run-openbw-e2e.sh}. The question
 * it answers is
 * "does the bot's production reach its first tech unit", and the honest way to
 * ask it is not
 * to play a full 20-minute game, watch the summary, and infer the answer. The
 * bot can see its
 * own buildings, so it stops the run at the moment the answer is known.
 * </p>
 *
 * <p>
 * <b>It cannot turn a failure green.</b> The shell still asserts the same two
 * facts (plus the
 * in-game time) at the end against the {@code GameSummary} line, so a run that
 * exits early
 * without a Core or without a Dragoon fails exactly as it would have failed
 * after 20 minutes.
 * The gate is only about not spending the remaining 19 minutes.
 * </p>
 *
 * <p>
 * <b>Why it lives in {@code atlantis.production} and not next to the other exit
 * governors</b>
 * ({@code atlantis.information.decisions.ForceExitLocallyAfterSomeTime}):
 * those are
 * referenced from {@code atlantis.units.special}, which is core, and
 * {@code ArchitectureBoundaryTest} forbids core from depending on
 * {@code atlantis.information..}.
 * The existing ones are in the frozen baseline - adding a new one there would
 * have grown the
 * store, which CONVENTIONS §5 forbids. This is a production-lifecycle decision
 * anyway, and
 * {@code ProductionCommander} is in the package allowed to make it.
 * </p>
 *
 * <p>
 * Off by default ({@code CORE_AND_DRAGOON_GATE=true} in ENV), so a normal game
 * is unaffected.
 * </p>
 */
public class ForceExitWhenOpeningProven extends Commander {

    /**
     * Completed Dragoons on the board.
     *
     * <p>
     * {@code Count.existing} is the right number here rather than
     * {@code existingOrInProduction}:
     * a Dragoon still warping in is not yet proof that the tech chain works - the
     * next event
     * could be a cancel - whereas a finished one is the fact this gate is named
     * after.
     * </p>
     */
    private static int completedDragoons() {
        return Count.existing(AUnitType.Protoss_Dragoon);
    }

    @Override
    public boolean applies() {
        if (!Env.coreAndDragoonGate()) return false;

        // Once a second is plenty for a condition that only becomes true once, and it
        // keeps
        // the check off the hot path (this runs every frame otherwise).
        if (A.now() % 30 != 0) return false;

        // Both halves of the documented condition: a finished Core AND Dragoons.
        // Dragoons alone used to end the run, which is exactly how a phantom
        // Dragoon count (no gas, no refinery, no Core) proved an opening that
        // never happened.
        return Count.existing(AUnitType.Protoss_Cybernetics_Core) >= 1
            && Count.dragoons() >= 2;
    }

    @Override
    protected boolean handle() {
        AConsole.errPrintln("####################################################");
        AConsole.println("### ForceExitWhenOpeningProven: Core + Dragoon #####");
        AConsole.errPrintln("####################################################");

        AGame.sendMessage("ForceExitWhenOpeningProven");
        ErrorLog.printErrorOnce(
            "Opening proven at " + AGame.timeSeconds() + " in-game seconds: CyberneticsCore="
                + Count.existingOrInProduction(AUnitType.Protoss_Cybernetics_Core)
                + " Dragoons=" + completedDragoons());

        Atlantis.getInstance().onEnd(true);

        return false;
    }
}
