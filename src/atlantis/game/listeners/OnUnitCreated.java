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

        // CherryVis first-seen for our own units (Stardust records at creation,
        // not at discovery - BWAPI never "discovers" our own units, so the
        // UNIT_DISCOVERED path misses the whole army). Engine type id, not
        // AUnitType.id() (NEXT #68).
        if (unit.isOur() && atlantis.cherryvis.ACherryVis.isEnabled()) {
            atlantis.cherryvis.simple.ACherryVis_UnitFirstSeen.record(
                A.now(), unit.id(), unit.type().ut().id, unit.x(), unit.y()
            );
        }

        // Our unit
        if (unit.isOur() && A.now() >= 2) {
            handleOurUnitCreated(unit);
        }
    }

    private static void handleOurUnitCreated(AUnit unit) {
        OnOurUnitCreated.update(unit);
    }
}
