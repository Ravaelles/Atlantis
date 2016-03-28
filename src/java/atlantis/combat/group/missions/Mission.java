package atlantis.combat.group.missions;

import atlantis.units.AUnit;



/**
 * Represents behavior for group of units e.g. DEFEND, ATTACK etc.
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

    /**
     * Returns true for units that should accept orders in this frame.
     */
    protected abstract boolean canIssueOrderToUnit(AUnit unit);

    // =========================================================

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}
