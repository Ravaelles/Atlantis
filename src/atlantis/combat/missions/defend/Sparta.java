package atlantis.combat.missions.defend;

import atlantis.combat.missions.defend.MissionDefend;
import atlantis.combat.missions.defend.MissionDefendFocusPoint;
import atlantis.combat.missions.defend.MoveToDefendFocusPoint;
import atlantis.combat.retreating.RetreatManager;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.position.Positions;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import atlantis.util.We;

import java.util.List;

/**
 * Make Zealots stand in one line and defend narrow choke point like in 300.
 * That's why this is Sparta!!!
 */
public class Sparta extends MissionDefend {

    //    public static final double HOLD_DIST_FOR_MELEE = 0.6;
    private static final double MAX_MELEE_DIST_TO_ATTACK = 1.1;

    // =========================================================

    public Sparta() {
        super();
        setName("Sparta");
        focusPointManager = new MissionDefendFocusPoint();
    }

    // =========================================================

    @Override
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

    @Override
    public double optimalDist(AUnit unit) {
        if (We.zerg()) {
            return 3.5 + (unit.isRanged() ? 1 : 0) + unit.friendsInRadius(3).count() / 6.0;
//            return 1.2 + (unit.isRanged() ? 1 : 0) + unit.friendsInRadius(3).count() / 6.0;
        }

        if (We.protoss()) {
            double base = Enemy.terran() ? 4 : 0.08;
            return base + (unit.isRanged() ? 1 : 0);
        }

        return 2.0 + (unit.isRanged() ? 1 : 0) + unit.friendsInRadius(3).count();
    }

    // =========================================================

    private boolean advance() {
        if (unitToFocus > 0 && (unit.isStopped() || A.everyNthGameFrame(17))) {
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

    // =========================================================

    @Override
    public boolean allowsToRetreat(AUnit unit) {
        if (unit.isRanged()) {
            return unit.hp() <= 20;
        }

//        return false;

        if (unit.distToFocusPoint() <= 3) {
            return false;
        }
//
        return
//            AGame.timeSeconds() > 300
//                || unit.hp() > (unit.meleeEnemiesNearCount(2) >= 2 ? 33 : 17);
            (unit.hp() <= 17 && unit.friendsInRadiusCount(3) <= 2)
                || (unit.hp() <= 17 && unit.friendsInRadiusCount(1) >= 4);
    }

    @Override
    public boolean allowsToAttackEnemyUnit(AUnit unit, AUnit enemy) {
        focusPoint = focusPoint();
        if (!focusPoint.isAroundChoke()) {
            unit.addLog("Sparta:---");
            return super.allowsToAttackEnemyUnit(unit, enemy);
        }

        main = Select.main();
        focusPointDistToBase = focusPoint.distTo(main);
        unitToEnemy = unit.distTo(enemy);
        unitToBase = unit.groundDist(main);
        enemyDistToBase = enemy.groundDist(main);
        enemyDistToFocus = enemy.groundDist(focusPoint);

        // =========================================================

        if (unit.isRanged()) {
            if (unit.weaponRangeAgainst(enemy) >= 6) {
                return true;
            }

            return enemyDistToBase - 2.1 <= focusPointDistToBase;
        }

        if (enemy.isRanged()) {
            return enemyDistToFocus <= 2 || (enemyDistToBase + 3 < unitToBase) || unitToEnemy <= 3;
        }

        if (enemy.isWorker() && unitToEnemy <= 1.2 && enemyDistToFocus <= 1) {
            return true;
        }

        if (unitToBase >= 40) {
            return false;
        }

        // If unit outside our region...
        if (enemyDistToBase + 3 <= focusPointDistToBase) {
            if (unit.isMelee()) {
                unit.addLog("Sparta:A");
//               System.out.println("enemyDistToBase = " + enemyDistToBase);
//               System.out.println("focusPointDistToBase = " + focusPointDistToBase);
//               System.out.println("unitToEnemy = " + unitToEnemy);
                return unitToEnemy <= 1;
            }
        }

        if ((enemy.isZealot() || enemy.isZergling()) && unit.isZealot()) {
            boolean canAttack = unitToEnemy <= MAX_MELEE_DIST_TO_ATTACK
                || (isEnemyBehindLineOfDefence() && enemy.isZergling())
                || (enemyDistToFocus <= 1.2 && unit.enemiesNear().count() == 0);

            if (canAttack) {
                unit.addLog("Sparta:C");
            }

            return canAttack;
        }

        if (!isEnemyBehindLineOfDefence() && unitToEnemy > MAX_MELEE_DIST_TO_ATTACK) {
            return false;
        }

        if (isEnemyBehindLineOfDefence() && unit.hp() >= 18 && !RetreatManager.shouldRetreat(unit)) {
            unit.addLog("Sparta:D");
            return true;
        }

        boolean canAttack =
            unit.distTo(enemy) <= (unit.isMelee() ? MAX_MELEE_DIST_TO_ATTACK : unit.weaponRangeAgainst(enemy));

        if (canAttack) {
            unit.addLog("Sparta:E");
        }

        return canAttack;

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
        boolean distanceGood = dist <= optimalDist(unit);

        if (distanceGood) {
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
            && unit.lastActionMoreThanAgo(6);
    }

    public static boolean canUseSpartaMission() {
        if (We.terran()) {
            return false;
        }

        if (We.zerg() && Enemy.protoss()) {
            return false;
        }

        return true;
    }

}
