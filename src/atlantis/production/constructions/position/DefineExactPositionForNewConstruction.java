package atlantis.production.constructions.position;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.BuildingTilesAreOccupied;

public class DefineExactPositionForNewConstruction {
    public static APosition exactPositionForNewConstruction(
        AUnitType building, ProductionOrder order, Construction newConstructionOrder
    ) {
        APosition positionToBuild;

        // =========================================================

//        if (order.isUsingExactPosition() && order.atPosition() != null) {
        if (order != null && order.isUsingExactPosition() && order.aroundPosition() != null) {
            //            System.err.println("Using exact position for " + building + " - " + order);
            APosition exact = APosition.create(order.aroundPosition());

            // An exact position is a request, not a promise: the world changes after
            // it is made (another building goes up, a unit parks on the tile), and
            // nothing re-validated it - the value was handed back verbatim, so the
            // builder walked to a tile it could never build on and was cancelled
            // after ~57s ("CyberneticsC took too long / buildable:false"). Check the
            // tiles before honouring it, and fall back to a real search when they are
            // no longer free.
            if (BuildingTilesAreOccupied.check(exact, building)) {
                order.markAsNotUsingExactPosition();
                positionToBuild = newConstructionOrder.findPositionForNewBuilding();
            }
            else {
                positionToBuild = exact;
            }
            //            CameraCommander.centerCameraOn(positionToBuild);
        }
        else {
            positionToBuild = newConstructionOrder.findPositionForNewBuilding();
        }
        newConstructionOrder.setPositionToBuild(positionToBuild);
//        }

        // =========================================================

        return positionToBuild;
    }
}
