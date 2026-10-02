package atlantis.game.listeners;

import atlantis.core.world.Worlds;
import atlantis.game.A;
import atlantis.units.AUnit;
import bwapi.Unit;

public class OnUnitCreated {
    public static void onUnitCreated(Unit u) {
        if (u == null) {
            System.err.println("onUnitCreate got null");
            return;
        }

        AUnit unit = Worlds.units().createFrom(u);

        // Our unit
        if (unit.isOur() && A.now() >= 2) {
            handleOurUnitCreated(unit);
        }
    }

    private static void handleOurUnitCreated(AUnit unit) {
        OnOurUnitCreated.update(unit);
    }
}
