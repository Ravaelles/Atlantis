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

            // CherryVis first-seen (Stardust pattern, NEXT #68 follow-up):
            // buildings record at completion with their own position (a warp
            // never moves), but MOBILE units still report the producer's
            // position here - every late Probe recorded at the Nexus, and the
            // viewer matches nothing in the crowd. Mobile units are deferred a
            // few frames, until they walk out with a position of their own
            // (processed per frame from ACherryVis.update).
            if (unit.isCompleted() && atlantis.cherryvis.ACherryVis.isEnabled()) {
                if (unit.isABuilding()) {
//                    System.err.println("CVIS_FIRSTSEEN via=completed id=" + unit.id()
//                        + " type=" + unit.type().ut().id + " pos=[" + unit.x() + "," + unit.y() + "]");
                    atlantis.cherryvis.simple.ACherryVis_UnitFirstSeen.record(
                        atlantis.game.A.now(), unit.id(), unit.type().ut().id, unit.x(), unit.y()
                    );
                    atlantis.cherryvis.simple.ACherryVis_UnitFirstSeen.noteEngineIdOf(unit);
                }
                else {
//                    System.err.println("CVIS_FIRSTSEEN deferred id=" + unit.id());
                    atlantis.cherryvis.simple.ACherryVis_UnitFirstSeen.defer(unit.id());
                }
            }

            OnOurNewUnitCompleted.ourNewUnitCompleted(unit);
        }
    }
}
