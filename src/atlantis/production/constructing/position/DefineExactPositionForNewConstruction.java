package atlantis.production.constructing.position;

import atlantis.combat.micro.terran.bunker.position.NewBunkerPositionFinder;
import atlantis.game.CameraCommander;
import atlantis.map.position.APosition;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.position.modifier.PositionModifier;
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
        if (order != null && order.isUsingExactPosition() && order.atPosition() != null) {
            //            System.err.println("Using exact position for " + building + " - " + order);
            positionToBuild = APosition.create(order.atPosition());
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

        if (order.getModifier() != null && order.atPosition() == null) {
            APosition position = definePosition(building, order, newConstructionOrder);
//            System.err.println("DEFINE BUNKER position = " + position + " / " + order.getModifier());

            order.forceSetPosition(position);
        }

        if (order.atPosition() != null && order.isUsingExactPosition()) {
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

        if (order.atPosition() != null) order.markAsUsingExactPosition();

//        System.err.println("=== POST ========================= ");
//        System.err.println("order    = " + order);
//        System.err.println("modifier = " + order.getModifier());
//        System.err.println("at       = " + order.atPosition());
//        System.err.println("isUsingExactPosition = " + order.isUsingExactPosition());
//        System.err.println("============================================================== ");
    }

    private static APosition definePosition(AUnitType building, ProductionOrder order, Construction newConstructionOrder) {
        if (order.atPosition() != null && order.isUsingExactPosition()) {
            return APosition.create(order.atPosition());
        }

        return PositionModifier.toPosition(
            order.getModifier(), building, null, newConstructionOrder
        );
    }
}
