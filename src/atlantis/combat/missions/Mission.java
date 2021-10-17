package atlantis.combat.missions;

import atlantis.AGame;
import atlantis.Atlantis;
import atlantis.map.AMap;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.UnitActions;
import bwapi.Color;


/**
 * Represents behavior for squad of units e.g. DEFEND, CONTAIN (enemy at his base), ATTACK etc.
 */
public abstract class Mission {

    private static Mission instance;
    protected MissionFocusPointManager focusPointManager;
    private String name;
    protected APosition temporaryTarget = null;

    // =========================================================

    protected Mission(String name) {
        this.name = name;
        instance = this;
    }

    public abstract boolean update(AUnit unit);

    public boolean allowsToAttackEnemyUnit(AUnit unit, AUnit enemy) {
        return true;
    }

    // =========================================================

//    protected boolean handleUnitSafety(AUnit unit, boolean avoidBuildings, boolean avoidMelee) {
//        if (AAvoidEnemyDefensiveBuildings.avoidCloseBuildings(unit, false)) {
//            return true;
//        }
//
//        if (AAvoidEnemyMeleeUnitsManager.avoidCloseMeleeUnits(unit)) {
//            return true;
//        }
//
//        return false;
//    }

    protected boolean handleWeDontKnowWhereTheEnemyIs(AUnit unit) {
        if (temporaryTarget == null || AMap.isExplored(temporaryTarget)) {
            temporaryTarget = AMap.getRandomUnexploredPosition(unit.getPosition());
            System.out.println("Go to unexplored " + temporaryTarget);
        }
        if (temporaryTarget == null || AMap.isVisible(temporaryTarget)) {
            temporaryTarget = AMap.getRandomInvisiblePosition(unit.getPosition());
            System.out.println("Go to invisible " + temporaryTarget);
        }

        if (temporaryTarget != null) {
            unit.move(temporaryTarget, UnitActions.MOVE_TO_ENGAGE, "#FindEnemy");
            Atlantis.game().drawLineMap(unit.getPosition(), temporaryTarget, Color.Yellow);
            return true;
        }
        else {
            System.err.println("No invisible position found");
            return false;
        }
    }

    // =========================================================

    public static Mission getInstance() {
        return instance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public APosition focusPoint() {
        return focusPointManager.focusPoint();
    }

    public boolean isMissionContain() {
        return this.equals(Missions.CONTAIN);
    }

    public boolean isMissionDefend() {
        return this.equals(Missions.DEFEND);
    }

    public boolean isMissionAttack() {
        return this.equals(Missions.ATTACK);
    }

    public boolean isMissionUms() {
        return false;
//        return this.equals(Missions.UMS);
    }
}
