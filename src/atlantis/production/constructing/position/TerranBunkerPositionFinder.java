package atlantis.production.constructing.position;

import atlantis.combat.micro.terran.TerranBunker;
import atlantis.map.base.ABaseLocation;
import atlantis.map.choke.AChoke;
import atlantis.map.base.Bases;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.Construction;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class TerranBunkerPositionFinder {
    private static AUnitType bunker = AUnitType.Terran_Bunker;

    public static APosition findPosition(AUnit builder, Construction order, HasPosition nearTo) {

        // @TODO: This is a problem. Leave the logic for TerranBunker as it is now...
//        if (nearTo != null) {
//            return APositionFinder.findStandardPosition(builder, bunker, nearTo, 12);
//        }


        HasPosition hasPosition = TerranBunker.get().nextBuildingPosition();
        return hasPosition != null ? hasPosition.position() : null;
    }

    // =========================================================

//    private static HasPosition defineNearTo(Construction order) {
//        if (order != null && order.productionOrder() != null && order.productionOrder().getModifier() != null) {
//            String locationModifier = order.productionOrder().getModifier();
//            return defineBunkerPosition(locationModifier);
//        }
//        else if (order == null) {
//            return defineBunkerPosition(PositionModifier.MAIN);
//        }
//        else {
//            return defineBunkerPosition(PositionModifier.NATURAL);
//        }
//    }

    private static APosition defineBunkerPosition(String locationModifier) {
        AUnit mainBase = Select.main();
        if (mainBase == null || mainBase.position() == null) {
            return null;
        }

        // =========================================================
        // at MAIN

        if (locationModifier.equals(PositionModifier.MAIN)) {
            AChoke mainChoke = Chokes.mainChoke();
            if (mainChoke != null) {
                return mainBase.translateTilesTowards(mainChoke, 6)
                    .makeBuildable(8)
                    .makeWalkable(8);
            }

            return mainBase.position();
        }

        // =========================================================
        // at MAIN CHOKEPOINT

        else if (locationModifier.equals(PositionModifier.MAIN_CHOKE)) {
            AChoke mainChoke = Chokes.mainChoke();
            if (mainChoke != null) {
                return APosition.create(mainChoke.center())
                    .translateTilesTowards(mainBase, 5)
                    .makeWalkable(8);
            }
        }

        // =========================================================
        // at NATURAL CHOKEPOINT

        else if (locationModifier.equals(PositionModifier.NATURAL_CHOKE)) {
            AChoke chokepointForNatural = Chokes.natural(mainBase.position());
            if (chokepointForNatural != null && mainBase != null) {
                ABaseLocation naturalBase = Bases.natural(Select.main().position());
                return APosition.create(chokepointForNatural.center())
                    .translateTilesTowards(naturalBase, 5)
                    .makeWalkable(8);


//                    System.err.println(nearTo);
//                    System.err.println("DIST TO CHOKE = " + nearTo.distanceTo(chokepointForNatural.getCenter()));
//                    System.err.println("DIST TO REGION = " + nearTo.distanceTo(nearTo.getRegion().getCenter()));
            }
        }

        // Invalid location
        System.err.println("Can't define bunker location: " + locationModifier);
        return null;
    }

}
