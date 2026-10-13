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

        // CherryVis first-seen for our own units, but ONLY when completed
        // (Stardust records at the incomplete->complete transition, not at
        // creation - NEXT #68 follow-up): a unit being trained reports its
        // PRODUCER's position (every late Probe recorded at the Nexus,
        // (288,1456)), so the viewer - nearest same-type within 25px, no
        // retry - matches nothing and the unit reads "i???" forever.
        // Incomplete units are recorded at completion (OnUnitCompleted),
        // where the position is their own. Engine type id, not AUnitType.id().
        if (unit.isOur() && unit.isCompleted() && atlantis.cherryvis.ACherryVis.isEnabled()) {
            System.err.println("CVIS_FIRSTSEEN via=created id=" + unit.id()
                + " type=" + unit.type().ut().id + " pos=[" + unit.x() + "," + unit.y() + "]");
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
