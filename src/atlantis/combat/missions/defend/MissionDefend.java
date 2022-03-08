package atlantis.combat.missions.defend;

import atlantis.combat.missions.AFocusPoint;
import atlantis.combat.missions.Mission;
import atlantis.combat.missions.ProtossMissionAdjustments;
import atlantis.game.AGame;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.We;

public class MissionDefend extends Mission {

    private AFocusPoint focusPoint;
    private double focusPointDistToBase;
    private double unitDistToEnemy;
    private double unitDistToBase;
    private double enemyDistToBase;
    private double enemyDistToFocus;

    public MissionDefend() {
        super("Defend");
        focusPointManager = new MissionDefendFocusPoint();
    }

    @Override
    public boolean update(AUnit unit) {
        if (AGame.isUms()) {
            return false;
        }

        // =========================================================

        AFocusPoint focusPoint = focusPoint();
        if (focusPoint == null) {
            if (!Have.base()) {
                return false;
            }

            System.err.println("Couldn't define choke point.");
            throw new RuntimeException("Couldn't define choke point.");
        }

        // Don't reposition if enemies Near
        if (unit.enemiesNear().inRadius(7, unit).atLeast(3)) {
            return false;
        }

        return MoveToDefendFocusPoint.move(unit, focusPoint);
    }

    // =========================================================

    public boolean allowsToAttackEnemyUnit(AUnit unit, AUnit enemy) {
        focusPoint = focusPoint();
        AUnit base = Select.main();
        if (focusPoint == null || base == null) {
            return true;
        }

        // =========================================================

        focusPointDistToBase = focusPoint.distTo(base);
        unitDistToEnemy = unit.distTo(enemy);
        unitDistToBase = unit.groundDist(base);
        enemyDistToBase = enemy.groundDist(base);
        enemyDistToFocus = enemy.groundDist(focusPoint);

        if (notAllowedToAttackTooFar(unit, enemy)) {
            return false;
        }

        if (
//                (unit.isMelee() && unit.hasWeaponRangeToAttack(enemy, 0.1))
                (unit.isMelee() && unitDistToEnemy <= 1.01)
                || (unit.isRanged() && unit.hasWeaponRangeToAttack(enemy, 0.6))
        ) {
            if (unit.cooldownRemaining() == 0 || unit.lastAttackFrameMoreThanAgo(40)) {
                return true;
            }
        }

        if (focusPoint.isAroundChoke()) {
            if (enemyDistToBase < (focusPointDistToBase - 0.5)) {
                return true;
            }
            else if (enemyDistToBase > (focusPointDistToBase + 0.5)) {
                return false;
            }
        }

//        if (unit.isMelee() && enemy.isMelee() && "300".equals(unit.tooltip())) {
//            return unit.distTo(enemy) <= 1;
////            return false; // 300 mode - stand in line, default to SC auto attacks
//        }

        if (unit.isMelee() && enemyDistToBase > unitDistToBase) {
            return false;
        }

        // Zealots vs Zealot fix
        if (ProtossMissionAdjustments.allowsToAttackEnemyUnits(unit, enemy)) {
            return true;
        }

        if (Have.main()) {
            if (Select.enemy().inRadius(18, Select.main()).atLeast(1)) {
                return true;
            }

            if (Select.enemy().inRadius(18, Select.naturalOrMain()).atLeast(1)) {
                return true;
            }
        }

        if (focusPointDistToBase < enemyDistToBase || enemyDistToBase < unitDistToBase) {
            return true;
        }

        return false;
    }

    private boolean notAllowedToAttackTooFar(AUnit unit, AUnit enemy) {
        if (enemy.isMelee() && unit.isMelee() && unitDistToEnemy >= 1.01) {
            return false;
        }

        if (
            unit.isMelee()
                && enemyDistToFocus >= 1
                && enemyDistToBase > focusPointDistToBase
        ) {
            return true;
        }

        else if (
            unit.isRanged()
                && enemyDistToFocus >= 1
                && enemyDistToBase > focusPointDistToBase
        ) {
            return true;
        }

        return false;
    }

    @Override
    public boolean forcesUnitToFight(AUnit unit, Units enemies) {
        if (We.terran()) {
            return false;
        }

        if (
            (unit.isMelee() || unit.hpMoreThan(40))
            && unit.friendsNear().combatBuildings(false).inRadius(5, unit).notEmpty()
        ) {
            unit.addLog("ProtectBuilding");
            return true;
        }

        if (unit.hpLessThan(36) && unit.friendsNearCount() <= 2) {
            return false;
        }

        if (unit.isDragoon() && enemies.onlyMelee() && unit.hp() >= 30 && unit.lastAttackFrameMoreThanAgo(35)) {
            return true;
        }

        if (unit.isRanged() && (unit.isHealthy() || unit.shieldDamageAtMost(10))) {
            return true;
        }

//        if (unit.isMelee() && unit.friendsNear().inRadius(1.3, unit).atLeast(3)) {
//            return true;
//        }

        return false;
//        return enemies.onlyMelee() && unit.hp() >= 18;
    }
}
