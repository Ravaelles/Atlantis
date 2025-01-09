package atlantis.production.constructions.position;

import atlantis.combat.micro.zerg.ZergCreepColony;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.region.MainRegion;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.position.base.FindPositionForBase;
import atlantis.production.constructions.position.protoss.FindPositionForCannon;
import atlantis.production.constructions.position.terran.SupplyDepotPositionFinder;
import atlantis.combat.micro.terran.bunker.position.NewBunkerPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.workers.FreeWorkers;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

public class FindPosition {
//    public static APosition findForBuildingNear(AUnitType building, HasPosition near) {
//        return findForBuilding(null, building, null, near, 8);
//    }

    public static APosition findForBuilding(
        AUnit builder, AUnitType building, Construction construction, HasPosition nearTo, double maxDistance
    ) {
        return findForBuildingRaw(builder, building, construction, nearTo, maxDistance);

//        APosition raw = findForBuildingRaw(builder, building, construction, nearTo, maxDistance);
//        if (raw == null) return null;
//        if (building.isBase() || building.isGasBuilding()) return raw;
//
//        return raw.translateByTiles(-building.dimensionLeftTiles(), -building.dimensionUpTiles());
    }

    private static APosition findForBuildingRaw(
        AUnit builder, AUnitType building, Construction construction, HasPosition nearTo, double maxDistance
    ) {
        AbstractPositionFinder._STATUS = "Reset";

        // =========================================================

        nearTo = DefineNearTo.defineNearTo(building, nearTo);
//        System.err.println("nearTo = " + nearTo + " for " + building + " / main = " + Select.mainOrAnyBuildingPosition());

        if (builder == null) builder = defineBuilder();

        if (maxDistance <= 5 && building.isBunker()) maxDistance = 10;
        if (maxDistance < 0) maxDistance = MaxBuildingDist.MAX_DIST;

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
        // Standard building

        APosition standardPosition = APositionFinder.findStandardPosition(builder, building, nearTo, maxDistance);

        if (standardPosition == null && !ignorePositionNotFoundException(building, standardPosition)) {
            ErrorLog.printMaxOncePerMinute(
                "findStandardPosition returned null at " + A.minSec()
                    + "\n    / reason:" + AbstractPositionFinder._STATUS
                    + "\n    / building:" + building
                    + "\n    / near:" + nearTo
                    + "\n    / freeSupply:" + AGame.supplyFree()
                    + " (" + AGame.supplyUsed() + "/" + AGame.supplyTotal() + ")"
                    + "\n    / minerals:" + A.minerals()
                    + "\n    / builder:" + builder
                    + "\n    / max:" + maxDistance
            );
        }

        APositionFinder.clearCache();
        return standardPosition;
    }

    private static AUnit defineBuilder() {
        AUnit builder = FreeWorkers.get().first();
        if (builder != null) return builder;

        return Select.ourWorkers().first();
    }

    private static boolean ignorePositionNotFoundException(AUnitType building, APosition standardPosition) {
        if (We.protoss() && !building.needsPower()) return false;

        if (A.supplyFree() >= 3) {
            if (building.isPylon()) return true;
            if (!"OK".equals(AbstractPositionFinder._STATUS)) {
                if ("Can't physically build here".equals(AbstractPositionFinder._STATUS)) return true;
                if (AbstractPositionFinder._STATUS.contains(" modulo ")) return true;
            }
        }

        if (standardPosition == null
            && Count.workers() >= 4
            && (!We.protoss() || Count.pylons() >= 1)
        ) return true;

        if (We.protoss() && (Count.pylons() - 1) <= Count.forgeWithUnfinished()) return true;

        return false;
    }

    private static APosition forCombatBuilding(
        AUnit builder, AUnitType building, Construction construction, HasPosition nearTo, double maxDistance
    ) {
        if (building.isCannon()) {
            return FindPositionForCannon.find(nearTo, builder, construction);
        }

        // =========================================================

        else if (building.isBunker()) {
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
