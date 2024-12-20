package atlantis.production.constructing.position.protoss;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class TooCloseToOtherPylons {
    public static boolean isTooCloseToOtherPylons(AUnit builder, AUnitType building, APosition position) {
        if (!building.isPylon()) return false;

        if (A.supplyUsed() % 3 != 0) return false;

        int pylonsNear;
        if (AGame.supplyUsed() < 25) {
            pylonsNear = Select.ourOfType(AUnitType.Protoss_Pylon).inRadius(8, position).count();
        }
        else if (AGame.supplyUsed() < 35) {
            pylonsNear = Select.ourOfType(AUnitType.Protoss_Pylon).inRadius(6.5, position).count();
        }
        else if (AGame.supplyUsed() < 70) {
            pylonsNear = Select.ourOfType(AUnitType.Protoss_Pylon).inRadius(4.5, position).count();
        }
        else if (AGame.supplyUsed() < 100) {
            pylonsNear = Select.ourOfType(AUnitType.Protoss_Pylon).inRadius(3.2, position).count();
        }
        else if (AGame.supplyUsed() < 140) {
            pylonsNear = Select.ourOfType(AUnitType.Protoss_Pylon).inRadius(2, position).count();
        }
        else {
            pylonsNear = -1;
        }

        return pylonsNear >= (A.supplyTotal() <= 40 ? 1 : 2)
            && failed("Pylon too close to other pylons (" + pylonsNear + ")");
    }

    private static boolean failed(String reason) {
        AbstractPositionFinder._CONDITION_THAT_FAILED = reason;
        return true;
    }
}
