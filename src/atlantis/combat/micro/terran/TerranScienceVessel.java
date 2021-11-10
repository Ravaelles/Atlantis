
package atlantis.combat.micro.terran;

import atlantis.combat.micro.avoid.AAvoidUnits;
import atlantis.combat.micro.generic.MobileDetector;
import atlantis.combat.squad.Squad;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.UnitActions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import bwapi.TechType;

import java.util.HashMap;

public class TerranScienceVessel extends MobileDetector {

    protected static AUnitType type = AUnitType.Terran_Science_Vessel;

    // =========================================================

    public static boolean update(AUnit scienceVessel) {
        return MobileDetector.update(scienceVessel);
    }

}
