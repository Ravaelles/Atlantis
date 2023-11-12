package atlantis.production.constructing.position;

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
            if (order.getModifier() != null) {
                APosition position = PositionModifier.toPosition(
                    order.getModifier(), building, null, newConstructionOrder
                );
                System.err.println("FORCE BUNKER position from BO = " + position + " / " + order.getModifier());

                order.forceSetPosition(position);
                if (order.atPosition() != null) order.markAsUsingExactPosition();
            }
        }

        // =========================================================

        if (order != null && order.isUsingExactPosition() && order.atPosition() != null) {
            System.err.println("Using exact position for " + building + " - " + order);
            positionToBuild = APosition.create(order.atPosition());
        }
        else {
            positionToBuild = newConstructionOrder.findPositionForNewBuilding();
        }
        newConstructionOrder.setPositionToBuild(positionToBuild);

        // =========================================================

        return positionToBuild;
    }
}
