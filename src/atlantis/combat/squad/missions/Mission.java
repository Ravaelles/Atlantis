package atlantis.combat.squad.missions;

import atlantis.units.AUnit;
import atlantis.wrappers.APosition;



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
    
}
