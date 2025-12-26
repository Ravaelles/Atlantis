package atlantis.combat.advance.focus_choke;

import atlantis.combat.missions.attack.focus.ProtossShouldIgnoreUseOfMiddleMapChokePoint;
import atlantis.game.A;
import atlantis.map.choke.AChoke;
import atlantis.map.path.PathToEnemyBase;
import atlantis.units.select.Select;
import atlantis.util.log.ErrorLog;

import java.util.ArrayList;

public class MiddleFocusChoke {
    public static AChoke get() {
        if (ProtossShouldIgnoreUseOfMiddleMapChokePoint.ignore()) {
            return null;
        }

        // =========================================================

        ArrayList<AChoke> chokes = PathToEnemyBase.chokesLeadingToEnemyBase();

        int index = Math.min(2, chokes.size() - 2);

        if (index <= chokes.size() - 1 && index >= 2) {
            return chokes.get(index);
        }

        if (!A.isUms() && chokes.size() > 0 && Select.ourBuildings().atLeast(1)) {
            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("No middle choke found! Chokes on path: "
                + chokes.size() + "/" + index);
        }

        return null;


//
//        return null;
    }
}
