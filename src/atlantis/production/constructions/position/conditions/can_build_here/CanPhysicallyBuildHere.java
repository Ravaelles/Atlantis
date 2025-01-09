package atlantis.production.constructions.position.conditions.can_build_here;

import atlantis.Atlantis;
import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.information.strategy.Strategy;
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

        if (AllowHereEarlyEvenWithoutRequirements.allowEarlyBuildingWithoutRequirements(builder, building, position))
            return true;

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

        if (!isCanBuildHere(builder, building, position)) {
            if (positionUnexploredAndNotVisibleLetsDoit(position, building)) return true;
            if (allowEarlyForgeAndGatewayDuringForgeExpand(building, position)) return true;
            if (allowNearFirstUnfinishedPylon(building, position)) return true;

            if (!Env.isTesting()) AbstractPositionFinder._STATUS = "Can't physically build here";
            return false;
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
            return distTo >= 4 && distTo <= 7;
        }

        return false;
    }

    private static boolean isCanBuildHere(AUnit builder, AUnitType building, APosition position) {
        if (Env.isTesting()) {
            if (!apprxForTesting(building, position)) {
                return false;
            }
            return true;
        }

        return Atlantis.game().canBuildHere(position.toTilePosition(), building.ut(), builder.u());
    }

    private static boolean allowEarlyForgeAndGatewayDuringForgeExpand(AUnitType building, APosition position) {
        if (
            We.protoss()
                && A.supplyUsed() <= 13
                && Strategy.get().isExpansion()
                && (building.isForge() || building.isGateway())
        ) {
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

    private static boolean apprxForTesting(AUnitType building, APosition position) {
//        System.err.println(Select.ourBasesWithUnfinished().distToNearest(position));

        if (We.protoss() && building.needsPower()) {
            if (Select.ourOfType(AUnitType.Protoss_Pylon).countInRadius(5.98, position) == 0) {
                AbstractPositionFinder._STATUS = "[Testing] No power";
                return false;
            }
        }

        if (!position.isBuildableIncludeBuildings()) {
            AbstractPositionFinder._STATUS = "[Testing] Not buildable";
            return false;
        }

        int countNearBuildings = Select.ourBuildingsWithUnfinished().inRadius(2.95, position).count();
        if (
//            Select.ourBasesWithUnfinished().countInRadius(4.02, position) == 0
//                && Select.ourBuildingsWithUnfinished().countInRadius(3.02, position) == 0
            countNearBuildings == 0
        ) {
//            System.err.println("------- NOTHING " + position + " -------");
            return true;
        }

//        System.err.println("countNearBuildings = " + countNearBuildings);

        AbstractPositionFinder._STATUS = "[Testing] Can't physically build here";
        return false;
    }
}
