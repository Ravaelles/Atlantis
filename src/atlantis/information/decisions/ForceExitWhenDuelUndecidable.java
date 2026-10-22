package atlantis.information.decisions;

import atlantis.Atlantis;
import atlantis.architecture.Commander;
import atlantis.config.env.Env;
import atlantis.debug.profiler.RealTime;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.units.select.Select;
import atlantis.util.AConsole;
import atlantis.util.log.ErrorLog;

/**
 * Undecidable-duel exit (NEXT.md FIRST TASK, step 3; owner 2026-10-21):
 * some UMS maps end the duel without ending the game. Measured on
 * 1Drag_v_1Zeal (four live runs): the slot-3 zealot is known, listed
 * AND visible from frame 24, hits us (4-16 times in the first half
 * wall-second), our dragoon fires back ~11k shots for zero kills, and
 * at ~frame 336 the map removes the zealot without a death (slot 3
 * empties, a neutral unit appears elsewhere). From then on there is
 * nothing to engage for 19 wall seconds - the game burns the whole
 * 20 s bot cap for a DRAW it already is (measured 25-31 s wall).
 *
 * <p>Fires ONLY when all hold: a local UMS run, no enemy units listed
 * right now, our combat units have taken hits (contact proven, not
 * "enemy not met yet"), we killed nothing (a wiped army with
 * reinforcements on the way is a real fight, never this exit), and
 * the game is past the trigger-setup grace (alliances and removals
 * happen in the first seconds; absent data must never trip an exit -
 * the M&M lesson). A real melee game always lists its enemies while
 * they live, and wiping them scores kills - so this cannot fire there;
 * a cloaked unit with a known owner stays listed and blocks it too.
 *
 * <p>Same bounded exit path as {@link ForceExitLocallyAfterSomeTime}:
 * banner, message, error log, {@code onEnd(false)} - the verdict
 * machinery then prints DRAW (zero losses, hits taken, past 100
 * frames), never FAIL and never a loss.
 */
public class ForceExitWhenDuelUndecidable extends Commander {

    /**
     * Trigger setup and scripted removals run in the first game seconds;
     * contact on 1Drag lands inside the first wall second, so this is
     * pure margin (exit at ~8 s wall instead of the 20 s cap).
     */
    private static final int GRACE_REAL_SECONDS = 8;

    @Override
    public boolean applies() {
        if (!Env.isLocal()) return false;
        if (!AGame.isUms()) return false;
        if (A.now() % 24 != 0) return false;
        if (!Select.enemyUnits().isEmpty()) return false;

        if (Select.ourCombatUnits().totalHits() == 0) return false;
        if (Select.ourCombatUnits().totalKills() > 0) return false;

        return RealTime.gameLengthInRealSeconds() >= GRACE_REAL_SECONDS;
    }

    protected boolean handle() {
        AConsole.errPrintln("####################################################");
        AConsole.errPrintln("####### ForceExitWhenDuelUndecidable ###############");
        AConsole.errPrintln("####################################################");

        AGame.sendMessage("ForceExitWhenDuelUndecidable");
        ErrorLog.printErrorOnce(
            "Undecidable duel: no enemy units listed, but our combat units took "
                + Select.ourCombatUnits().totalHits() + " hits in "
                + RealTime.gameLengthInRealSeconds() + " real seconds - ending early"
        );

        Atlantis.getInstance().onEnd(false);

        return false;
    }
}
