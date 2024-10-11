package atlantis.production.constructing.position.conditions;

import atlantis.Atlantis;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.We;

public class CanPhysicallyBuildHere {
    /**
     * Returns true if game says it's possible to build given building at this position.
     */
    public static boolean check(AUnit builder, AUnitType building, APosition position) {
        if (position == null) {
            AbstractPositionFinder._CONDITION_THAT_FAILED = "POSITION IS NULL";
            return false;
        }
        if (builder == null) {
            AbstractPositionFinder._CONDITION_THAT_FAILED = "BUILDER IS NULL";
            return false;
        }

//        if (building.isGasBuilding()) {
//            AAdvancedPainter.paintCircleFilled(position, 5, Color.Red);
//        }

        // Fix to allow UNEXPLORED positions and treat them as buildable
        if (
            building.isBase()
                && !building.isGasBuilding()
                && A.supplyTotal() >= 60
                && (!position.isExplored() || !position.isPositionVisible())
        ) return true;

//        if (!position.isExplored() && position.regionsMatch(MainRegion.mainRegion())) return true;

//        if (
//            (!position.isExplored() || !position.isPositionVisible())
//                &&
//                (building.isBase() || building.isCombatBuilding())
//        ) return true;

//        if (!We.zerg() && Atlantis.game().hasCreep(position.toTilePosition())) {
//            AbstractPositionFinder._CONDITION_THAT_FAILED = "Ugly creep on it";
//            return false;
//        }

        if (!Atlantis.game().canBuildHere(position.toTilePosition(), building.ut(), builder.u())) {
            if (positionUnexploredAndNotVisibleLetsDoit(position, building)) return true;

            if (We.protoss() && A.supplyUsed() <= 12 && (building.isForge() || building.isGateway())) {
//                System.err.println("---------------------- ");
//                System.err.println("position = " + position);
//                System.err.println("Select.ourOfTypeWithUnfinished(AUnitType.Protoss_Pylon).inRadius(4.8, position).atLeast(1) = " + Select.ourOfTypeWithUnfinished(AUnitType.Protoss_Pylon).inRadius(4.8, position).atLeast(1));
                HasPosition pylon = Select.ourOfTypeWithUnfinished(AUnitType.Protoss_Pylon).nearestTo(position);
//                System.err.println("pylon A = " + pylon);
                if (pylon == null) {
                    pylon = ConstructionRequests.nearestOfTypeTo(AUnitType.Protoss_Pylon, position, 10);
//                    System.err.println("pylon B = " + pylon);
//                    System.err.println("requests = " + ConstructionRequests.all());
                }

//                System.err.println("pylon C = " + pylon);
//                if (pylon != null) {
//                    System.err.println("AAA = " + pylon.distTo(position));
//                }

                // Fix for early building, place it just below pylon
//                if (pylon == null) {
//                    APosition pylonConstr = ConstructionRequests.nearestOfTypeTo(AUnitType.Protoss_Pylon, position, 100);
//                    System.err.println("pylonConstr = " + pylonConstr);
//                    if (pylonConstr != null) {
//                        position = pylonConstr.translateByTiles(0, 2);
//                        System.err.println("FIX position = " + position);
//                    }
//                }

                if (
                    pylon != null
                        && position.isBuildable()
//                        && position.translateByTiles(building.getTileWidth() / 2, building.getTileHeight() / 2).isBuildable()
                        && position.distTo(pylon) <= 4.9
                ) {
                    return true;
                }
            }

            AbstractPositionFinder._CONDITION_THAT_FAILED = "Can't physically build here";
            return false;
        }

        return true;
    }

    private static boolean positionUnexploredAndNotVisibleLetsDoit(APosition position, AUnitType building) {
        return We.terran()
            && !position.isExplored()
            && !position.isPositionVisible()
            && !building.isCombatBuilding();
    }
}
