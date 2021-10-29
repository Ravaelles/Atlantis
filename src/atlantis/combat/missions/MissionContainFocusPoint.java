package atlantis.combat.missions;

import atlantis.CameraManager;
import atlantis.enemy.AEnemyUnits;
import atlantis.information.AFoggedUnit;
import atlantis.map.ABaseLocation;
import atlantis.map.AChoke;
import atlantis.map.AMap;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class MissionContainFocusPoint extends MissionFocusPoint {

    private APosition containEnemyAtPoint = null;

    @Override
    public APosition focusPoint() {
        if (containEnemyAtPoint != null) {
            return containEnemyAtPoint;
        }

        APosition enemyBase = AEnemyUnits.enemyBase();
        if (enemyBase != null) {
            return containEnemyAtPoint = containPointIfEnemyBaseIsKnown(enemyBase);
        }

        AFoggedUnit enemyBuilding = AEnemyUnits.nearestEnemyBuilding();
        if (enemyBuilding != null) {
            return containEnemyAtPoint = enemyBuilding.position();
        }

        AUnit nearestEnemy = Select.enemy().nearestTo(Select.our().first());
        if (nearestEnemy != null) {
            return containEnemyAtPoint = nearestEnemy.position();
        }

        return null;
    }

    // =========================================================

    private APosition containPointIfEnemyBaseIsKnown(APosition enemyBase) {
        AChoke chokepoint = AMap.getChokeForNaturalBase(enemyBase);
        if (chokepoint != null) {
            CameraManager.centerCameraOn(chokepoint.getCenter());
            return containEnemyAtPoint = chokepoint.getCenter();
        }

        ABaseLocation natural = AMap.naturalBase(enemyBase.position());
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
////        AChoke choke = AMap.getChokepointForNaturalBase(mainBase.getPosition());
////        return choke == null ? null : choke.getCenter();
//    }

}
