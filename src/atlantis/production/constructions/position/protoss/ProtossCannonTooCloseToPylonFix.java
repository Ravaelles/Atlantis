package atlantis.production.constructions.position.protoss;

import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class ProtossCannonTooCloseToPylonFix {
    public static boolean isTooClose(AUnitType building, APosition position) {
        if (!building.isCannon()) return false;

        if (Select.ourBasesWithUnfinished().inRadius(3.4, position).count() >= 1) return true;

        return false;
    }
}
