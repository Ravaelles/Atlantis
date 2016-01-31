package atlantis.combat.micro.terran;

import atlantis.wrappers.SelectUnits;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;

public class TerranMedic {

    public static boolean update(Unit medic) {

        // Define nearest wounded infantry unit
        Unit nearestWoundedInfantry = SelectUnits.ourCombatUnits().infantry().wounded()
                .inRadius(11, medic).nearestTo(medic);

        // If there's wounded unit, heal it.
        if (nearestWoundedInfantry != null) {
            medic.rightClick(nearestWoundedInfantry, false);
            return true;
        } // If no wounded unit, get close to random infantry
        else {
            Unit nearestInfantry = SelectUnits.ourCombatUnits().ofType(
                    UnitType.UnitTypes.Terran_Marine,
                    UnitType.UnitTypes.Terran_Firebat,
                    UnitType.UnitTypes.Terran_Ghost
            ).nearestTo(medic);
            if (nearestInfantry != null && nearestInfantry.distanceTo(medic) > 1.8 
                    && !nearestInfantry.equals(medic.getTarget())) {
                medic.rightClick(nearestInfantry, false);
            }
            return false;
        }
    }

}
