package atlantis.production.dynamic.protoss.units;

import atlantis.game.A;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

import static atlantis.production.AbstractDynamicUnits.buildToHave;

public class ProduceShuttles {
    public static boolean shuttles() {
        if (!Have.roboticsFacility()) return false;

        if (A.supplyUsed() <= 100) {
            if (
                Have.notEvenPlanned(AUnitType.Protoss_Robotics_Facility)
                    || Count.ofType(AUnitType.Protoss_Reaver) >= Count.ofType(AUnitType.Protoss_Shuttle)
            ) {
                return false;
            }
        }

        return buildToHave(AUnitType.Protoss_Shuttle, 1);
    }
}
