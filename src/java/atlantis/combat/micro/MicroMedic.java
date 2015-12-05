package atlantis.combat.micro;

import atlantis.wrappers.SelectUnits;
import jnibwapi.Unit;

public class MicroMedic {

    public static boolean update(Unit medic) {

        // Define nearest wounded infantry unit
        Unit nearestWoundedInfantry = SelectUnits.ourCombatUnits().infantry().wounded().nearestTo(medic);

        // If there's wounded unit, heal it.
        if (nearestWoundedInfantry != null) {
            medic.rightClick(nearestWoundedInfantry, false);
            return true;
        } // If no wounded unit, get close to random infantry
        else {
            Unit nearestInfantry = SelectUnits.ourCombatUnits().infantry().exclude(medic).nearestTo(medic);
            if (nearestInfantry != null && nearestInfantry.distanceTo(medic) > 1.5) {
                medic.rightClick(nearestInfantry, false);
            }
            return false;
        }
    }

}
