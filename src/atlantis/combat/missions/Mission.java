package atlantis.combat.missions;

import atlantis.position.APosition;
import atlantis.units.AUnit;



/**
 * Represents behavior for squad of units e.g. DEFEND, ATTACK etc.
 */
public abstract class Mission {

    private static Mission instance;
    private String name;

    // =========================================================

    protected Mission(String name) {
        this.name = name;
        instance = this;
    }

    // =========================================================

    public abstract boolean update(AUnit unit);

    public abstract APosition focusPoint();

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
