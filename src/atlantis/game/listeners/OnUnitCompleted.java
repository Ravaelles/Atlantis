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

            // CherryVis first-seen (Stardust pattern, NEXT #68 follow-up): this
            // is where a trained/warped unit is first recorded - at completion,
            // with its OWN position. Recording at creation caught the
            // producer's position (every late Probe at the Nexus) and the
            // viewer matched nothing. Dedup in record() keeps this harmless
            // against the seed/discovery paths.
            if (unit.isCompleted() && atlantis.cherryvis.ACherryVis.isEnabled()) {
                atlantis.cherryvis.simple.ACherryVis_UnitFirstSeen.record(
                    atlantis.game.A.now(), unit.id(), unit.type().ut().id, unit.x(), unit.y()
                );
            }

            OnOurNewUnitCompleted.ourNewUnitCompleted(unit);
        }
    }
}
