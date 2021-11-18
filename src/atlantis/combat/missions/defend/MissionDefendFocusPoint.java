package atlantis.combat.missions;

import atlantis.AGame;
import atlantis.enemy.EnemyInformation;
import atlantis.map.AChoke;
import atlantis.map.Chokes;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.Cache;

public class MissionDefendFocusPoint extends MissionFocusPoint {

    private Cache<APosition> cache = new Cache<>();

    // =========================================================

    @Override
    public APosition focusPoint() {
        return cache.get(
            "focusPoint",
            30,
            () -> {
                if (AGame.isUms()) {
                    return null;
                }

                // === Enemies that breached into base =============

                if (Have.base()) {
                    AUnit enemyNear = EnemyInformation.enemyNearAnyOurBuilding();
//                    AUnit enemyInBase = Select.enemy().combatUnits().effVisible().inRadius(10, Select.main()).first();
                    if (enemyNear != null) {
                        return enemyNear.position();
                    }
                }

                // === Natural choke if second base ================

                if (Count.basesWithUnfinished() >= 2) {
                    AChoke natural = Chokes.natural();
                    if (natural != null) {
                        return natural.center();
                    }
                }

                // === Main choke ================

                AChoke mainChoke = Chokes.mainChoke();
                if (mainChoke != null) {
                    return mainChoke.center();
                }

                // === Focus enemy attacking the main base =================

                AUnit mainBase = Select.main();
                if (mainBase == null) {
                    return null;
                }

                AUnit nearEnemy = Select.enemy()
                        .combatUnits()
                        .excludeTypes(AUnitType.Protoss_Observer, AUnitType.Zerg_Overlord)
                        .effVisible()
                        .inRadius(12, mainBase)
                        .nearestTo(mainBase);
                if (nearEnemy != null) {
                    return nearEnemy.position();
                }
//
//                // === Gather around defensive buildings ===================
//
//                AUnit defBuilding = Select.ourOfTypeIncludingUnfinished(AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND).mostDistantTo(mainBase);
//                if (defBuilding != null) {
//                    return defBuilding.position().translateTilesTowards(mainBase.position(), 5);
//                }

                // === Return position near the choke point ================

//                AChoke chokepointForNatural = AMap.getChokeForNatural(mainBase.position());
//                if (chokepointForNatural != null) {
//                    return APosition.create(chokepointForNatural.getCenter());
//                }

                // === Return position near the first building ================

                AUnit building = Select.ourBuildings().first();
                if (building != null) {
                    return APosition.create(Chokes.nearestChoke(building.position()).center());
                }

                return null;
            }
        );
    }

//    public static APosition focusPoint() {
//
//        // === Load infantry into bunkers ==========================
//
//        if (TerranInfantry.tryLoadingInfantryIntoBunkerIfPossible(unit)) {
//            return true;
//        }
//
//        // =========================================================
//        // Too close to the chokepoint
//        if (isCriticallyCloseToFocusPoint(unit, focusPoint)) {
//            boolean result = unit.moveAwayFrom(
//                    focusPoint, 0.5, "Too close (" + unit.distanceTo(focusPoint) + ")"
//            );
//            if (result) {
//                return true;
//            }
//            else {
//                unit.setTooltip("FAILED Too close");
//            }
//        }
//
//        // =========================================================
//        // Unit is quite close to the choke point
//        else if (isCloseEnoughToFocusPoint(unit, focusPoint)) {
//
//            // Too many stacked units
//            if (isTooManyUnitsAround(unit, focusPoint)) {
//                if (unit.isMoving()) {
//                    unit.stop("#DHold");
//                    return true;
//                }
//            }
//
//            // Everything is okay, be here
//            else {
//                if (unit.type().isTank() && !unit.isSieged()) {
//                    unit.siege();
//                    return true;
//                }
//                else {
//                    if (unit.isMoving()) {
//                        unit.stop("#DHold");
//                    }
//                    return true;
//                }
//            }
//        }
//
//        // =========================================================
//        // Unit is far from choke point
//        else {
//            if (unit.distanceTo(focusPoint) > 3) {
//                unit.move(focusPoint, UnitActions.MOVE, "#DPositioning");
//                return true;
//            }
//        }
//
//        return false;
//    }

    // =========================================================


//    /**
//     * AUnit will go towards important choke point near main base.
//     */
//    private static boolean moveUnitIfNeededNearChokePoint(AUnit unit) {
//        return false;
//    }
//
//    private static boolean isTooManyUnitsAround(AUnit unit, APosition focusPoint) {
//        return Select.ourCombatUnits().inRadius(1.0, unit).count() >= 3;
//    }
//
//    private static boolean isCloseEnoughToFocusPoint(AUnit unit, APosition focusPoint) {
//        if (unit == null || focusPoint == null) {
//            return false;
//        }
//
//        // Bigger this value is, farther from choke will units stand
////        double unitShootRangeExtra = +0.3;
//
//        // Distance to the center of choke point.
//        double distToChoke = unit.distanceTo(focusPoint);
//
//        // Define distance which is considered "Close enough"
//        double acceptableDistance = getCloseEnoughDistanceToFocusPoint(unit)
//                + Select.ourCombatUnits().inRadius(3, unit).count() / 6.0;
//
//        return distToChoke < acceptableDistance;
////
////        // How far can the unit shoot
////        double unitShootRange =  unit.getWeaponRangeGround();
////
////        // Define max allowed distance from choke point to consider "still close"
////        double maxDistanceAllowed = unitShootRange + unitShootRangeExtra;
////
////        return distToChoke <= maxDistanceAllowed;
//    }
//
//    private static int getCloseEnoughDistanceToFocusPoint(AUnit unit) {
//        int base = 3;
//
//        if (unit.isTank()) {
//            return base + (AGame.isEnemyTerran() ? 0 : 2);
//        }
//        else {
//            return base;
//        }
//    }
//
//    private static boolean isCriticallyCloseToFocusPoint(AUnit unit, APosition focusPoint) {
//        if (unit == null || focusPoint == null) {
//            return false;
//        }
//
//        // Distance to the center of choke point.
//        double distToChoke = unit.distanceTo(focusPoint);
//
//        // Can't be closer than X from choke point
//        if (distToChoke > 0.1 && distToChoke <= getCriticallyCloseDistanceToFocusPoint(unit)) {
//            return true;
//        }
//
////        // Bigger this value is, farther from choke will units stand
////        double standFarther = 1;
////
////        // How far can the unit shoot (in build tiles)
////        double unitShootRange = unit.getWeaponRangeGround();
////
////        // Define max distance
////        double maxDistance = unitShootRange + standFarther;
////
////        return distToChoke <= maxDistance;
//
//        return false;
//    }
//
//    private static double getCriticallyCloseDistanceToFocusPoint(AUnit unit) {
//        double base = 1.2;
//
//        if (unit.isTank()) {
//            return base + (AGame.isEnemyTerran() ? 0 : 2);
//        }
//        else {
//            return base;
//        }
//    }

}