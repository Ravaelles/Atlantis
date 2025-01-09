package atlantis.game.listeners;

import atlantis.units.AUnit;
import bwapi.Unit;

public class OnUnitCompleted {
    public static void update(Unit u) {
        if (u == null) {
            System.err.println("onUnitCompleted got null");
            return;
        }

        AUnit unit = AUnit.getById(u);
        unit.refreshType();
        if (unit.isOur()) {
            OnOurNewUnitCompleted.ourNewUnitCompleted(unit);
        }
    }
}
