package atlantis.combat.squad.missions;

import atlantis.position.APosition;
import atlantis.units.AUnit;



/**
 * Represents behavior for squad of units e.g. DEFEND, ATTACK etc.
 */
public abstract class Mission {

    private String name;
    
    // =========================================================

    public Mission(String name) {
        this.name = name;
    }
    
    // =========================================================

    /**
     * If returns true, it's not allowed for micro managers to act.
     */
    public abstract boolean update(AUnit unit);

    public abstract APosition getFocusPoint();
    
    // =========================================================

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
