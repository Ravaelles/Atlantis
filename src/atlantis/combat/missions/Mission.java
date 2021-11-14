package atlantis.combat.missions;

import atlantis.AGame;
import atlantis.debug.APainter;
import atlantis.map.AMap;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.units.actions.UnitActions;
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

    public abstract boolean update(AUnit unit);

    // Template method
    public boolean allowsToAttackEnemyUnit(AUnit unit, AUnit enemy) {
        return true;
    }

    // Template method
    public boolean allowsToAttackDefensiveBuildings(AUnit unit, AUnit defensiveBuilding) {
        return false;
    }

    public boolean forcesUnitToFight(AUnit unit, Units enemies) {
        return false;
    }

    // =========================================================

//    protected boolean handleUnitSafety(AUnit unit, boolean avoidBuildings, boolean avoidMelee) {
//        if (AAvoidEnemyDefensiveBuildings.avoidCloseBuildings(unit, false)) {
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
        if (temporaryTarget == null || temporaryTarget.isExplored()) {
            temporaryTarget = AMap.getRandomUnexploredPosition(unit.position());
//            if (temporaryTarget != null) {
//                System.out.println("Go to unexplored " + temporaryTarget);
//            }
        }
        if (temporaryTarget == null || temporaryTarget.isVisible()) {
            temporaryTarget = AMap.getRandomInvisiblePosition(unit.position());
//            if (temporaryTarget != null) {
//                System.out.println("Go to invisible " + temporaryTarget);
//            }
        }

        if (temporaryTarget != null) {
            unit.move(temporaryTarget, UnitActions.MOVE_TO_ENGAGE, "#FindEnemy");
            APainter.paintLine(unit.position(), temporaryTarget, Color.Yellow);
            return true;
        }
        else {
            if (!AGame.isUms()) {
                System.err.println("No invisible position found");
            }
            return false;
        }
    }

    // =========================================================

    @Override
    public String toString() {
        return "Mission " + name;
    }

    // =========================================================

    public static Mission getInstance() {
        return instance;
    }

    public String name() {
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
