package atlantis.combat.micro;

import jnibwapi.Unit;
import atlantis.wrappers.SelectUnits;

public class MicroMedic {

	public static boolean update(Unit medic) {

		// Define nearest wounded infantry unit
		Unit nearestWoundedInfantry = SelectUnits.ourCombatUnits().infantry().wounded().nearestTo(medic);

		// If there's wounded unit, heal it.
		if (nearestWoundedInfantry != null) {
			medic.rightClick(nearestWoundedInfantry, false);
			return true;
		}

		// If no wounded unit, act like ordinary infantry
		else {
			return false;
		}
	}

}
