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
    protected MissionDefendAllowsToAttack allowsToAttack;
    protected double focusPointToBase;
    protected double unitToEnemy;
    protected double unitToFocus;
    protected double unitToBase;
    protected double enemyToBase;
    protected double enemyToFocus;

    public MissionDefend(AUnit unit) {
        super("Defend");
        focusPointManager = new MissionDefendFocusPoint();
        allowsToAttack = new MissionDefendAllowsToAttack(this);
    }

    // =========================================================

    @Override
    public boolean update(AUnit unit) {
        if (AGame.isUms()) {
            return false;
        }

        this.unit = unit;
        this.main = Select.main();
        this.focusPoint = focusPoint();

        if (focusPoint == null) {
            return noFocusPoint();
        }

        // =========================================================

        // Don't reposition if enemies Near
        if (unit.enemiesNear().combatUnits().inRadius(7.2, unit).atLeast(2)) {
            return false;
        }

        return (new MoveToDefendFocusPoint()).move(unit, focusPoint);
    }

    // =========================================================

    @Override
    public boolean allowsToAttackEnemyUnit(AUnit unit, AUnit enemy) {
        return allowsToAttack.allowsToAttackEnemyUnit(unit, enemy);
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
                if (Enemy.protoss()) return false;

                if (unit.hp() <= 10) return false;
            }

            unit.addLog("ProtectBuilding");
            return true;
        }

        if (unit.hpLessThan(36) && unit.friendsNearCount() <= 2 && unit.lastAttackFrameMoreThanAgo(30 * 4)) {
            return false;
        }

        if (unit.isHydralisk()) {
            if (unit.woundPercentMin(60) || unit.meleeEnemiesNearCount(2) >= 2) {
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

    protected  boolean noFocusPoint() {
        if (!Have.base()) {
            return false;
        }

        System.err.println("Couldn't define choke point.");
        throw new RuntimeException("Couldn't define choke point.");
    }

    @Override
    public double optimalDist(AUnit unit) {
        return (new MoveToDefendFocusPoint()).optimalDist();
    }
}
