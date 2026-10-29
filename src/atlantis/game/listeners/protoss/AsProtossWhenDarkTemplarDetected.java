package atlantis.game.listeners.protoss;

import atlantis.game.A;
import atlantis.production.constructions.cancelling.CancelNotStarted;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;
import atlantis.util.log.ErrorLog;

public class AsProtossWhenDarkTemplarDetected {
    public static void update(AUnit dt) {
        if (!Have.observer() && !Have.cannon() && !A.hasMinerals(600)) {
            ErrorLog.printMaxOncePerMinute("@@@@@@@@@@ DT detected...");
            if (CancelNotStarted.cancel(AUnitType.Protoss_Nexus, "Cancel due to DT!")) {
                ErrorLog.printMaxOncePerMinute("@@@@@@@@@@ DT detected - cancel nexus");
            }

            // Single policy (NEXT #228): the Forge order is the V2
            // detection goal's job (addDetectionGoals) - the legacy queue
            // write died with the drain, so it only phantom-planned.
        }
    }
}
