package atlantis.combat.missions.other;

import atlantis.combat.missions.defend.MissionDefend;
import atlantis.combat.missions.defend.MissionDefendFocusPoint;
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

//    public static final double HOLD_DIST_FOR_MELEE = 0.6;
    public static final double HOLD_DIST_FOR_MELEE = 0;
    public static final double HOLD_DIST_FOR_MELEE_MARGIN = 0.08;
    private static final double MAX_MELEE_DIST_TO_ATTACK = 1.1;

    // =========================================================

    public Sparta() {
        super();
        setName("Sparta");
        focusPointManager = new MissionDefendFocusPoint();
    }

    // =========================================================

    public boolean update(AUnit unit) {
        focusPoint = focusPoint();
        if (!focusPoint.isAroundChoke() || unit.hasNoWeaponAtAll()) {
            return super.update(unit);
        }

        this.unit = unit;
        unitToFocus = unit.distTo(focusPoint);

        // Don't reposition if enemies Near
//        if (unit.enemiesNear().combatUnits().inRadius(6.2, unit).atLeast(2)) {
//            return false;
//        }

        return (new MoveToDefendFocusPoint()).handleWrongSideOfFocus(unit, focusPoint)
            || holdOnPerpendicularLine()
            || advance();
    }

    // =========================================================

    private boolean advance() {
        if (unitToFocus > 0 && A.everyNthGameFrame(9)) {
            HasPosition point = definePointForSpartan();

            if (point == null) {
//                System.err.println("Empty Spartan point for " + unit);
                return false;
            }

            String dist = A.dist(unitToFocus);

            return unit.move(
                point,
                Actions.MOVE_FOCUS,
                "Spartan" + dist,
                true
            );
        }

        return false;
    }

    private HasPosition definePointForSpartan() {

        // Ranged
        if (unit.isRanged()) {
            return focusPoint.translatePercentTowards(unit, 30)
                .makeFreeOfOurUnits(2, 0.12, unit);
        }

        // Melee
        Positions points = new Positions(focusPoint.choke().perpendicularLine());
        HasPosition spartanPoint = HasPosition.nearestPositionFreeFromUnits(points, unit);

//        if (spartanPoint != null) {
//            System.out.println("Spartan point " + spartanPoint.toStringPixels() + " for " + unit);
//        }

        return spartanPoint != null ? spartanPoint : focusPoint;
    }

    public boolean allowsToAttackEnemyUnit(AUnit unit, AUnit enemy) {
        if (!unit.mission().focusPoint().isAroundChoke()) {
            System.err.println("Invalid Sparta not around choke");
            return super.allowsToAttackEnemyUnit(unit, enemy);
        }

        if (unit.isRanged()) {
            return enemyDistToBase - 2.1 <= focusPointDistToBase;
        }

        if (enemy.isRanged()) {
            return enemyDistToFocus <= 2.1 || enemyDistToBase < unitToBase;
        }

        if (enemy.isWorker() && unitToEnemy <= 3 && enemyDistToFocus <= 3 && A.seconds() <= 250) {
            return true;
        }

        if (unitToBase >= 40) {
            return false;
        }

        // =========================================================

        focusPoint = focusPoint();
        if (!focusPoint.isAroundChoke()) {
            return super.allowsToAttackEnemyUnit(unit, enemy);
        }

        main = Select.main();
        focusPointDistToBase = focusPoint.distTo(main);
        unitToEnemy = unit.distTo(enemy);
        unitToBase = unit.groundDist(main);
        enemyDistToBase = enemy.groundDist(main);
        enemyDistToFocus = enemy.groundDist(focusPoint);

        // =========================================================

        if ((enemy.isZealot() || enemy.isZergling()) && unit.isZealot()) {
            return unitToEnemy <= MAX_MELEE_DIST_TO_ATTACK
                || (isEnemyBehindLineOfDefence() && enemy.isZergling())
                || (enemyDistToFocus <= 1.2 && unit.enemiesNear().count() == 0);
        }

        if (!isEnemyBehindLineOfDefence() && unitToEnemy > MAX_MELEE_DIST_TO_ATTACK) {
            return false;
        }

        if (isEnemyBehindLineOfDefence()) {
            return true;
        }

        return unit.distTo(enemy) <= (unit.isMelee() ? MAX_MELEE_DIST_TO_ATTACK : unit.weaponRangeAgainst(enemy));

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

    private boolean isEnemyBehindLineOfDefence() {
        return unitToBase + 5 > enemyDistToBase;
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

        if (!(new MoveToDefendFocusPoint()).isOnValidSideOfChoke(unit, focusPoint)) {
            return false;
        }

        if (focusPoint.choke().perpendicularLine().isEmpty()) {
            System.err.println("Undefined focusPoint choke perpendicularLine");
            return false;
        }

        return unit.isMelee() ? holdForMelee() : holdForRanged();
    }

    private boolean holdForMelee() {
        HasPosition spartanPoint = definePointForSpartan();

        if (spartanPoint == null) {
            return false;
        }

        double dist = spartanPoint.distTo(unit);

        if (Math.abs(dist - HOLD_DIST_FOR_MELEE) <= HOLD_DIST_FOR_MELEE_MARGIN) {
            if (shouldHold(unit)) {
                unit.holdPosition("Sparta", false);
            }
            return true;
        }

        return false;
    }

    private boolean holdForRanged() {
        double dist = unit.distTo(focusPoint);

        if (1.0 <= dist && dist <= 2.0) {
            if (shouldHold(unit)) {
                unit.holdPosition("Sparta", false);
            }
            return true;
        }

        return false;
    }

    private boolean shouldHold(AUnit unit) {
        return !unit.isAttacking()
            && !unit.isHoldingPosition()
            && !"HelpWithdraw".equals(unit.tooltip())
            && unit.lastActionMoreThanAgo(3);
    }

}
