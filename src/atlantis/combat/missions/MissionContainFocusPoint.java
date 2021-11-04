package atlantis.combat.missions;

import atlantis.CameraManager;
import atlantis.map.*;
import atlantis.position.APosition;

public class MissionContainFocusPoint extends MissionFocusPoint {

    private APosition containEnemyAtPoint = null;

    @Override
    public APosition focusPoint() {
        if (containEnemyAtPoint != null) {
            return containEnemyAtPoint;
        }

        AChoke naturalChoke = MapChokes.enemyNaturalChoke();
        if (naturalChoke != null) {
            containEnemyAtPoint = naturalChoke.position();
        }

        AChoke mainChoke = MapChokes.enemyMainChoke();
        if (mainChoke != null) {
            containEnemyAtPoint = mainChoke.position();
        }

//        APosition enemyBase = AEnemyUnits.enemyBase();
//        if (enemyBase != null) {
//            return containEnemyAtPoint = containPointIfEnemyBaseIsKnown(enemyBase);
//        }
//
//        AFoggedUnit enemyBuilding = AEnemyUnits.nearestEnemyBuilding();
//        if (enemyBuilding != null) {
//            return containEnemyAtPoint = enemyBuilding.position();
//        }
//
//        AUnit nearestEnemy = Select.enemy().nearestTo(Select.our().first());
//        if (nearestEnemy != null) {
//            return containEnemyAtPoint = nearestEnemy.position();
//        }

        return containEnemyAtPoint;
    }

    // =========================================================

    private APosition containPointIfEnemyBaseIsKnown(APosition enemyBase) {
        AChoke chokepoint = MapChokes.chokeForNatural(enemyBase);
        if (chokepoint != null) {
            CameraManager.centerCameraOn(chokepoint.getCenter());
            return containEnemyAtPoint = chokepoint.getCenter();
        }

        ABaseLocation natural = BaseLocations.natural(enemyBase.position());
        if (natural != null) {
            CameraManager.centerCameraOn(natural);
            return containEnemyAtPoint = natural.position();
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
////        AChoke choke = AMap.getChokepointForNatural(mainBase.getPosition());
////        return choke == null ? null : choke.getCenter();
//    }

}
