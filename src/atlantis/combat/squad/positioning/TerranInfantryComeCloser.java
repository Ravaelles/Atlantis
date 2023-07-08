package atlantis.combat.squad.positioning;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.architecture.Manager;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class TerranInfantryComeCloser extends Manager {

    public TerranInfantryComeCloser(AUnit unit) {
        super(unit);
    }

    public Manager handleTooFarFromMedic() {
        if (!unit.isTerranInfantry()) {
            return null;
        }
        if (Count.medics() == 0) {
            return null;
        }
        if (unit.enemiesNear().ranged().isEmpty()) {
            return null;
        }
        if (unit.friendsNear().groundUnits().inRadius(1.5, unit).atLeast(7)) {
            return null;
        }

        // =========================================================

        AUnit nearestMedic = Select.ourOfType(AUnitType.Terran_Medic).nearestTo();
        if (nearestMedic.distToMoreThan(unit, 2.3)) {
            unit.setTooltipTactical("LoveMedics");
            return usedManager(this);
        }

        return null;
    }
}
