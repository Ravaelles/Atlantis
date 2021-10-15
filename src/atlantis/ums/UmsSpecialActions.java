package atlantis.ums;

import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.units.actions.UnitActions;

public class UmsSpecialActions {

    public static AUnit NEW_NEUTRAL_THAT_WILL_RENEGADE_TO_US = null;

    public static boolean update() {
        if (NEW_NEUTRAL_THAT_WILL_RENEGADE_TO_US != null) {
            AUnit goToRenegade = NEW_NEUTRAL_THAT_WILL_RENEGADE_TO_US;

            System.out.println("Haaa! New companion!");

            for (AUnit unit : Select.our().inRadius(10, goToRenegade).listUnits()) {
                if (unit.distanceTo(goToRenegade) > 1.5) {
                    unit.move(goToRenegade, UnitActions.MOVE, "Friendly Renegade!");
                } else {
                    NEW_NEUTRAL_THAT_WILL_RENEGADE_TO_US = null;
                }
            }
            return true;
        }

        return false;
    }

}
