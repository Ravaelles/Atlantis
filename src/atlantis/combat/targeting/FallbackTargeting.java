package atlantis.combat.targeting;

import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class FallbackTargeting {
    public static AUnit fallbackTarget(AUnit unit, double maxDistFromEnemy) {
        if (unit.isMelee()) return forMelee(unit);

        return forRanged(unit);

//        // Workers
//        AUnit worker = Select.enemy().workers().canBeAttackedBy(unit, 0).nearestTo(unit);
//        if (worker != null && worker.isAlive()) {
//            return worker;
//        }
//
//        // Combat buildings - close
//        AUnit combatBuildingClose = Select.enemy()
//            .combatBuildings(false)
//            .canBeAttackedBy(unit, unit.isMelee() ? 4 : 8).nearestTo(unit);
//        if (combatBuildingClose != null && combatBuildingClose.isAlive()) {
//            return combatBuildingClose;
//        }
//
//        // Combat units
//        AUnit combat = Select.enemy().combatUnits()
//            .nonBuildings().canBeAttackedBy(unit, maxDistFromEnemy).nearestTo(unit);
//        if (combat != null && combat.isAlive()) {
//            return combat;
//        }
//
//        // Combat buildings - far
//        AUnit combatBuilding = Select.enemy().combatBuildings(false).canBeAttackedBy(unit, maxDistFromEnemy).nearestTo(unit);
//        if (combatBuilding != null && combatBuilding.isAlive()) {
//            return combatBuilding;
//        }
//
//        // Normal units
//        AUnit regular = Select.enemy().realUnits().canBeAttackedBy(unit, maxDistFromEnemy).nearestTo(unit);
//        if (regular != null && regular.isAlive()) {
//            return regular;
//        }
//
//        // Buildings
//        AUnit building = Select.enemy().buildings().canBeAttackedBy(unit, maxDistFromEnemy).nearestTo(unit);
//        if (building != null && building.isAlive()) {
//            return building;
//        }
//
//        return null;
    }

    private static AUnit forRanged(AUnit unit) {
        AUnit inRange;

        inRange = unit.enemiesNear().canBeAttackedBy(unit, 0).wounded().mostWounded();

        if (inRange != null) return inRange;

        inRange = unit.enemiesNear().canBeAttackedBy(unit, 0).nearestTo(unit);

        if (inRange != null) return inRange;

        return unit.squadCenterEnemiesNear().canBeAttackedBy(unit, 10).nearestTo(unit);
    }

    private static AUnit forMelee(AUnit unit) {
        AUnit inRange = unit.enemiesNear().canBeAttackedBy(unit, 0).mostWounded();

        if (inRange != null) return inRange;

        return unit.enemiesNear().canBeAttackedBy(unit, 10).nearestTo(unit);
    }
}
