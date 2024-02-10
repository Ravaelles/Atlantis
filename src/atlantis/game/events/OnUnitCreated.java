package atlantis.game.events;

import atlantis.game.A;
import atlantis.map.path.OurClosestBaseToEnemy;
import atlantis.production.constructing.Construction;
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

        ProtossWarping.updateNewBuildingJustWarped(unit);

        if (unit.isABuilding()) {
            if (unit.isBase()) CancelNotStartedBases.cancelNotStartedBases();

            Construction construction = unit.construction();
            if (construction == null) {
                A.errPrintln("No construction for " + unit);
            }
            if (construction != null) {
                construction.releaseReservedResources();
            }
        }

        Queue.get().refresh();

        if (unit.isABuilding()) {
            if (unit.isBase()) OurClosestBaseToEnemy.clearCache();
        }

        // CENTER CAMERA ON THE FIRST BUNKER
//        if (unit.isBunker() && Env.isLocal() && Count.bunkers() == 0) CameraCommander.centerCameraOn(unit);
    }
}
