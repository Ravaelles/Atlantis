package atlantis.combat.missions;

import atlantis.Atlantis;
import atlantis.combat.micro.AAvoidDefensiveBuildings;
import atlantis.combat.micro.AAvoidEnemyMeleeUnitsManager;
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

    // =========================================================

    protected Mission(String name) {
        this.name = name;
        instance = this;
    }

    public abstract boolean update(AUnit unit);

    // =========================================================

//    protected boolean handleUnitSafety(AUnit unit, boolean avoidBuildings, boolean avoidMelee) {
//        if (AAvoidDefensiveBuildings.avoidCloseBuildings(unit, false)) {
//            return true;
//        }
//
//        if (AAvoidEnemyMeleeUnitsManager.avoidCloseMeleeUnits(unit)) {
//            return true;
//        }
//
//        return false;
//    }

    protected boolean handleNoEnemyBuilding(AUnit unit) {
        APosition position = AMap.getRandomInvisiblePosition(unit.getPosition());
        if (position != null) {
            unit.move(position, UnitActions.MOVE_TO_ENGAGE, "#MA:FindEnemy");
            Atlantis.game().drawLineMap(unit.getPosition(), position, Color.Red);
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

    public boolean isMissionUmt() {
        return false;
//        return this.equals(Missions.UMT);
    }
    
}
