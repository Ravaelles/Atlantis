package atlantis.combat.missions;

import atlantis.enemy.AEnemyUnits;
import atlantis.information.AFoggedUnit;
import atlantis.map.*;
import atlantis.position.APosition;
import atlantis.units.select.Select;
import atlantis.util.Cache;
import atlantis.util.We;

public class MissionContainFocusPoint extends MissionFocusPoint {

    private Cache<APosition> cache = new Cache<>();

    @Override
    public APosition focusPoint() {
        return cache.get(
                "focusPoint",
                100,
                () -> {
                    APosition basePoint = basePoint();
                    if (basePoint != null && basePoint.region() != null && basePoint.region().center() != null) {
                        basePoint = basePoint.translateTilesTowards(4, basePoint.region().center());
                    }
                    return basePoint;
                }
        );
    }

    private APosition basePoint() {
        if (We.terran()) {
            AFoggedUnit enemyBuilding = AEnemyUnits.nearestEnemyBuilding();
            if (enemyBuilding != null && enemyBuilding.position() != null) {
                return enemyBuilding.position();
            }
        }

        AChoke mainChoke = Chokes.enemyMainChoke();
        APosition enemyNatural = Bases.enemyNatural();
        if (enemyNatural != null) {
            if (mainChoke != null) {
                return enemyNatural.translatePercentTowards(mainChoke, 40);
            }
            return enemyNatural;
        }

        AChoke naturalChoke = Chokes.enemyNaturalChoke();
        if (naturalChoke != null && naturalChoke.width() <= 4) {
            return naturalChoke.position();
        }

//                    if (mainChoke != null && mainChoke.getWidth() <= 4) {
//                        return mainChoke.position();
//                    }

//                    AFoggedUnit enemyBuilding = AEnemyUnits.nearestEnemyBuilding();
//                    if (enemyBuilding != null) {
//                        return enemyBuilding.position();
//                    }
////
//        AUnit nearestEnemy = Select.enemy().nearestTo(Select.our().first());
//        if (nearestEnemy != null) {
//            return nearestEnemy.position();
//        }

        APosition enemyBase = AEnemyUnits.enemyBase();
        if (enemyBase != null && enemyBase.position() != null) {
            return containPointIfEnemyBaseIsKnown(enemyBase);
        }

//                    AChoke mainChoke = Chokes.enemyMainChoke();
//        if (mainChoke != null) {
//            return mainChoke.position();
//        }

        // Try to go to some starting location, hoping to find enemy there.
        if (Select.main() != null) {
            AChoke choke = Chokes.nearestChoke(
                    Bases.nearestUnexploredStartingLocation(Select.main().position())
            );
            return choke != null ? choke.center() : null;
        }

        return null;
    }

    // =========================================================

    private APosition containPointIfEnemyBaseIsKnown(APosition enemyBase) {
        AChoke chokepoint = Chokes.natural(enemyBase);
        if (chokepoint != null) {
//            CameraManager.centerCameraOn(chokepoint.getCenter());
            return chokepoint.center();
        }

        ABaseLocation natural = Bases.natural(enemyBase.position());
        if (natural != null) {
//            CameraManager.centerCameraOn(natural);
            return natural.position();
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
