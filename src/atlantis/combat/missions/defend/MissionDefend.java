package atlantis.combat.missions.defend;

import atlantis.combat.missions.focus.AFocusPoint;
import atlantis.combat.missions.Mission;
import atlantis.combat.missions.ProtossMissionAdjustments;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Units;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import atlantis.util.We;

public class MissionDefend extends Mission {

    protected AUnit unit;
    protected AUnit main;
    protected AFocusPoint focusPoint;
    protected double focusPointToBase;
    protected double unitToEnemy;
    protected double unitToFocus;
    protected double unitToBase;
    protected double enemyToBase;
    protected double enemyToFocus;

    public MissionDefend() {
        super("Defend");
        focusPointManager = new MissionDefendFocusPoint();
    }

    @Override
    public boolean update(AUnit unit) {
        if (AGame.isUms()) {
            return false;
        }

        this.unit = unit;
        focusPoint = focusPoint();
        if (focusPoint == null) {
            if (!Have.base()) {
                return false;
            }

            System.err.println("Couldn't define choke point.");
            throw new RuntimeException("Couldn't define choke point.");
        }

        // =========================================================

        // Don't reposition if enemies Near
        if (unit.enemiesNear().combatUnits().inRadius(6.2, unit).atLeast(2)) {
            return false;
        }

        return (new MoveToDefendFocusPoint()).move(unit, focusPoint);
    }

    // =========================================================

    public boolean allowsToAttackEnemyUnit(AUnit unit, AUnit enemy) {
        if (focusPoint == null || main == null) {
            return true;
        }

        if (unit.isRanged()) {
            if (
                unit.hp() >= 20 && unit.isInWeaponRangeByGame(enemy)
                    && (!enemy.isRanged() || enemy.distToMoreThan(enemy, 2.8))
            ) {
                return true;
            }

            if (unit.distTo(enemy) <= 6) {
                return true;
            }

            if (unit.enemyWeaponRange(enemy) >= 6) {
                return true;
            }
        }

        // =========================================================

        if (unit.isZergling()) {
            if (Enemy.protoss() && unit.hp() <= 18) {
                return false;
            }

            if (A.seconds() <= 500 && Count.sunkens() > 0) {
                Selection sunkens = Select.ourOfType(AUnitType.Zerg_Sunken_Colony);

                if (
                    sunkens.inRadius(10, unit).notEmpty()
                        && sunkens.inRadius(enemy.isRanged() ? 3 : 6, unit).empty()
                ) {
                    unit.addLog("Trickster");
                    return false;
                }
            }
        }

        // =========================================================

        main = Select.main();
        focusPoint = focusPoint();
        focusPointToBase = focusPoint.distTo(main);
        unitToEnemy = unit.distTo(enemy);
        unitToBase = unit.groundDist(main);
        enemyToBase = enemy.groundDist(main);
        enemyToFocus = enemy.groundDist(focusPoint);

        if (unitToEnemy <= 3 && unit.isDragoon() && enemy.isZealot() && unit.hp() <= 18) {
            return false;
        }

//        if (
////                (unit.isMelee() && unit.hasWeaponRangeToAttack(enemy, 0.1))
//                (unit.isMelee() && unitToEnemy <= 1.09)
//                || (unit.isRanged() && unit.hasWeaponRangeToAttack(enemy, 2))
//        ) {
//            if (unit.cooldownRemaining() <= 3 || unit.lastAttackFrameMoreThanAgo(40)) {
//                return true;
//            }
//        }

//        if (notAllowedToAttackTooFar(unit, enemy)) {
//            return false;
//        }
//
//        if (unit.isMelee() && enemyDistToBase > unitToBase) {
//            return false;
//        }

        // Zealots vs Zealot fix
        if (ProtossMissionAdjustments.allowsToAttackEnemyUnits(unit, enemy)) {
            return true;
        }

        if (main != null) {
            if (Select.enemy().inRadius(18, main).atLeast(1)) {
                return true;
            }

            if (Select.enemy().inRadius(18, Select.naturalOrMain()).atLeast(1)) {
                return true;
            }
        }

//        if (focusPointDistToBase < enemyDistToBase || enemyDistToBase < unitToBase) {
//            return true;
//        }

        return false;
    }

    @Override
    public boolean forcesUnitToFight(AUnit unit, Units enemies) {
        if (We.terran()) {
            return false;
        }

        if (
            unit.isMelee()
            && unit.friendsNear().combatBuildings(false).inRadius(5, unit).notEmpty()
            && !"Sparta".equals(unit.tooltip())
        ) {
            if (unit.hp() <= 18) {
                if (Enemy.protoss()) {
                    return false;
                }

                if (unit.hp() <= 10) {
                    return false;
                }
            }

            unit.addLog("ProtectBuilding");
            return true;
        }

        if (unit.hpLessThan(36) && unit.friendsNearCount() <= 2 && unit.lastAttackFrameMoreThanAgo(30 * 4)) {
            return false;
        }

        if (unit.isHydralisk()) {
            if (unit.woundPercent(60) || unit.meleeEnemiesNearCount(2) >= 2) {
                return false;
            }
        }

        if (
            unit.isDragoon()
                && enemies.onlyMelee() && unit.hp() >= 40
                && unit.lastAttackFrameMoreThanAgo(30 * 4)
                && unit.nearestEnemyDist() >= 2.8
        ) {
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

    @Override
    public double optimalDist(AUnit unit) {
        return (new MoveToDefendFocusPoint()).optimalDist(unit);
    }
}
