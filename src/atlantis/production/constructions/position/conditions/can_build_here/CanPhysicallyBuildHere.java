package atlantis.production.constructions.position.conditions.can_build_here;

import atlantis.game.A;
import atlantis.information.strategy.Strategy;
import atlantis.map.MapTiles;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

public class CanPhysicallyBuildHere {
    /**
     * Returns true if game says it's possible to build given building at this position.
     */
    public static boolean check(AUnit builder, AUnitType building, APosition position) {
        if (position == null) {
            AbstractPositionFinder._STATUS = "POSITION IS NULL";
            return false;
        }
        if (builder == null) {
            AbstractPositionFinder._STATUS = "BUILDER IS NULL";
            return false;
        }

        if (ProtossAllowHereEarlyEvenWithoutRequirements.allow(builder, building, position)) {
            return true;
        }

//        if (building.isGasBuilding()) {
//            AAdvancedPainter.paintCircleFilled(position, 5, Color.Red);
//        }

        // Fix to allow UNEXPLORED positions and treat them as buildable
        if (
            building.isBase()
                && A.supplyTotal() >= 60
                && (!position.isExplored() || !position.isPositionVisible())
        ) {
            return true;
        }

        if (!isCanBuildHere(builder, building, position)) {
            if (positionUnexploredAndNotVisibleLetsDoit(position, building)) return true;
            if (allowEarlyForgeAndGatewayDuringForgeExpand(building, position)) {
                return true;
            }
            if (allowNearFirstUnfinishedPylon(building, position)) {
                return true;
            }

            // The status is a diagnostic string, so it is written in tests too -
            // it used to be guarded by Env.isTesting() on the grounds that tests
            // do not look at it. They do (RequestBuildingNearTest asserts the
            // status after each step), and a value that only exists in a game is a
            // value nobody can pin.
            AbstractPositionFinder._STATUS = "Can't physically build here";
            return false;
        }

        if (building.is(
            AUnitType.Protoss_Citadel_of_Adun,
            AUnitType.Protoss_Templar_Archives,
            AUnitType.Protoss_Observatory
        )) {
            if (Select.ourBuildingsWithUnfinished().countInRadius(3.5, position) > 0) {
                AbstractPositionFinder._STATUS = "Can't build CoE or Observatory here";
                return false;
            }
        }

        return true;
    }

    private static boolean allowNearFirstUnfinishedPylon(AUnitType building, APosition position) {
        if (building.isPylon()) return false;

        AUnit pylon = null;
        if (Count.pylons() == 0 && (pylon = Select.ourWithUnfinished(AUnitType.Protoss_Pylon).first()) != null) {
//            System.err.println("pylon = " + pylon + " " + A.minSec());
            if (pylon == null) return false;

            double distTo = pylon.distTo(position);
            return distTo >= 4 && distTo <= 7 && Select.ourBasesWithUnfinished().countInRadius(4.5, position) == 0;
        }

        return false;
    }

    private static boolean isCanBuildHere(AUnit builder, AUnitType building, APosition position) {
        return MapTiles.canBuildHere(builder, building, position);
    }

    private static boolean allowEarlyForgeAndGatewayDuringForgeExpand(AUnitType building, APosition position) {
        if (!We.protoss()) return false;
        if (!(building.isForge() || building.isGateway())) return false;

        if (A.supplyUsed() <= 13 && Strategy.get().isExpansion()) {
            HasPosition pylon = Select.ourOfTypeWithUnfinished(AUnitType.Protoss_Pylon).nearestTo(position);
            if (pylon == null) {
                pylon = ConstructionRequests.nearestOfTypeTo(AUnitType.Protoss_Pylon, position, 10);
            }

//            if (building.isGateway())
//                System.err.println("pylon = " + pylon
//                    + " / " + Select.ourOfTypeWithUnfinished(AUnitType.Protoss_Pylon).size()
//                    + " / " + ConstructionRequests.nearestOfTypeTo(AUnitType.Protoss_Pylon, position, 10)
//                    + " / " + ConstructionRequests.notStartedOfType(AUnitType.Protoss_Pylon).size()
//                );

            if (
//                pylon != null
//                    && position.isPositionVisible()
                position.isBuildableIncludeBuildings()
                    && position.isExplored()
//                    && position.translateByTiles(1, 0).isBuildable()
//                    && position.translateByTiles(0, 1).isBuildable()
                    && Select.ourBuildingsWithUnfinished().countInRadius(2.5, position) == 0
                    && (pylon == null || position.distTo(pylon) <= 5)
            ) {
                return true;
            }
        }
        return false;
    }

    private static boolean positionUnexploredAndNotVisibleLetsDoit(APosition position, AUnitType building) {
        return We.terran()
            && !position.isExplored()
            && !position.isPositionVisible()
            && !building.isCombatBuilding();
    }


}
