package atlantis.game.listeners;

import atlantis.core.world.Worlds;
import atlantis.units.AUnit;
import bwapi.Unit;

public class OnUnitCompleted {
    public static void update(Unit u) {
        if (u == null) {
            System.err.println("onUnitCompleted got null");
            return;
        }

        AUnit unit = Worlds.units().getById(u);
        unit.refreshType();
        if (unit.isOur()) {
            // Count what we BUILT, not what is standing: the end-of-game report needs
            // "3 of 4 Pylons left" and only this event can supply the total. Recording here
            // (rather than scanning the board each frame) also means a unit is counted
            // exactly once - this fires when construction or training finishes.
            atlantis.game.util.CompletedUnitsHistory.record(unit.type());

            OnOurNewUnitCompleted.ourNewUnitCompleted(unit);
        }
    }
}
