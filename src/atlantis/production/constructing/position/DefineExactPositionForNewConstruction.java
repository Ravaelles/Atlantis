package atlantis.production.constructing.position;

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

        if (order != null && order.isUsingExactPosition() && order.atPosition() != null) {
            System.err.println("Using exact position for " + building + " - " + order);
            positionToBuild = APosition.create(order.atPosition());
//            CameraCommander.centerCameraOn(positionToBuild);
        }
        else {
            positionToBuild = newConstructionOrder.findPositionForNewBuilding();
        }
        newConstructionOrder.setPositionToBuild(positionToBuild);

        // =========================================================

        return positionToBuild;
    }

    private static void defineBunkerPositionSearchConfig(AUnitType building, ProductionOrder order, Construction newConstructionOrder) {
        if (order.getModifier() != null) {
            if (!order.isUsingExactPosition() && order.atPosition() == null) {
                APosition position = definePosition(building, order, newConstructionOrder);
                System.err.println("FORCE BUNKER position = " + position + " / " + order.getModifier());

                order.forceSetPosition(position);
            }
        }

        if (order.atPosition() != null) order.markAsUsingExactPosition();
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
