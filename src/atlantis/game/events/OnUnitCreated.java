package atlantis.game.events;

import atlantis.game.A;
import atlantis.map.path.OurClosestBaseToEnemy;
import atlantis.production.constructing.ProtossWarping;
import atlantis.production.dynamic.expansion.decision.CancelNotStartedBases;
import atlantis.production.orders.production.queue.Queue;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;
import bwapi.Unit;

public class OnUnitCreated {
    public static void onUnitCreated(Unit u) {
        if (u == null) {
            System.err.println("onUnitCreate got null");
            return;
        }

        AUnit unit = AUnit.createFrom(u);

        // Our unit
        if (unit.isOur() && A.now() >= 2) {
            handleOurUnitCreated(unit);
        }
    }

    private static void handleOurUnitCreated(AUnit unit) {
        Count.clearCache();
        Select.clearCache();

        // Apply construction fix: detect new Protoss buildings and remove them from queue.
        if (We.protoss() && unit.type().isABuilding()) {
            ProtossWarping.handleNewBuildingWarped(unit);
        }

        if (unit.isABuilding()) {
            if (unit.isBase()) CancelNotStartedBases.cancelNotStartedBases();
        }

        Queue.get().refresh();

        if (unit.isABuilding()) {
            if (unit.isBase()) OurClosestBaseToEnemy.clearCache();
        }

        // CENTER CAMERA ON THE FIRST BUNKER
//        if (unit.isBunker() && Env.isLocal() && Count.bunkers() == 0) CameraCommander.centerCameraOn(unit);
    }
}
