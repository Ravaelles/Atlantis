package atlantis.combat.missions;

import atlantis.ACamera;
import atlantis.enemy.AEnemyUnits;
import atlantis.information.AFoggedUnit;
import atlantis.map.ABaseLocation;
import atlantis.map.AChokepoint;
import atlantis.map.AMap;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.Select;

public class MissionContainFocusPoint extends MissionFocusPoint {

    private APosition containEnemyAtPoint = null;

    @Override
    public APosition focusPoint() {
        if (containEnemyAtPoint != null) {
            return containEnemyAtPoint;
        }

        APosition enemyBase = AEnemyUnits.enemyBase();
        if (enemyBase != null) {
            return containEnemyAtPoint = containPointIfEnemyBaseIsKnown(containEnemyAtPoint);
        }

        AFoggedUnit enemyBuilding = AEnemyUnits.nearestEnemyBuilding();
        if (enemyBuilding != null) {
            return containEnemyAtPoint = enemyBuilding.getPosition();
        }

        AUnit nearestEnemy = Select.enemy().nearestTo(Select.our().first());
        if (nearestEnemy != null) {
            return containEnemyAtPoint = nearestEnemy.getPosition();
        }

        return null;
    }

    // =========================================================

    private APosition containPointIfEnemyBaseIsKnown(APosition enemyBase) {
        AChokepoint chokepoint = AMap.getChokepointForNaturalBase(enemyBase);
        if (chokepoint != null) {
            ACamera.centerCameraOn(chokepoint.getCenter());
            return containEnemyAtPoint = chokepoint.getCenter();
        }

        ABaseLocation natural = AMap.getNaturalBaseLocation(enemyBase.getPosition());
        if (natural != null) {
            ACamera.centerCameraOn(natural);
            return containEnemyAtPoint = natural.getPosition();
        }

        System.err.println("Shouldnt be here mate?");
        return null;
    }

//    private APosition containPointIfEnemyBaseNotKnown() {
//        AUnit nearestEnemy = Select.enemy().nearestTo(Select.our().first());
//        if (nearestEnemy != null) {
//            return nearestEnemy.getPosition();
//        }
//
//        return null;
//
////        AUnit mainBase = Select.mainBase();
////        if (mainBase == null) {
////            return null;
////        }
////
////        AChokepoint choke = AMap.getChokepointForNaturalBase(mainBase.getPosition());
////        return choke == null ? null : choke.getCenter();
//    }

}
