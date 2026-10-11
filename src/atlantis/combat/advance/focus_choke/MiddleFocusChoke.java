package atlantis.combat.advance.focus_choke;

import atlantis.combat.missions.attack.focus.ProtossShouldIgnoreUseOfMiddleMapChokePoint;
import atlantis.game.A;
import atlantis.map.choke.AChoke;
import atlantis.map.path.PathToEnemyBase;
import atlantis.units.select.Select;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

import java.util.ArrayList;

public class MiddleFocusChoke {
    public static AChoke get() {
        if (We.terran()) return null;
        if (ProtossShouldIgnoreUseOfMiddleMapChokePoint.ignore()) return null;

        // =========================================================

        ArrayList<AChoke> chokes = PathToEnemyBase.chokesLeadingToEnemyBase();

        // The choke at the MIDDLE of the path to the enemy base, and the old maths for it
        // could not return one for a path shorter than four chokes (measured 2026-10-11,
        // owner's report: `No middle choke found! Chokes on path: 2/0` on an ordinary
        // non-UMS map).
        //
        // It was:
        //     index = Math.min(2, chokes.size() - 2)          // always <= 2
        //     if (index <= chokes.size() - 1 && index >= 2)   // requires index >= 2
        // so the only value that passed BOTH was exactly 2, i.e. a path of four or more
        // chokes. With two chokes the index came out 0, and with three it came out 1: both
        // failed the second half and the method returned null. The two halves of the
        // condition disagreed about what the index was for - one clamped it into range, the
        // other demanded it not be small - and the disagreement is invisible in the log,
        // which reports it as a missing choke rather than as a rejected one.
        //
        // The middle of n chokes is n/2, and any path with at least one choke HAS a middle
        // (a path of one choke's middle is that choke). Guarding on "size >= 4" was never
        // the intent - it is an artefact of the two clamps above.
        if (chokes.isEmpty()) {
            if (!A.isUms() || Select.ourBuildings().atLeast(1)) {
                ErrorLog.printMaxOncePerMinutePlusPrintStackTrace(
                    "No chokes on the path to the enemy base at all"
                );
            }

            return null;
        }

        int index = chokes.size() / 2;

        return chokes.get(index);


//
//        return null;
    }
}
