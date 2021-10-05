package atlantis.combat.missions;

import atlantis.Atlantis;
import atlantis.map.AChokepoint;
import atlantis.map.AMap;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import bwapi.Color;


/**
 * Represents behavior for squad of units e.g. DEFEND, ATTACK etc.
 */
public abstract class Mission {

    private static Mission instance;
    private String name;
    private MissionFocusPointManager focusPointManager;
    private MissionUnitManager unitManager;

    // =========================================================

    public Mission(String name, MissionFocusPointManager focusPointManager, MissionUnitManager unitManager) {
        this.name = name;
        this.focusPointManager = focusPointManager;
        this.unitManager = unitManager;
        instance = this;
    }

    // =========================================================

    public abstract boolean update(AUnit unit);

    // =========================================================

    public APosition focusPoint() {
        return focusPointManager.focusPoint();
    }

    public AChokepoint focusChokepoint() {
        return focusPointManager.getChokepoint();
    }

    protected boolean handleNoEnemyBuilding(AUnit unit) {
        APosition position = AMap.getRandomInvisiblePosition(unit.getPosition());
        if (position != null) {
            unit.attackPosition(position);
            Atlantis.game().drawLineMap(unit.getPosition(), position, Color.Red); //TODO DEBUG
            unit.setTooltip("#MA:Forward!");
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

    public MissionFocusPointManager getFocusPointManager() {
        return focusPointManager;
    }

    public MissionUnitManager getUnitManager() {
        return unitManager;
    }

    public boolean isMissionDefend() {
        return this.equals(Missions.DEFEND);
    }

    public boolean isMissionAttack() {
        return this.equals(Missions.ATTACK);
    }

    public boolean isMissionUmt() {
        return this.equals(Missions.UMT);
    }
    
}
