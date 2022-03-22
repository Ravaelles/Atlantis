package atlantis.production.constructing.position;

import atlantis.combat.micro.terran.TerranBunker;
import atlantis.map.ABaseLocation;
import atlantis.map.AChoke;
import atlantis.map.Bases;
import atlantis.map.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.Construction;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class TerranBunkerPositionFinder {

    private static AUnitType bunker = AUnitType.Terran_Bunker;

    public static APosition findPosition(AUnit builder, Construction order) {
        HasPosition nearTo = defineNearTo(order);
        if (nearTo == null) {
            nearTo = Select.ourBuildings().first();
        }

        // =========================================================

        if (nearTo != null && Count.bunkers() > 0) {
            AUnit otherBunker = Select.ourOfType(bunker).nearestTo(Select.main());
            if (otherBunker != null) {
                nearTo = otherBunker;
            }
        }

        // =========================================================

//        if (nearTo == null) {
//            AUnit existingBunker = Select.ourOfType(AUnitType.Terran_Bunker).first();
//            if (existingBunker != null) {
//                nearTo = existingBunker.position();
//                APosition defendPoint = MissionDefend.getInstance().focusPoint();
//                if (defendPoint != null) {
//                    nearTo = nearTo.translatePercentTowards(defendPoint, 15);
//                }
//            }
//            else {
//                AUnit mainBase = Select.main();
//                if (mainBase != null) {
//                    nearTo = Select.main().position();
//                }
//
//                AChoke mainChoke = Chokes.mainChoke();
//                if (mainChoke != null) {
//                    int tilesAway = Enemy.zerg() ? 2 : 7;
//                    nearTo = nearTo.translateTilesTowards(tilesAway, mainChoke);
//                }
//            }
//        }

        // =========================================================
        // Find position near specified place

//        return APositionFinder.findStandardPosition(builder, bunker, nearTo, 30);
        HasPosition hasPosition = TerranBunker.get().nextBuildingPosition();
        return hasPosition != null ? hasPosition.position() : null;
    }

    // =========================================================

    private static HasPosition defineNearTo(Construction order) {
        if (order != null && order.productionOrder() != null && order.productionOrder().getModifier() != null) {
            String locationModifier = order.productionOrder().getModifier();
            return defineBunkerPosition(locationModifier);
        }
        else if (order == null) {
            return defineBunkerPosition(PositionModifier.MAIN);
        }
        else {
            return defineBunkerPosition(PositionModifier.NATURAL);
        }
    }

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
                return mainBase.translateTilesTowards(mainChoke, 4);
            }

            return mainBase.position();
        }

        // =========================================================
        // at MAIN CHOKEPOINT

        else if (locationModifier.equals(PositionModifier.MAIN_CHOKE)) {
            AChoke mainChoke = Chokes.mainChoke();
            if (mainChoke != null) {
                return APosition.create(mainChoke.center()).translateTilesTowards(mainBase, 4);
            }
        }

        // =========================================================
        // at NATURAL CHOKEPOINT

        else if (locationModifier.equals(PositionModifier.NATURAL_CHOKE)) {
            AChoke chokepointForNatural = Chokes.natural(mainBase.position());
            if (chokepointForNatural != null && mainBase != null) {
                ABaseLocation naturalBase = Bases.natural(Select.main().position());
                return APosition.create(chokepointForNatural.center()).translateTilesTowards(naturalBase, 5);

//                    System.out.println();
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
