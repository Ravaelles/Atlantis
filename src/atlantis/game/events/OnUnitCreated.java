package atlantis.game.events;

import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.game.CameraCommander;
import atlantis.map.path.OurClosestBaseToEnemy;
import atlantis.production.constructing.ProtossWarping;
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
            handleOurUnit(unit);
        }
    }

    private static void handleOurUnit(AUnit unit) {
        Count.clearCache();
        Select.clearCache();

        Queue.get().refresh();

        // Apply construction fix: detect new Protoss buildings and remove them from queue.
        if (We.protoss() && unit.type().isABuilding()) {
            ProtossWarping.handleWarpingNewBuilding(unit);
        }

        if (unit.isABuilding()) {
            if (unit.isBase()) OurClosestBaseToEnemy.clearCache();
        }

        // CENTER CAMERA ON THE FIRST BUNKER
        if (unit.isBunker() && Env.isLocal() && Count.bunkers() == 0) CameraCommander.centerCameraOn(unit);
    }
}
