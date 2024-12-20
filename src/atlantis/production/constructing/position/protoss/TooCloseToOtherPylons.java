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

        int radius;
        if (AGame.supplyUsed() < 25) {
            radius = 6;
        }
        else if (AGame.supplyUsed() < 70) {
            radius = 4;
        }
//        else if (AGame.supplyUsed() < 150) {
//            radius = 4;
//        }
        else {
            radius = 3;
        }

        int pylonsNear = Select.ourOfType(AUnitType.Protoss_Pylon).inRadius(radius, position).count();

        return pylonsNear >= (A.supplyTotal() <= 90 && A.supplyFree() >= 3 ? 1 : 2)
            && failed("Pylon too close to other pylons (" + pylonsNear + ")");
    }

    private static boolean failed(String reason) {
        AbstractPositionFinder._STATUS = reason;
        return true;
    }
}
