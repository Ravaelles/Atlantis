package atlantis.combat.missions;

import atlantis.AViewport;
import atlantis.debug.APainter;
import atlantis.enemy.AEnemyUnits;
import atlantis.map.ABaseLocation;
import atlantis.map.AChokepoint;
import atlantis.map.AMap;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.Select;
import bwapi.Color;

public class MissionContainFocusPointManager extends MissionFocusPointManager {

    private APosition containEnemyAtPoint = null;
//    private APosition enemyNatural = null;

    @Override
    public APosition focusPoint() {
        if (containEnemyAtPoint != null) {
            APainter.paintCircle(containEnemyAtPoint, 12, Color.Teal);
            APainter.paintCircle(containEnemyAtPoint, 10, Color.Teal);
            APainter.paintCircle(containEnemyAtPoint, 8, Color.Teal);
            APainter.paintCircle(containEnemyAtPoint, 6, Color.Teal);
            APainter.paintCircle(containEnemyAtPoint, 4, Color.Teal);
            return containEnemyAtPoint;
        }

        APosition enemyBase = AEnemyUnits.getEnemyBase();
        if (enemyBase == null) {
            return containPointIfEnemyBaseNotKnown();
        }

        return containEnemyAtPoint = containPointIfEnemyBaseIsKnown(enemyBase);
    }

    // =========================================================

    private APosition containPointIfEnemyBaseIsKnown(APosition enemyBase) {
        AChokepoint chokepoint = AMap.getChokepointForNaturalBase(enemyBase.getPosition());
        if (chokepoint != null) {
            AViewport.centerScreenOn(chokepoint.getCenter());
            System.out.println("Our base " + Select.mainBase());
            System.out.println("Located chokepoint " + chokepoint.getCenter());
            return containEnemyAtPoint = chokepoint.getCenter();
        }

        APosition natural = AMap.getNaturalBaseLocation(enemyBase.getPosition()).getPosition();
        if (natural == null) {
            AViewport.centerScreenOn(natural);
        }

        System.out.println("Our base " + Select.mainBase());
        System.out.println("Located natural " + natural);
        return containEnemyAtPoint = natural;
    }

    private APosition containPointIfEnemyBaseNotKnown() {
        AUnit mainBase = Select.mainBase();
        if (mainBase == null) {
            return null;
        }

        AChokepoint choke = AMap.getChokepointForNaturalBase(mainBase.getPosition());
        return choke == null ? null : choke.getCenter();
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

//    private APosition enemyNatural(APosition enemyBase) {
//        ABaseLocation baseLocation = AMap.getNaturalBaseLocation(enemyBase);
//
//        System.out.println("enemyNatural = " + baseLocation);
//        if (baseLocation != null) {
//            this.enemyNatural = baseLocation.getPosition();
//            return this.enemyNatural;
//        }
//
//        return null;
//    }

//    private APosition couldNotDefineFocusPoint() {
//        return focusPoint();
//    }

}
