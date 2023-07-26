package atlantis.combat.micro.zerg.overlord;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class StayAtHome extends Manager {

    public StayAtHome(AUnit unit) {
        super(unit);
    }

    public Manager handle() {
        AUnit main = Select.main();
        if (main != null && unit.distToMoreThan(main, 8)) {
            unit.move(main, Actions.MOVE_FOCUS, "Home", true);
            return usedManager(this);
        }

        return null;
    }
}

