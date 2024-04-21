package atlantis.production.constructing.position;

import atlantis.combat.micro.zerg.ZergCreepColony;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.region.MainRegion;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.position.base.FindPositionForBase;
import atlantis.production.constructing.position.terran.SupplyDepotPositionFinder;
import atlantis.combat.micro.terran.bunker.position.NewBunkerPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.workers.FreeWorkers;
import atlantis.util.log.ErrorLog;

public class FindPosition {
    public static APosition findForBuildingNear(AUnitType building, HasPosition near) {
        return findForBuilding(null, building, null, near, 8);
    }

    public static APosition findForBuilding(
        AUnit builder, AUnitType building, Construction construction, HasPosition nearTo, double maxDistance
    ) {
        nearTo = DefineNearTo.defineNearTo(building, nearTo);

        if (builder == null) builder = FreeWorkers.get().first();

        if (maxDistance <= 5 && building.isBunker()) maxDistance = 10;
        if (maxDistance < 0) maxDistance = 29;
        if (construction != null) construction.setMaxDistance(maxDistance);

        // === GAS extracting buildings ============================

        if (building.isGasBuilding()) return GasBuildingPositionFinder.findPositionForGasBuilding(building, nearTo);

            // === Base ================================================

        else if (building.isBase()) {
            return FindPositionForBase.forNewBase(builder, building, construction, nearTo);
        }

        // =========================================================

        else if (building.isSupplyDepot()) {
            APosition position = SupplyDepotPositionFinder.findPosition(builder, construction, nearTo);
            if (position == null) {
                ErrorLog.printMaxOncePerMinute(
                    "SupplyDepotPositionFinder returned null \n    / near:" + nearTo
                        + "\n    Fallback to default now"
                );

//                AUnit near = Select.ourBuildings().notInRadius(3, nearTo).random();
//                if (nearTo != null && nearTo.distTo(near) >= 3) {
//                    position = findForBuilding(builder, building, construction, near, 35);
//
//                    if (position == null) {
//                        ErrorLog.printMaxOncePerMinute(
//                            "SupplyDepotPositionFinder CONSEQUENTLY returned null \n    / near:" + nearTo
//                                + "\n    There's no hope now."
//                        );
//                    }
//                    else {
//                        ErrorLog.printMaxOncePerMinute("Interestingly, Depot fix helped this time. Near: " + near);
//                    }
//                }
            }

            if (position != null) return position;
            return null;
        }

        // === Combat building =====================================

        else if (building.isCombatBuilding()) {
            APosition position = forCombatBuilding(builder, building, construction, nearTo, maxDistance);
            if (position != null) return position;
        }

        // =========================================================
        // STANDARD BUILDINGS

        // If we didn't specify location where to build, build somewhere near the main base
        nearTo = DefineNearTo.defineNearTo(nearTo);

        // Hopeless case, all units have died, just quit.
        if (nearTo == null) {
            ErrorLog.printMaxOncePerMinute("nearTo is still null for " + building);
            return null;
        }

        // =========================================================
        // Standard place

        APosition standardPosition = APositionFinder.findStandardPosition(builder, building, nearTo, maxDistance);

        if (standardPosition == null && Count.workers() >= 4) {
            ErrorLog.printMaxOncePerMinute(
                "findStandardPosition returned null"
                    + "\n    / reason:" + AbstractPositionFinder._CONDITION_THAT_FAILED
                    + "\n    / building:" + building
                    + "\n    / near:" + nearTo
                    + "\n    / builder:" + builder
                    + "\n    / max:" + maxDistance
            );
        }

//        System.err.println("position for " + building + " = " + standardPosition);

        return standardPosition;
    }

    private static APosition forCombatBuilding(
        AUnit builder, AUnitType building, Construction construction, HasPosition nearTo, double maxDistance
    ) {
        if (building.isBunker()) {
//            return TerranBunkerPositionFinder.findPosition(builder, construction, nearTo);
//            return (new NewBunkerPositionFinder(nearTo, builder, construction)).find();
            APosition thePosition = (new NewBunkerPositionFinder(nearTo, builder)).find();

            if (Count.bunkers() <= 0 && thePosition != null && !thePosition.regionsMatch(MainRegion.mainRegion())) {
                if (
                    construction.productionOrder().getModifier() != null
                        && construction.productionOrder().getModifier().equals("MAIN_CHOKE")
                ) {
                    ErrorLog.printMaxOncePerMinute("Fix for first MAIN_CHOKE bunker, place it in main.");
                    return (new NewBunkerPositionFinder(Select.main(), builder)).find();
                }
            }

            return thePosition;
        }

        // =========================================================
        // Creep colony

        else if (building.is(AUnitType.Zerg_Creep_Colony)) {
            return ZergCreepColony.findPosition(building, builder, construction);
        }

        return null;
    }
}
