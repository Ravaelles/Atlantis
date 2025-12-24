package atlantis.combat.advance.focus_choke;

import atlantis.combat.missions.attack.focus.ProtossShouldIgnoreUseOfMiddleMapChokePoint;
import atlantis.map.choke.AChoke;
import atlantis.map.path.PathToEnemyBase;
import atlantis.util.log.ErrorLog;

import java.util.ArrayList;

public class MiddleFocusChoke {
    public static AChoke get() {
        if (ProtossShouldIgnoreUseOfMiddleMapChokePoint.ignore()) {
            return null;
        }

        // =========================================================

        ArrayList<AChoke> chokes = PathToEnemyBase.chokesLeadingToEnemyBase();

        int index = Math.min(3, chokes.size() / 2 + 1);


        if (index >= 2 && index < chokes.size() - 1) {
            return chokes.get(index);
        }

        ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("No middle choke found! Chokes on path: "
            + chokes.size() + " / " + index);

        return null;
    }
}
