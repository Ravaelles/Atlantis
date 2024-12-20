package atlantis.game.listeners;

import atlantis.game.A;
import atlantis.game.event.Events;
import atlantis.map.path.OurClosestBaseToEnemy;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.protoss.ProtossWarping;
import atlantis.production.constructing.terran.TerranNewBuilding;
import atlantis.production.dynamic.expansion.ExpansionCommander;
import atlantis.production.dynamic.expansion.decision.CancelNotStartedBases;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

public class OnOurUnitCreated {
    public static void update(AUnit unit) {
        Count.clearCache();
        Select.clearCache();

        if (unit.isABuilding()) {
            ProtossWarping.updateNewBuildingJustWarped(unit);
            TerranNewBuilding.updateNewBuilding(unit);

            if (unit.isBase()) {
                ExpansionCommander.justExpanded();
                CancelNotStartedBases.cancelNotStartedOrEarlyBases(unit);
            }

            Construction construction = unit.construction();
            if (construction == null && !unit.type().isAddon()) {
                A.errPrintln("No construction for " + unit);
            }
//            if (construction != null) {
//                construction.releaseReservedResources();
//            }

            ProductionOrder order = unit.productionOrder();
            if (order != null) order.releasedReservedResources();
            else if (construction != null) construction.releaseReservedResources();

            if (We.protoss() && unit.type().isPylon() && Select.countOurOfTypeWithUnfinished(AUnitType.Protoss_Pylon) == 1) {
                AUnit builder = unit.construction() == null ? null : unit.construction().builder();
                Events.dispatch("FirstPylonUnitCreated", unit, builder);
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
