package atlantis.combat.missions;

import atlantis.combat.missions.focus.AFocusPoint;
import atlantis.combat.missions.focus.MissionFocusPoint;
import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.map.AMap;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.units.actions.Actions;
import bwapi.Color;


/**
 * Represents behavior for squad of units e.g. DEFEND, CONTAIN (enemy at his base), ATTACK etc.
 */
public abstract class Mission {

    private static Mission instance;
    protected MissionFocusPoint focusPointManager;
    private String name;
    protected APosition temporaryTarget = null;

    // =========================================================

    protected Mission(String name) {
        this.name = name;
        instance = this;
    }

    // =========================================================

    public abstract boolean update(AUnit unit);

    /**
     * Optimal distance to focus point or -1 if not defined.
     */
    public abstract double optimalDist(AUnit unit);

    // =========================================================

    public AFocusPoint focusPoint() {
        return focusPointManager.focusPoint();
    }

    // Template method
    public boolean allowsToRetreat(AUnit unit) {
        return true;
    }

    // Template method
    public boolean allowsToAttackEnemyUnit(AUnit unit, AUnit enemy) {
        if (enemy.isCombatBuilding()) {
            return allowsToAttackCombatBuildings(unit, enemy);
        }

        return true;
    }

    // Template method
    public boolean allowsToAttackCombatBuildings(AUnit unit, AUnit combatBuilding) {
        if (unit.isInfantry() && unit.hp() <= 39) {
            return false;
        }

        return unit.friendsNearCount() >= 7;
    }

    // Template method
    public boolean forcesUnitToFight(AUnit unit, Units enemies) {
        return false;
    }

    // =========================================================

    protected boolean enemyIsNearAnyOurBuilding(AUnit enemy) {
        return EnemyInfo.isEnemyNearAnyOurBase();
    }

//    protected boolean handleUnitSafety(AUnit unit, boolean avoidBuildings, boolean avoidMelee) {
//        if (AAvoidEnemyCombatBuildings.avoidCloseBuildings(unit, false)) {
//            return true;
//        }
//
//        if (AAvoidEnemyMeleeUnits.avoidCloseMeleeUnits(unit)) {
//            return true;
//        }
//
//        return false;
//    }

    protected boolean handleWeDontKnowWhereTheEnemyIs(AUnit unit) {
        if (unit.isMoving()) {
            return false;
        }

        // Go to random UNEXPLORED
        if ((A.isUms() || A.chance(10)) && (temporaryTarget == null || temporaryTarget.isExplored())) {
            temporaryTarget = AMap.getRandomUnexploredPosition(unit.position());
//            if (temporaryTarget != null) {
//            System.out.println("Go to unexplored " + temporaryTarget);
//            }
        }

        // Go to random INVISIBLE
        if (temporaryTarget == null || temporaryTarget.isPositionVisible()) {
            temporaryTarget = AMap.randomInvisiblePosition(unit);
//            if (temporaryTarget != null) {
//            System.out.println("Go to invisible " + temporaryTarget);
//            }
        }

        if (temporaryTarget != null) {
            unit.move(temporaryTarget, Actions.MOVE_ENGAGE, "#FindEnemy", true);
            APainter.paintLine(unit.position(), temporaryTarget, Color.Yellow);
            return true;
        }
        else {
//            if (!AGame.isUms()) {
//                System.err.println("No invisible position found");
//            }
            return false;
        }
    }

    // =========================================================

    @Override
    public String toString() {
        return "Mission " + name;
    }

    // =========================================================

    public static Mission get() {
        return instance;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isMissionContain() {
        return this.equals(Missions.CONTAIN);
    }

    public boolean isMissionDefend() {
        return this.equals(Missions.DEFEND);
    }

    public boolean isMissionSparta() {
        return this.equals(Missions.SPARTA);
    }

    public boolean isMissionDefendOrSparta() {
        return isMissionSparta() || isMissionDefend();
    }

    public boolean isMissionAttack() {
        return this.equals(Missions.ATTACK);
    }

    public boolean isMissionUms() {
        return false;
//        return this.equals(Missions.UMS);
    }

}
