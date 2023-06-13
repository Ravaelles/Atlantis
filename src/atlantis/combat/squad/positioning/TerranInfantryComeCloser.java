package atlantis.combat.squad.positioning;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class TerranInfantryComeCloser {

    public static boolean isTooFarFromMedic(AUnit unit) {
        if (!unit.isTerranInfantry()) {
            return false;
        }
        if (Count.medics() == 0) {
            return false;
        }
        if (unit.enemiesNear().ranged().isEmpty()) {
            return false;
        }
        if (unit.friendsNear().groundUnits().inRadius(1.5, unit).atLeast(7)) {
            return false;
        }

        // =========================================================

        AUnit nearestMedic = Select.ourOfType(AUnitType.Terran_Medic).nearestTo(unit);
        if (nearestMedic.distToMoreThan(unit, 2.3)) {
            unit.setTooltipTactical("LoveMedics");
            return true;
        }

        return false;
    }
}
