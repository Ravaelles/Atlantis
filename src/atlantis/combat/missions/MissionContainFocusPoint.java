package atlantis.combat.missions;

import atlantis.CameraManager;
import atlantis.enemy.AEnemyUnits;
import atlantis.information.AFoggedUnit;
import atlantis.map.*;
import atlantis.position.APosition;
import atlantis.util.Cache;
import atlantis.util.We;

public class MissionContainFocusPoint extends MissionFocusPoint {

    private Cache<APosition> cache = new Cache<>();
    private APosition containEnemyAtPoint = null;

    @Override
    public APosition focusPoint() {
        return cache.get(
                "focusPoint",
                100,
                () -> {
                    if (containEnemyAtPoint != null) {
                        return containEnemyAtPoint;
                    }

                    if (We.terran()) {
                        AFoggedUnit enemyBuilding = AEnemyUnits.nearestEnemyBuilding();
                        if (enemyBuilding != null) {
                            return containEnemyAtPoint = enemyBuilding.position();
                        }
                    }

                    AChoke naturalChoke = Chokes.enemyNaturalChoke();
                    if (naturalChoke != null && naturalChoke.getWidth() <= 4) {
                        containEnemyAtPoint = naturalChoke.position();
                    }

                    AChoke mainChoke = Chokes.enemyMainChoke();
                    if (mainChoke != null && mainChoke.getWidth() <= 4) {
                        containEnemyAtPoint = mainChoke.position();
                    }

//        APosition enemyBase = AEnemyUnits.enemyBase();
//        if (enemyBase != null) {
//            return containEnemyAtPoint = containPointIfEnemyBaseIsKnown(enemyBase);
//        }

                    AFoggedUnit enemyBuilding = AEnemyUnits.nearestEnemyBuilding();
                    if (enemyBuilding != null) {
                        return containEnemyAtPoint = enemyBuilding.position();
                    }
////
//        AUnit nearestEnemy = Select.enemy().nearestTo(Select.our().first());
//        if (nearestEnemy != null) {
//            return containEnemyAtPoint = nearestEnemy.position();
//        }

                    if (mainChoke != null) {
                        containEnemyAtPoint = mainChoke.position();
                    }
                    return containEnemyAtPoint;
                }
        );
    }

    // =========================================================

    private APosition containPointIfEnemyBaseIsKnown(APosition enemyBase) {
        AChoke chokepoint = Chokes.natural(enemyBase);
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
