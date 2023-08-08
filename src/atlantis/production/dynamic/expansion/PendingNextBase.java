package atlantis.production.dynamic.expansion;

import atlantis.config.AtlantisConfig;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.log.ErrorLog;

public class PendingNextBase {
    public static HasPosition basePositionOrNull() {
        APosition notYetStarted = notStartedBase();

        if (notYetStarted != null) {
            return notYetStarted;
        }

        return unfinishedBase();
    }

    public static APosition notStartedBase() {
        Construction futureBase = ConstructionRequests.getNotStartedOfType(AtlantisConfig.BASE);

        if (futureBase == null) {
//            ErrorLog.printMaxOncePerMinute("ReinforceBaseWithCombatBuildings: futureBase == null");
            return null;
        }

        APosition basePosition = futureBase.buildPosition();

        if (basePosition == null) {
            ErrorLog.printMaxOncePerMinute("ReinforceBaseWithCombatBuildings: futureBase.buildPosition() == null");
            return null;
        }

        return basePosition;
    }

    public static AUnit unfinishedBase() {
        return Select.ourUnfinished().bases().first();
    }
}