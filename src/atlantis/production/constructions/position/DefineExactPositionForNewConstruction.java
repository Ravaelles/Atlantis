package atlantis.production.constructions.position;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.units.BuildingTilesAreOccupied;
import atlantis.production.constructions.position.modifier.PositionModifier;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;

public class DefineExactPositionForNewConstruction {
    public static APosition exactPositionForNewConstruction(
        AUnitType building, ProductionOrder order, Construction newConstructionOrder
    ) {
        APosition positionToBuild;

        // === Bunker ===========================================

        if (building.isBunker()) {
            defineBunkerPositionSearchConfig(building, order, newConstructionOrder);
        }

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

    private static void defineBunkerPositionSearchConfig(AUnitType building, ProductionOrder order, Construction newConstructionOrder) {
//        System.err.println("--- PRE ---------------------------- ");
//        System.err.println("order    = " + order);
//        System.err.println("modifier = " + order.getModifier());
//        System.err.println("at       = " + order.atPosition());
//        System.err.println("isUsingExactPosition = " + order.isUsingExactPosition());
//        System.err.println("------------------------------------ ");

        if (order.getModifier() != null && order.aroundPosition() == null) {
            APosition position = definePosition(building, order, newConstructionOrder);
//            System.err.println("DEFINE BUNKER position = " + position + " / " + order.getModifier());

            order.setAroundPosition(position);
        }

        if (order.aroundPosition() != null && order.isUsingExactPosition()) {
            order.markAsUsingExactPosition();
//            System.err.println("@@@@@@@@@@@@@@@@@@@@@ OK, RETURN EXACT " + order.atPosition());
            return;
        }
//        else {
//            (new NewBunkerPositionFinder()).find();
//        }

//        if (order.getModifier() != null) {
//            if (!order.isUsingExactPosition() && order.atPosition() == null) {
//                APosition position = definePosition(building, order, newConstructionOrder);
//                System.err.println("FORCE BUNKER position = " + position + " / " + order.getModifier());
//
//                order.forceSetPosition(position);
//            }
//        }

        if (order.aroundPosition() != null) order.markAsUsingExactPosition();

//        System.err.println("=== POST ========================= ");
//        System.err.println("order    = " + order);
//        System.err.println("modifier = " + order.getModifier());
//        System.err.println("at       = " + order.atPosition());
//        System.err.println("isUsingExactPosition = " + order.isUsingExactPosition());
//        System.err.println("============================================================== ");
    }

    private static APosition definePosition(AUnitType building, ProductionOrder order, Construction newConstructionOrder) {
        if (order.aroundPosition() != null && order.isUsingExactPosition()) {
            return APosition.create(order.aroundPosition());
        }

        return PositionModifier.toPosition(
            order.getModifier(), building, null, newConstructionOrder
        );
    }
}
