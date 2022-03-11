package atlantis.combat.missions.other;

import atlantis.combat.missions.defend.MissionDefend;
import atlantis.combat.missions.defend.MoveToDefendFocusPoint;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.position.Positions;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

import java.util.List;

/**
 * Make Zealots stand in one line and defend narrow choke point like in 300.
 * That's why this is Sparta!!!
 */
public class Sparta extends MissionDefend {

    public static final double HOLD_DIST_FOR_MELEE = 0.6;
    public static final double HOLD_DIST_FOR_MELEE_MARGIN = 0.08;

    public boolean update(AUnit unit) {
        focusPoint = focusPoint();
        if (!focusPoint.isAroundChoke()) {
            return super.update(unit);
        }

        this.unit = unit;
        unitDistToFocus = unit.distTo(focusPoint);

        // Don't reposition if enemies Near
//        if (unit.enemiesNear().combatUnits().inRadius(6.2, unit).atLeast(2)) {
//            return false;
//        }

        return MoveToDefendFocusPoint.wrongSideOfFocus() || holdOnPerpendicularLine() || advance();
    }

    private boolean advance() {
        if (unitDistToFocus > 0) {
            APosition point = definePointForSpartan();

            if (point == null) {
                System.err.println("Empty Spartan point for " + unit);
                return false;
            }

            String dist = A.dist(unitDistToFocus);
            return unit.move(
                point,
                Actions.MOVE_FOCUS,
                "Spartan" + dist,
                true
            );
        }

        return false;
    }

    private APosition definePointForSpartan() {

        // Ranged
        if (unit.isRanged()) {
            return unit.translatePercentTowards(focusPoint, 30);
        }

        // Melee
        List<APosition> points = focusPoint.choke().perpendicularLine();
        System.out.println(points.size() + " ----------------------");
        for (HasPosition point : points) {
            System.out.println(point.x() + ", " + point.y());
        }
        return points.get(points.size() / 2);
    }

//    private double optimalDist() {
//        return unit.isMelee() ? 0.5 : 2.5;
//    }

    public boolean allowsToAttackEnemyUnit(AUnit unit, AUnit enemy) {
        focusPoint = focusPoint();
        if (!focusPoint.isAroundChoke()) {
            return super.allowsToAttackEnemyUnit(unit, enemy);
        }

        // =========================================================

        main = Select.main();
        focusPointDistToBase = focusPoint.distTo(main);
        unitDistToEnemy = unit.distTo(enemy);
        unitDistToBase = unit.groundDist(main);
        enemyDistToBase = enemy.groundDist(main);
        enemyDistToFocus = enemy.groundDist(focusPoint);

        // =========================================================

//        if (notAllowedToAttackTooFar(unit, enemy)) {
//            return false;
//        }

//        if (unit.isMelee() && enemyDistToBase > unitDistToBase) {
//            return false;
//        }

        return unit.distTo(enemy) <= (unit.isMelee() ? 1.3 : unit.weaponRangeAgainst(enemy));

//        return unit.isMelee() ? forMelee(unit, enemy);

//        if (unit.isMelee() && enemy.isMelee()) {
//            return unit.distTo(enemy) <= 1.1;
//        }

//        if (enemyDistToBase < (focusPointDistToBase - 0.5)) {
//            return true;
//        }
//        else if (enemyDistToBase > (focusPointDistToBase + 0.5)) {
//            return false;
//        }
    }

    private boolean holdOnPerpendicularLine() {
//        if (!unit.isMelee()) {
//            return false;
//        }

//        if (unit.enemiesNear().inRadius(1.1, unit).isNotEmpty()) {
//            return false;
//        }

        if (!focusPoint.isAroundChoke()) {
            return false;
        }

        if (focusPoint.choke().perpendicularLine().isEmpty()) {
            System.err.println("Undefined focusPoint choke perpendicularLine");
            return false;
        }

        return unit.isMelee() ? holdForMelee() : holdForRanged();
    }

    private boolean holdForMelee() {
        HasPosition perpPoint = (new Positions(focusPoint.choke().perpendicularLine())).nearestTo(unit);

        if (perpPoint == null) {
            return false;
        }

        double dist = perpPoint.distTo(unit);

        if (Math.abs(dist - HOLD_DIST_FOR_MELEE) <= HOLD_DIST_FOR_MELEE_MARGIN) {
            unit.holdPosition("Sparta", false);
            return true;
        }

        return false;
    }

    private boolean holdForRanged() {
        if (unit.distTo(focusPoint) <= 2.5) {
            unit.holdPosition("Sparta", false);
            return true;
        }

        return false;
    }

//    protected boolean notAllowedToAttackTooFar(AUnit unit, AUnit enemy) {
//        if (
//            unit.isZealot()
//                && enemy.isMelee()
//                && (unitDistToEnemy >= 1.09 && enemyDistToBase >= focusPointDistToBase)
//                && focusPoint != null
//                && focusPoint.isAroundChoke()
//        ) {
//            return true;
//        }
//
//        if (
//            unit.isMelee()
//                && enemyDistToFocus >= 1.2
//                && enemyDistToBase > focusPointDistToBase
//        ) {
//            return true;
//        }
//
//        else if (
//            unit.isRanged()
//                && (enemyDistToFocus <= 2.1 || unitDistToEnemy <= 5)
////                && enemyDistToBase > focusPointDistToBase
//        ) {
//            return true;
//        }
//
//        return false;
//    }

}
