package atlantis.units.special.ums;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class GoToNeutralNewCompanions extends Manager {
    public static AUnit NEW_NEUTRAL_THAT_WILL_RENEGADE_TO_US = null;

    public GoToNeutralNewCompanions(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return A.isUms();
    }

    protected Manager handle() {
        if (NEW_NEUTRAL_THAT_WILL_RENEGADE_TO_US != null) {
            AUnit goToRenegade = NEW_NEUTRAL_THAT_WILL_RENEGADE_TO_US;

            for (AUnit unit : Select.our().inRadius(10, goToRenegade).list()) {
                if (unit.distTo(goToRenegade) > 0.5) {
                    unit.move(goToRenegade, Actions.SPECIAL, "Friendly Renegade!", true);
                }
                else {
                    NEW_NEUTRAL_THAT_WILL_RENEGADE_TO_US = null;
                }
            }

            return usedManager(this, "Friendly Renegade!");
        }

        return null;
    }
}
