package atlantis.combat.missions;

import atlantis.debug.APainter;
import atlantis.enemy.AEnemyUnits;
import atlantis.map.ABaseLocation;
import atlantis.map.AChokepoint;
import atlantis.map.AMap;
import atlantis.position.APosition;
import bwapi.Color;

public class MissionContainFocusPointManager extends MissionFocusPointManager {

//    private AChokepoint enemyBaseChokepoint = null;
    private APosition enemyNatural = null;

    @Override
    public APosition focusPoint() {
        if (enemyNatural != null) {
            APainter.paintCircle(enemyNatural, 10, Color.Teal);
            return enemyNatural;
        }

        APosition enemyBase = AEnemyUnits.getEnemyBase();
        if (enemyBase == null) {
            System.out.println("No base");
            return null;
        }

        return enemyNatural(enemyBase);
    }

//    public AChokepoint getChokepoint() {
////        APosition attackFocusPoint = focusPoint();
////        if (attackFocusPoint == null) {
////            return null;
////        }
////
////        return AMap.getNearestChokepoint(attackFocusPoint);
//
//        System.out.println("No choke");
//        if (enemyBaseChokepoint != null) {
//            APainter.paintCircle(enemyBaseChokepoint.getPosition(), 5, Color.Teal);
//            APainter.paintCircle(enemyBaseChokepoint.getPosition(), 5, Color.Yellow);
//            return enemyBaseChokepoint;
//        }
//
//        APosition enemyBase = AEnemyUnits.getEnemyBase();
//        if (enemyBase != null) {
//            enemyBaseChokepoint = AMap.getRegion(enemyBase).getChokepoints().get(0);
//            enemyNatural = enemyNatural(enemyBase);
//            return enemyBaseChokepoint;
//        }
//
//        return null;
//    }

    private APosition enemyNatural(APosition enemyBase) {
        ABaseLocation baseLocation = AMap.getNaturalBaseLocation(enemyBase);

        System.out.println("enemyNatural = " + baseLocation);
        if (baseLocation != null) {
            this.enemyNatural = baseLocation.getPosition();
            return this.enemyNatural;
        }

        return null;
    }

    private APosition couldNotDefineFocusPoint() {
        return focusPoint();
    }

}
